"""
brand_dashboard.py
역할: 브랜드 벡터 → 포지셔닝 분석 + 인사이트 생성
호출 위치: main.py → POST /api/brand/analyze

함수 목록:
- clip01(), score100(), grade_from_score()  : 점수 정규화 헬퍼
- safe_mean()                               : None 안전 평균
- build_quadrant_summary()                  : 사분면 설명 생성
- build_similarity_gap()                    : 경쟁 브랜드 유사도 격차 계산
- compute_intent_alignment()               : 이미지 정합도 평균 계산
- compute_consistency_bundle()             : 최종 일관성 점수 Bundle
- topk_oos_projection()                    : 사용자 벡터 → 2D 좌표 투영
- compute_top3_competitors()               : 유사 브랜드 Top-N 도출
- build_insight_prompt_payload()           : LLM payload 조립
- build_single_snapshot_json()             : 최종 응답 JSON 조립
- analyze_brand()                          : 전체 분석 파이프라인
- generate_insight_llm()                   : GPT 인사이트 생성 (한국어 필드명)
"""

import json
import os
from pathlib import Path
import numpy as np
import pandas as pd
from sklearn.metrics.pairwise import cosine_similarity
from sklearn.manifold import MDS
from openai import OpenAI
from dotenv import load_dotenv

load_dotenv()

client = OpenAI()

# =====================================================
# 기존 브랜드 벡터 + 메타데이터 로드
# 서버 시작 시 1회만 실행됨
# DATA_DIR: .env에서 설정 (기본값: data)
# =====================================================
DATA_DIR = Path(os.getenv("DATA_DIR", "data"))
VECTOR_CACHE_PATH = DATA_DIR / "brand_vectors.npy"
META_CACHE_PATH = DATA_DIR / "brand_meta.json"

if not VECTOR_CACHE_PATH.exists() or not META_CACHE_PATH.exists():
    raise FileNotFoundError(
        f"브랜드 데이터 파일이 없습니다.\n"
        f"BrandMap/output/cache/ 의 파일 2개를 pythonserver/data/ 폴더에 복사해주세요.\n"
        f"필요 파일: brand_vectors.npy, brand_meta.json"
    )

vectors_dict = np.load(VECTOR_CACHE_PATH, allow_pickle=True).item()
with open(META_CACHE_PATH, "r", encoding="utf-8") as f:
    meta_dict = json.load(f)

brand_ids = list(vectors_dict.keys())
features = np.vstack([vectors_dict[bid] for bid in brand_ids])

meta_rows = []
for bid in brand_ids:
    meta_rows.append({
        "brand_id": bid,
        "brand_folder": meta_dict[bid]["brand_folder"],
        "category": meta_dict[bid]["category"],
        "brand_name": meta_dict[bid]["brand_name"]
    })
df_meta = pd.DataFrame(meta_rows)

# =====================================================
# MDS 연산 (서버 시작 시 1회만 실행)
# 기존 브랜드들의 2D 포지셔닝 좌표 계산
# =====================================================
cosine_sim = cosine_similarity(features)
distance_matrix = np.clip(1.0 - cosine_sim, 0.0, None)
mds = MDS(
    n_components=2,
    dissimilarity="precomputed",
    random_state=42,
    n_init=4,
    max_iter=300
)
mds_coords = mds.fit_transform(distance_matrix)
df_meta["mds_d1"] = mds_coords[:, 0]
df_meta["mds_d2"] = mds_coords[:, 1]


# =====================================================
# 점수 계산 헬퍼 함수
# =====================================================

GRADE_BANDS = [
    (90, "A"),
    (80, "B"),
    (70, "C"),
    (60, "D"),
    (0,  "E"),
]

# CLIP ViT-B/32 이미지-텍스트 정합도 실질 최대값
# (8~32장 이미지 평균 기준, OpenAI CLIP 특성 반영)
CLIP_IMAGE_TEXT_MAX = 0.35


def clip01(x: float) -> float:
    """값을 0~1 범위로 강제 제한"""
    return float(np.clip(x, 0.0, 1.0))


def score100(x: float) -> int:
    """0~1 값을 0~100 정수로 변환"""
    return int(np.clip(round(float(x)), 0, 100))


def grade_from_score(score: int) -> str:
    """점수를 A~E 등급으로 변환"""
    for threshold, grade in GRADE_BANDS:
        if score >= threshold:
            return grade
    return "E"


def safe_mean(values: list, default: float = 0.0) -> float:
    """None 제거 후 평균 계산. 값이 없으면 default 반환"""
    arr = np.array([v for v in values if v is not None], dtype=float)
    return float(arr.mean()) if arr.size else float(default)


def build_quadrant_summary(
    quadrant: str,
    x: float,
    y: float,
    x_label: str = None,
    y_label: str = None
) -> str:
    """브랜드 맵 사분면 설명 문자열 생성"""
    return (
        f"이 브랜드는 {x_label or 'x'} 축에서 {quadrant} 성향을 보이고, "
        f"좌표는 ({x:.2f}, {y:.2f})이다."
    )


def build_similarity_gap(top3_df: pd.DataFrame) -> dict:
    """
    Top1 vs Top2 유사도 차이 계산
    차이가 클수록 1등 브랜드와 포지셔닝이 뚜렷하게 구분됨
    """
    if top3_df is None or len(top3_df) == 0:
        return {"gap": 0.0, "explanation": "비교 대상이 충분하지 않습니다."}
    top1 = float(top3_df.iloc[0]["similarity"])
    top2 = float(top3_df.iloc[1]["similarity"]) if len(top3_df) > 1 else top1
    gap = max(top1 - top2, 0.0)
    return {
        "gap": round(gap, 3),
        "explanation": "top3 경쟁사 대비 유사도 격차가 이 값보다 커질수록 차별화가 더 선명해집니다."
    }


def compute_intent_alignment(image_alignments: list) -> float:
    """
    이미지 정합도 평균 계산
    image_alignments: [{"similarity": 0.82, ...}, ...]
    """
    if not image_alignments:
        return 0.0
    sims = [clip01(x.get("similarity", 0.0)) for x in image_alignments]
    return safe_mean(sims, 0.0)


def compute_consistency_bundle(
    image_alignments: list,
    axis_alignment_raw: float,
    selected_tags: list = None,
    risk_flags: list = None,
) -> dict:
    """
    최종 일관성 점수 Bundle 생성
    이미지-텍스트 정합도(intent_raw)만 사용
    CLIP_IMAGE_TEXT_MAX로 정규화하여 0~100 점수로 변환
    axis_alignment_raw는 포지셔닝 지표이므로 일관성 계산에서 제외
    """
    intent_raw = compute_intent_alignment(image_alignments)
    axis_raw = clip01(axis_alignment_raw)
    normalized = min(intent_raw / CLIP_IMAGE_TEXT_MAX, 1.0)
    consistency_score = score100(normalized * 100)
    intent_score = score100(intent_raw * 100)
    axis_score = score100(axis_raw * 100)

    return {
        "score": consistency_score,
        "grade": grade_from_score(consistency_score),
        "intentAlignment": intent_score,
        "axisAlignment": axis_score,
        "evidence": {
            "imageAlignments": image_alignments,
            "intentAlignment": intent_score,
            "axisAlignment": axis_score,
        },
        "risk": {
            "flags": risk_flags or [],
            "warning": "이미지 평균 기반으로 평가되며, 아웃라이어 설명은 인사이트에서 처리합니다."
        }
    }


# =====================================================
# 브랜드 포지셔닝 계산
# =====================================================

def topk_oos_projection(
    new_vec: np.ndarray,
    base_vecs: np.ndarray,
    base_coords: np.ndarray,
    k: int = 5,
    power: float = 2.0,
    eps: float = 1e-12,
) -> tuple:
    """
    사용자 벡터를 기존 브랜드 맵 2D 좌표계로 투영 (OOS projection)
    반환: (2D 좌표 np.ndarray, 전체 거리 배열, top-k 인덱스)
    """
    sims = cosine_similarity(new_vec.reshape(1, -1), base_vecs).ravel()
    dists = np.clip(1.0 - sims, 0.0, None)
    top_idx = np.argsort(dists)[:k]
    top_dists = dists[top_idx]
    top_coords = base_coords[top_idx]

    # 거의 동일한 브랜드가 있으면 해당 위치로 직접 매핑
    if np.any(top_dists < eps):
        return top_coords[np.argmin(top_dists)].copy(), dists, top_idx

    weights = 1.0 / np.power(top_dists + eps, power)
    coords = np.average(top_coords, axis=0, weights=weights)
    return coords, dists, top_idx


def compute_top3_competitors(
    user_vec: np.ndarray,
    topn: int = 3,
) -> tuple:
    """
    사용자 벡터와 기존 브랜드 간 cosine similarity 계산 → Top-N 반환
    features, df_meta는 서버 시작 시 1회 로드된 전역 변수 사용
    반환: (전체 랭킹 DataFrame, Top-N DataFrame)
    """
    sims = cosine_similarity(user_vec.reshape(1, -1), features).ravel()
    rankdf = df_meta.copy()
    rankdf["similarity"] = sims
    rankdf = rankdf.sort_values("similarity", ascending=False).reset_index(drop=True)
    return rankdf, rankdf.head(topn)


def build_insight_prompt_payload(
    brand_id: str,
    brand_profile: dict,
    image_alignments: list,
    axis_info: dict,
    competitors: list,
    consistency: dict,
) -> dict:
    """LLM 인사이트 생성용 payload 조립"""
    return {
        "brandId": brand_id,
        "brandProfile": brand_profile,
        "imageAlignments": image_alignments,
        "axisInfo": axis_info,
        "competitors": competitors,
        "consistency": consistency,
    }


def build_single_snapshot_json(
    brand_id: str,
    user_x: float,
    user_y: float,
    user_quadrant: str,
    top3: pd.DataFrame,
    consistency_bundle: dict,
    image_alignments: list,
    quadrant_summary: str,
    similarity_gap: dict,
    risk: dict,
    strategy: dict,
    brand_profile: dict,
    user_text: str,
) -> dict:
    """최종 응답 JSON 조립"""
    return {
        "brandId": brand_id,
        "brandProfile": brand_profile,
        "userText": user_text,
        "consistency": consistency_bundle,
        "position": {
            "x": float(user_x),
            "y": float(user_y),
            "quadrant": user_quadrant,
            "quadrantSummary": quadrant_summary,
        },
        "similarBrands": [
            {
                "name": row["brand_name"],
                "similarity": float(row["similarity"]),
            }
            for _, row in top3.iterrows()
        ],
        "similarityGap": similarity_gap,
        "risk": risk,
        "strategy": strategy,
        "evidence": {
            "imageAlignments": image_alignments,
            "intentAlignment": consistency_bundle["intentAlignment"],
            "axisAlignment": consistency_bundle["axisAlignment"],
        },
    }


def analyze_brand(
    project_id: str,
    brand_vector: list,
    brand_profile: dict,
    user_text: str,
    selected_tags: list,
    image_alignments: list,
) -> dict:
    """
    브랜드 분석 전체 파이프라인 실행 함수
    반환: snapshot dict (Spring Boot가 이걸 받아 DB에 저장)
    """
    user_vec = np.array(brand_vector, dtype=np.float32).reshape(-1)

    # 1. OOS projection → 2D 좌표
    user_xy, _, _ = topk_oos_projection(
        new_vec=user_vec,
        base_vecs=features,
        base_coords=mds_coords,
        k=5,
        power=2.0,
    )
    user_x, user_y = float(user_xy[0]), float(user_xy[1])

    # 2. Top3 경쟁 브랜드
    _, top3 = compute_top3_competitors(user_vec, topn=3)

    # 3. 사분면 결정
    if user_x >= 0 and user_y >= 0:
        user_quadrant = "Q1"
    elif user_x < 0 and user_y >= 0:
        user_quadrant = "Q2"
    elif user_x < 0 and user_y < 0:
        user_quadrant = "Q3"
    else:
        user_quadrant = "Q4"

    # 4. 분석 지표 계산
    quadrant_summary = build_quadrant_summary(
        user_quadrant, user_x, user_y,
        x_label="performance", y_label="lifestyle"
    )
    similarity_gap = build_similarity_gap(top3)
    axis_alignment_raw = float(top3["similarity"].mean()) if len(top3) > 0 else 0.0
    risk_flags = ["브랜드 이미지 희석 가능성"] if axis_alignment_raw < 0.8 else []

    # 5. 일관성 점수
    consistency_bundle = compute_consistency_bundle(
        image_alignments=image_alignments,
        axis_alignment_raw=axis_alignment_raw,
        selected_tags=selected_tags,
        risk_flags=risk_flags,
    )

    # 6. 전략 방향 (similarBrands 기반)
    keep_items = [top3.iloc[0]["brand_name"]] if len(top3) > 0 else []
    strengthen_items = [row["brand_name"] for _, row in top3.head(2).iterrows()]
    strategy = {
        "keep": keep_items,
        "strengthen": strengthen_items,
        "remove": [],
    }

    # 7. snapshot 조립
    snapshot = build_single_snapshot_json(
        brand_id=project_id,
        user_x=user_x,
        user_y=user_y,
        user_quadrant=user_quadrant,
        top3=top3,
        consistency_bundle=consistency_bundle,
        image_alignments=image_alignments,
        quadrant_summary=quadrant_summary,
        similarity_gap=similarity_gap,
        risk=consistency_bundle["risk"],
        strategy=strategy,
        brand_profile=brand_profile,
        user_text=user_text,
    )

    # 8. LLM 인사이트 생성 후 snapshot에 추가
    insight_json = generate_insight_llm(snapshot)
    snapshot["insight"] = insight_json

    return snapshot


# =====================================================
# GPT 인사이트 생성 (한국어 필드명)
# =====================================================

INSIGHT_SCHEMA = {
    "name": "brand_insight_response",
    "strict": True,
    "schema": {
        "type": "object",
        "additionalProperties": False,
        "properties": {
            "종합평가": {"type": "string"},
            "강점": {
                "type": "array",
                "items": {"type": "string"},
                "minItems": 2,
                "maxItems": 3
            },
            "기회": {
                "type": "array",
                "items": {"type": "string"},
                "minItems": 2,
                "maxItems": 3
            },
            "액션": {
                "type": "array",
                "items": {"type": "string"},
                "minItems": 3,
                "maxItems": 4
            },
            "주의점": {
                "type": "array",
                "items": {"type": "string"},
                "minItems": 2,
                "maxItems": 3
            },
            "우선순위": {
                "type": "object",
                "additionalProperties": False,
                "properties": {
                    "유지": {
                        "type": "array",
                        "items": {"type": "string"},
                        "minItems": 1,
                        "maxItems": 2
                    },
                    "강화": {
                        "type": "array",
                        "items": {"type": "string"},
                        "minItems": 1,
                        "maxItems": 2
                    },
                    "제거": {
                        "type": "array",
                        "items": {"type": "string"},
                        "minItems": 0,
                        "maxItems": 2
                    }
                },
                "required": ["유지", "강화", "제거"]
            }
        },
        "required": ["종합평가", "강점", "기회", "액션", "주의점", "우선순위"]
    }
}


def generate_insight_llm(snapshot: dict) -> dict:
    """snapshot → GPT(gpt-4o-mini) → 한국어 브랜드 인사이트 JSON 생성"""
    system_prompt = """
당신은 브랜드 코치입니다.
사용자가 기획 중인 브랜드의 현재 상태를 분석 데이터를 바탕으로
짧고 명확한 한국어 인사이트로 정리해주는 역할입니다.

[입력 데이터 활용 방법]
반드시 아래 데이터를 실제로 반영해서 씁니다. 어디에나 해당되는 일반적인 말은 금지입니다.
- brandProfile.description / core_values / keywords → 종합평가와 강점에 반영
- userText → 사용자의 실제 의도를 파악해 액션과 기회에 반영
- similarBrands → 유사한 브랜드와의 비교 맥락 제공에 활용
- consistency.score → 이미지와 브랜드 설명의 일치 정도를 주의점에 반영

[데이터 해석 기준]
- similarBrands: 1위 브랜드명을 종합평가에 직접 언급합니다. (한글 발음 표기로)
  similarity 0.90 이상 → "~와 매우 유사한 방향입니다"
  similarity 0.85~0.90 → "~와 비슷한 영역에 있습니다"
  similarity 0.85 미만 → "~와는 다른 독자적인 영역을 형성하고 있습니다"

- consistency.score:
  70 이상 → 이미지와 브랜드 설명이 잘 맞는 편. 강점에 반영.
  60~69 → 일부 이미지가 방향과 다를 수 있음. 주의점에 반영.
  60 미만 → 이미지 선택을 재검토할 필요가 있음. 주의점에 반영.

- position.quadrant:
  Q1 (x>0, y>0) → active + performance 성향
  Q2 (x<0, y>0) → active + lifestyle 성향
  Q3 (x<0, y<0) → calm + lifestyle 성향
  Q4 (x>0, y<0) → calm + performance 성향
  → 이 브랜드가 속한 사분면 특성을 종합평가에 반영합니다.

- userText → 사용자가 직접 쓴 말에서 핵심 의도 1~2개를 뽑아 액션에 구체적으로 반영합니다.

[말투 원칙]
- 따뜻하고 직접적인 코치 말투로 씁니다.
- 보고서처럼 딱딱하게 쓰지 말고 대화하듯 씁니다.
- 짧고 명확한 문장을 씁니다.
- 같은 말을 반복하지 않습니다.
- 겁을 주거나 비판하는 식으로 쓰지 않습니다.
- 단정적으로 몰아붙이지 않습니다.

[언어 규칙]
한국어로만 씁니다.
영문 알파벳(A~Z, a~z)을 문장 안에 직접 쓰지 마세요.
브랜드 이름 등 고유명사는 한글 발음 표기로 씁니다.
한자를 그대로 쓰지 마세요. 한자에서 온 한국어 단어는 사용 가능합니다.

[금지 표현]
근거 없는 추측 → "~할 수 있습니다" "~같아요" "~일 것 같습니다" "~싶어요"
전문 용어 과잉 → "정합성" "포지셔닝" "경쟁 우위" "전략적 방향성" "브랜드 자산" "시장 지배력" "차별화" "독자성" "정체성 희석" "콘셉트" "일관성"
추상 표현 → "장면" "분위기" "감각"

[권장 표현]
현재 상태 설명 → "~한 편입니다" "~합니다"
필요한 것 → "~이 필요합니다" "~하는 것이 중요합니다"
구체적 묘사 → "이미지" "인상" "느낌" "스토리" "대표 이미지"

[각 필드 작성 기준]
종합평가 (2~3문장):
입력된 브랜드 설명과 포지션을 바탕으로 이 브랜드의 현재 특성을 설명합니다.
이 브랜드에만 해당되는 말로 구체적으로 씁니다.

강점 (2~3개, 각 30자 이내):
지금 이 브랜드에서 관찰되는 실제 강점입니다. "~한 편입니다" 형태로 씁니다.
예시: "이미지와 브랜드 설명의 방향이 잘 맞는 편입니다."

기회 (2~3개, 각 30자 이내):
지금 데이터를 보면 더 발전시킬 수 있는 구체적인 영역입니다.
"~하면 좋습니다" "~로 이어집니다" 형태로 씁니다.

액션 (3~4개, 각 30자 이내):
지금 당장 실행할 수 있는 행동입니다. "~해보세요" 형태로 씁니다.
막연한 말이 아니라 실제로 할 수 있는 행동을 씁니다.
예시: "브랜드를 한 문장으로 정리해보세요."
반드시 userText에서 사용자가 직접 언급한 내용(커뮤니티, 소품, 의류 등)에서 출발합니다.
userText에 없는 개념(사회적 이슈, 트렌드, 마케팅 채널 등)을 임의로 만들지 않습니다.

주의점 (2~3개, 각 30자 이내):
데이터에서 보이는 주의할 부분입니다. "~될 수 있습니다" 형태로 씁니다.
공포감이나 경고처럼 쓰지 않고 중립적으로 씁니다.
consistency.score와 similarBrands에서 직접 읽히는 내용만 씁니다.
근거 없는 제품/마케팅 조언은 쓰지 않습니다.

우선순위:
- 유지 (1~2개): 지금 잘 되고 있어서 그대로 가져갈 것
- 강화 (1~2개): 더 발전시키면 좋을 것
- 제거 (0~2개): 지금 방향과 맞지 않는 것. 없으면 빈 배열.

[핵심 원칙]
읽는 사람이 "그래서 내가 뭘 하면 되는지" 바로 알 수 있어야 합니다.
입력 데이터에 있는 구체적인 정보를 반드시 활용합니다.
"""

    response = client.responses.parse(
        model="gpt-4o-mini",
        input=[
            {"role": "system", "content": system_prompt},
            {"role": "user", "content": json.dumps(snapshot, ensure_ascii=False)}
        ],
        text={
            "format": {
                "type": "json_schema",
                "name": INSIGHT_SCHEMA["name"],
                "schema": INSIGHT_SCHEMA["schema"],
                "strict": True
            }
        }
    )

    return json.loads(response.output_text)
