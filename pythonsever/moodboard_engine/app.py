# 파이썬(FastAPI) 코드 수정본
from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
from typing import List
import httpx
import asyncio
import config
from image_services import (
    generate_queries_with_llm, 
    process_category_pipeline, 
    extract_palette
)

app = FastAPI()

# ⭐️ 수정: allow_origins에 8089 포트를 추가하세요!
app.add_middleware(
    CORSMiddleware, 
    allow_origins=["http://localhost:8089", "http://127.0.0.1:8089", "http://localhost:3000"], 
    allow_credentials=True,
    allow_methods=["*"], 
    allow_headers=["*"]
)

class MoodboardRequest(BaseModel):
    brand_intro: str
    item_type: str
    selected_tags: List[str]

# ⭐️ 수정: 프론트엔드(brandLogic.js)가 호출하는 경로와 일치시켰습니다.
@app.post("/api/ai/moodboard/generate")
async def create_moodboard(req: MoodboardRequest):
    # 1. LLM 쿼리 생성
    queries_data = generate_queries_with_llm(req.item_type, req.selected_tags, req.brand_intro)
    if not queries_data:
        raise HTTPException(status_code=500, detail="LLM failed to generate queries.")

    results = {}
    all_downloaded_pixels = [] 
    
    # 2. 8개 카테고리 동시 병렬 처리
    async with httpx.AsyncClient() as client:
        pipeline_tasks = [
            process_category_pipeline(client, key, data) 
            for key, data in queries_data.items()
        ]
        
        gathered_results = await asyncio.gather(*pipeline_tasks)
        
        for key, final_urls, category_pixels in gathered_results:
            results[key] = final_urls
            all_downloaded_pixels.extend(category_pixels)
            
    # 3. 팔레트 색상 데이터 추출
    results["palette"] = extract_palette(all_downloaded_pixels)
        
    return {"status": "success", "images": results, "queries": queries_data}