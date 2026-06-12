import json
import torch
import numpy as np
import asyncio
from transformers import CLIPProcessor, CLIPModel
from PIL import Image
from io import BytesIO
from sklearn.cluster import KMeans
from openai import OpenAI
import httpx
import config

# AI 모델 초기화 (서버 구동 시 1회만 로드)
client = OpenAI(api_key=config.OPENAI_API_KEY)
device = "cuda" if torch.cuda.is_available() else "cpu"
model = CLIPModel.from_pretrained("openai/clip-vit-base-patch32").to(device)
processor = CLIPProcessor.from_pretrained("openai/clip-vit-base-patch32")

def generate_queries_with_llm(item, tags, intro):
    system_prompt = """You are a World-Class Visual Art Director and AI Prompter.
Analyze the user's detailed input: 'Brand Intro' (Q1) and 'Scene/Object Hint' (Q2).
Your goal is to translate these specific narratives into high-fidelity visual search queries.

CRITICAL ARCHITECTURE AWARENESS (DUAL-API STRATEGY):
You must understand that we use TWO different stock photo APIs depending on the category. You must tailor the "query" to the specific strengths of each API.
1. UNSPLASH API (For: essence, sports, lifestyle, environment, tone): 
   - Unsplash is a moody, artistic photography community. 
   - Rule: Keep the "query" extremely simple and emotional. MAX 1-2 basic words. Do not use complex technical terms.
2. PEXELS API (For: product, material): 
   - Pexels is a commercial stock photo database. It is excellent for clear, literal objects and textures.
   - Rule: You can be slightly more literal here. For materials, use "[Material] background" or "[Material] texture" (e.g., "leather texture", "metal background"). For products, use "[Item] flatlay" or "[Item] studio".

TIME & LIGHTING CONSISTENCY:
Identify the core lighting or time-of-day (e.g., night, dawn, neon, sunny) from the input and apply it across ALL "clip" prompts to maintain a highly cohesive moodboard.

CATEGORY SPECIFICATIONS:
- 'essence' (Unsplash): Abstract visual mood. Query: [1-2 words like "dark abstract"].
- 'sports' (Unsplash): Human action. Query: [1 broad word like "running"]. Clip: [Action + lighting].
- 'lifestyle' (Unsplash): Everyday vibe. Query: [1-2 words like "night street"].
- 'environment' (Unsplash): Landscape/architecture. Query: [1-2 words like "concrete building"].
- 'tone' (Unsplash): Cinematic color grading. Query: [1-2 words like "shadows"].
- 'product' (Pexels): Finished items ONLY. Query: [2-3 words like "jacket flatlay", "sneakers studio"]. Clip: [Item + mood]. STRICTLY NO humans.
- 'material' (Pexels): Extreme macro close-up of deduced raw materials. Query: [2 words ONLY: "leather texture", "nylon background"]. Clip: [Extreme close-up of specific material]. STRICTLY NO stationery, pens, or flags.

CRITICAL INSTRUCTION FOR OUTPUT FORMAT:
Output ONLY a valid JSON object. Do not wrap in markdown blocks.

{
  "essence": {"query": "...", "clip": "..."},
  "sports": {"query": "...", "clip": "..."},
  "lifestyle": {"query": "...", "clip": "..."},
  "environment": {"query": "...", "clip": "..."},
  "tone": {"query": "...", "clip": "..."},
  "product": {"query": "...", "clip": "..."},
  "material": {"query": "...", "clip": "..."}
}
""" 
    user_prompt = f"Item: {item}\nTags: {', '.join(tags)}\nIntro: {intro}"
    try:
        response = client.chat.completions.create(
            model="gpt-4o-mini",
            response_format={ "type": "json_object" }, 
            messages=[{"role": "system", "content": system_prompt}, {"role": "user", "content": user_prompt}]
        )
        return json.loads(response.choices[0].message.content)
    except Exception as e:
        print("❌ AI 답변 파싱 에러:", e)
        return {}

async def fetch_image(async_client, image_data):
    try:
        response = await async_client.get(image_data["thumb_url"], timeout=3.0)
        img = Image.open(BytesIO(response.content)).convert("RGB")
        return {"img": img, "small_url": image_data["small_url"]}
    except:
        return None

def filter_and_get_top_images(text_prompt, valid_results):
    if not valid_results: return []
    images = [res["img"] for res in valid_results]
    inputs = processor(text=[text_prompt], images=images, return_tensors="pt", padding=True, truncation=True).to(config.device if hasattr(config, 'device') else device)
    with torch.inference_mode():
        probs = model(**inputs).logits_per_image.softmax(dim=0).cpu().numpy().flatten()
    sorted_idx = np.argsort(probs)[::-1]
    return [valid_results[i] for i in sorted_idx[:8]]

async def process_category_pipeline(async_client, key, data):
    print(f"DEBUG: [{key}] 카테고리 처리 시작! 쿼리: {data.get('query')}") # ⭐️ 로그 추가
    search_query = data.get("query", "aesthetic")
    clip_prompt = data.get("clip", "high quality photography")
    urls_data = []

    try:
        if key in ["product", "material"]:
            res = await async_client.get(
                f"https://api.pexels.com/v1/search?query={search_query}&per_page=12", 
                headers={"Authorization": config.PEXELS_API_KEY},
                timeout=5.0 
            )
            if res.status_code == 200:
                photos = res.json().get("photos", [])
                urls_data = [{"thumb_url": p["src"]["tiny"], "small_url": p["src"]["medium"]} for p in photos]
        else:
            res = await async_client.get(
                f"https://api.unsplash.com/search/photos?query={search_query}&per_page=12", 
                headers={"Authorization": f"Client-ID {config.UNSPLASH_KEY}"},
                timeout=5.0
            )
            if res.status_code == 200:
                results = res.json().get("results", [])
                urls_data = [{"thumb_url": r["urls"]["thumb"], "small_url": r["urls"]["small"]} for r in results]

        if not urls_data:
            return key, ["https://placehold.co/400x400/334155/ffffff.png?text=No+Image"], []

        tasks = [fetch_image(async_client, u) for u in urls_data]
        downloaded = await asyncio.gather(*tasks)
        valid = [r for r in downloaded if r is not None]

        best_results = filter_and_get_top_images(clip_prompt, valid)
        final_urls = [res["small_url"] for res in best_results]

        category_pixels = []
        for res_dict in best_results:
            try:
                img_array = np.array(res_dict["img"].resize((20, 20)))
                category_pixels.append(img_array.reshape(-1, 3))
            except: continue

        return key, final_urls, category_pixels

    except httpx.TimeoutException:
        print(f"⏱️ [{key}] 카테고리 타임아웃! (API 서버 응답 지연)")
        return key, ["https://placehold.co/400x400/ef4444/ffffff.png?text=API+Timeout"], []
        
    except httpx.RequestError as e:
        print(f"❌ [{key}] 카테고리 네트워크 통신 에러: {e}")
        return key, ["https://placehold.co/400x400/ef4444/ffffff.png?text=Network+Error"], []
        
    except Exception as e:
        print(f"⚠️ [{key}] 카테고리 알 수 없는 내부 에러: {e}")
        return key, ["https://placehold.co/400x400/334155/ffffff.png?text=System+Error"], []

def extract_palette(all_downloaded_pixels):
    """모든 카테고리에서 수집한 픽셀로 K-Means 팔레트 추출"""
    if all_downloaded_pixels:
        kmeans = KMeans(n_clusters=8, random_state=42, n_init=10).fit(np.vstack(all_downloaded_pixels))
        hex_colors = sorted(['{:02x}{:02x}{:02x}'.format(*c) for c in kmeans.cluster_centers_.astype(int)],
                            key=lambda x: int(x[0:2], 16)*0.299 + int(x[2:4], 16)*0.587 + int(x[4:6], 16)*0.114, reverse=True)
        return [f"https://placehold.co/150x150/{color}/{color}.png" for color in hex_colors]
    else:
        return ["https://placehold.co/150x150/eeeeee/eeeeee.png"]