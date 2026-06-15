"""
brand_ai_engine.py
역할: 사용자 입력(텍스트 + 태그 + 이미지 URL) → 브랜드 벡터 생성
호출 위치: main.py → POST /api/brand/vector

함수 목록:
- build_brand_profile_payload()   : 브랜드 스키마 → 프로필 subset 추출
- extract_brand_schema()          : GPT 호출 → 브랜드 스키마 생성
- embed_text()                    : 텍스트 → CLIP 벡터
- weighted_brand_text_vector()    : 스키마 필드별 가중 평균 벡터
- load_image_from_url()           : URL → PIL Image
- create_image_embedding()        : PIL Image → CLIP 벡터
- create_mean_image_vector()      : 이미지 목록 → 평균 벡터 + 개별 벡터 목록
- compute_image_alignments()      : 이미지별 텍스트 유사도 계산
- create_brand_vector()           : 텍스트 + 이미지 → 최종 브랜드 벡터
- build_user_brand_vector()       : 전체 파이프라인 실행
"""

import json
import os
import numpy as np
import torch
import requests
import open_clip
from PIL import Image
from io import BytesIO
from openai import OpenAI
from dotenv import load_dotenv
from sklearn.metrics.pairwise import cosine_similarity

load_dotenv()

# OpenAI 클라이언트 (OPENAI_API_KEY 환경변수에서 자동 로드)
client = OpenAI()

# =====================================================
# CLIP 모델 로드
# 서버 시작 시 1회만 실행됨
# API 요청마다 재로드되지 않으므로 속도에 영향 없음
# =====================================================
model, _, preprocess = open_clip.create_model_and_transforms(
    "ViT-B-32",
    pretrained="laion2b_s34b_b79k"
)
tokenizer = open_clip.get_tokenizer("ViT-B-32")


def build_brand_profile_payload(result: dict) -> dict:
    """브랜드 스키마에서 LLM 인사이트 생성용 프로필 subset 추출"""
    return {
        "category": result.get("category", ""),
        "description": result.get("description", ""),
        "core_values": result.get("core_values", []),
        "keywords": result.get("keywords", []),
        "slogans_core": result.get("slogans_core", ""),
        "slogans_campaign": result.get("slogans_campaign", ""),
    }


def extract_brand_schema(user_text: str, user_tags: list) -> dict:
    """
    사용자 입력 → GPT(gpt-4o-mini) → 브랜드 스키마 JSON 생성
    반환: {"result": 전체 스키마 dict, "brand_profile": 프로필 subset dict}
    """
    prompt = f"""
You are an expert brand strategist.
The user may write in Korean or English.
Your job is to:
1. Understand the user's brand intent.
2. Translate to English if necessary.
3. Normalize the information into a brand profile.
4. Preserve the user's intended meaning.
5. Do NOT invent unrelated concepts.
6. Infer missing information only when strongly implied.
7. Generate text suitable for CLIP embedding and brand similarity comparison.
Output JSON only.

Schema:
{{
 "category": "",
 "description": "",
 "core_values": [],
 "keywords": [],
 "slogans_core": "",
 "slogans_campaign": ""
}}

Field Rules:
category:
- Short category label.
- Examples:
 "Running Lifestyle"
 "Premium Athleisure"
 "Performance Running"
 "Outdoor Adventure"

description:
- 1 sentence.
- Explain what the brand is.

core_values:
- 3~5 values.
- MUST be a list of strings
- Example:
 ["Consistency", "Wellness", "Balance"]

keywords:
User selected tags:
{user_tags}

TASK:
You are a tag classifier.

Rules:
- Use ONLY the given user tags.
- Do NOT modify, translate, or create new tags.
- Assign each tag to its correct category using the taxonomy below.

TAXONOMY:

Active:
dynamic
speed
intense
power
race
energy

Calm:
wellness
minimal
soft
mindful
balance
relaxed

Performance:
training
running
sport
trail
outdoor
athlete

Lifestyle:
street
fashion
casual
urban
daily
stylish

Return JSON in exactly this format:
{{
  "Active": [],
  "Calm": [],
  "Performance": [],
  "Lifestyle": []
}}

Return only structured JSON output.

slogans_core:
- One sentence.
- Summarize the brand philosophy.
- Stay close to the user's intent.
- Not an advertising slogan.

slogans_campaign:
- One sentence.
- Express the brand direction.
- Can be slightly more expressive.
- Still grounded to the user's intent.

Important:
The resulting profile should be comparable in quality, depth, clarity, and structure to the following reference examples.
Return strictly valid JSON.
All fields must match schema types exactly.
Do not return strings for array fields.

Reference Example 1:
Brand: Alo Yoga
Category: Premium Athleisure
Description: A premium athleisure brand inspired by yoga and mindfulness.
Core Values: Ethical production, Mindful movement, Wellness
Keywords: Calm, Wellness, Minimal, Stylish
Slogans Core: Move with purpose, lead with heart.
Slogans Campaign: Inspiring mindful movement.

Reference Example 2:
Brand: Nike
Category: Global Performance Sportswear
Description: A global sports brand that drives sport forward through innovation, community, and performance.
Core Values: Innovation, Inspiration, Athletic excellence, Community
Keywords: Performance, Dynamic, Athlete, Power
Slogans Core: Just Do It.
Slogans Campaign: If you have a body, you are an athlete.

Use these examples only as references for quality and writing style.
Do not copy, imitate, or reference the brands directly.
Generate a unique profile based solely on the user's intent.

User Input:
{user_text}
"""

    response = client.chat.completions.create(
        model="gpt-4o-mini",
        temperature=0.2,
        response_format={"type": "json_object"},
        messages=[
            {
                "role": "system",
                "content": (
                    "You extract brand concepts into structured JSON. "
                    "Return valid JSON only. "
                    "Never output markdown. "
                    "Never output explanations. "
                    "Never output code blocks."
                )
            },
            {
                "role": "user",
                "content": prompt
            }
        ]
    )

    raw = response.choices[0].message.content
    try:
        result = json.loads(raw)
    except json.JSONDecodeError:
        # JSON 파싱 실패 시 빈 스키마 반환 (서비스 중단 방지)
        result = {
            "category": "",
            "description": "",
            "core_values": [],
            "keywords": [],
            "slogans_core": "",
            "slogans_campaign": ""
        }

    brand_profile = build_brand_profile_payload(result)
    return {"result": result, "brand_profile": brand_profile}


def embed_text(text: str) -> np.ndarray:
    """
    텍스트 1개 → CLIP 512차원 벡터
    빈 문자열이면 0벡터 반환
    """
    if not text:
        return np.zeros(512, dtype=np.float32)
    tokens = tokenizer(str(text))
    with torch.no_grad():
        text_features = model.encode_text(tokens)
        text_features /= text_features.norm(dim=-1, keepdim=True)
    return text_features.cpu().numpy()[0]


def weighted_brand_text_vector(schema: dict) -> np.ndarray:
    """
    브랜드 스키마 필드별 가중 평균 벡터 생성
    가중치: description(40%) + core_values(25%) + keywords(25%) + slogans(각 5%)
    """
    section_weights = {
        "description": 0.40,
        "core_values": 0.25,
        "keywords": 0.25,
        "slogans_core": 0.05,
        "slogans_campaign": 0.05,
    }
    vectors = []
    weights = []
    for field, weight in section_weights.items():
        text = schema.get(field, "")
        if text:
            vectors.append(embed_text(str(text)))
            weights.append(weight)
    if not vectors:
        return np.zeros(512, dtype=np.float32)
    mat = np.vstack(vectors)
    text_vector = np.average(mat, axis=0, weights=weights)
    norm = np.linalg.norm(text_vector)
    if norm > 0:
        text_vector /= norm
    return text_vector


def load_image_from_url(image_url: str) -> Image.Image:
    """URL → PIL Image 변환. 요청 실패 시 예외 발생"""
    response = requests.get(image_url, timeout=10)
    response.raise_for_status()
    return Image.open(BytesIO(response.content)).convert("RGB")


def create_image_embedding(image: Image.Image) -> np.ndarray:
    """PIL Image → CLIP 512차원 벡터"""
    image_tensor = preprocess(image).unsqueeze(0)
    with torch.no_grad():
        image_features = model.encode_image(image_tensor)
        image_features /= image_features.norm(dim=-1, keepdim=True)
    return image_features.cpu().numpy()[0]


def create_mean_image_vector(image_urls: list) -> tuple:
    """
    이미지 URL 목록 → (평균 이미지 벡터, 개별 이미지 벡터 목록)
    평균 벡터: 최종 브랜드 벡터 계산에 사용
    개별 벡터: 이미지별 텍스트 유사도(image_alignments) 계산에 사용
    반환: (mean_vector: np.ndarray, individual_vectors: list[np.ndarray])
    """
    individual_vectors = []
    for url in image_urls:
        image = load_image_from_url(url)
        vector = create_image_embedding(image)
        individual_vectors.append(vector)

    mean_vector = np.mean(individual_vectors, axis=0)
    norm = np.linalg.norm(mean_vector)
    if norm > 0:
        mean_vector = mean_vector / norm

    return mean_vector, individual_vectors


def compute_image_alignments(
    image_urls: list,
    individual_image_vectors: list,
    text_vector: np.ndarray
) -> list:
    """
    각 이미지가 브랜드 텍스트 설명과 얼마나 일치하는지 계산
    이미지 벡터 vs 텍스트 벡터 cosine similarity
    반환: [{"imageIndex": 1, "imageUrl": "...", "similarity": 0.82}, ...]
    """
    alignments = []
    text_vec_2d = text_vector.reshape(1, -1)
    for i, (url, img_vec) in enumerate(zip(image_urls, individual_image_vectors)):
        sim = float(cosine_similarity(img_vec.reshape(1, -1), text_vec_2d)[0][0])
        alignments.append({
            "imageIndex": i + 1,
            "imageUrl": url,
            "similarity": round(sim, 4),
        })
    return alignments


def create_brand_vector(text_vector: np.ndarray, image_vector: np.ndarray) -> np.ndarray:
    """
    최종 브랜드 벡터 생성
    이미지 70% + 텍스트 30% 가중 합산 후 L2 정규화
    """
    brand_vector = image_vector * 0.7 + text_vector * 0.3
    norm = np.linalg.norm(brand_vector)
    if norm > 0:
        brand_vector = brand_vector / norm
    return brand_vector


def build_user_brand_vector(
    user_text: str,
    image_urls: list,
    user_selected_tags: list
) -> dict:
    """
    전체 파이프라인 실행 함수
    Spring Boot → main.py → 이 함수 순으로 호출됨

    반환 dict:
        brand_vector      : 512차원 numpy 배열 (Spring Boot에 전달)
        brand_profile     : GPT 생성 브랜드 프로필 (LLM 인사이트에 활용)
        brand_schema      : 브랜드 스키마 전체
        text_vector       : 텍스트 임베딩 벡터
        image_alignments  : 이미지별 텍스트 유사도 목록
    """
    # 이미지 장수 검증 (8~12장)
    if not (8 <= len(image_urls) <= 12):
        raise ValueError(
            f"이미지는 8~12장이어야 합니다. 현재: {len(image_urls)}장"
        )

    # 태그 개수 검증 (1~4개)
    if not (1 <= len(user_selected_tags) <= 4):
        raise ValueError(
            f"태그는 1~4개이어야 합니다. 현재: {len(user_selected_tags)}개"
        )

    # STEP 1: GPT → 브랜드 스키마 + 프로필 생성
    schema_result = extract_brand_schema(user_text, user_selected_tags)
    brand_schema = schema_result["result"]
    brand_profile = schema_result["brand_profile"]

    # STEP 2: 텍스트 벡터 생성
    text_vector = weighted_brand_text_vector(brand_schema)

    # STEP 3: 이미지 벡터 생성 (평균 + 개별)
    image_vector, individual_image_vectors = create_mean_image_vector(image_urls)

    # STEP 4: 이미지-텍스트 유사도 계산
    image_alignments = compute_image_alignments(
        image_urls, individual_image_vectors, text_vector
    )

    # STEP 5: 최종 브랜드 벡터 생성
    brand_vector = create_brand_vector(text_vector, image_vector)

    return {
        "brand_vector": brand_vector,
        "brand_profile": brand_profile,
        "brand_schema": brand_schema,
        "text_vector": text_vector,
        "image_alignments": image_alignments,
    }
