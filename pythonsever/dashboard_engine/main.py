"""
main.py
FastAPI 서버 - BrandBeacon AI API
Spring Boot가 이 서버를 HTTP로 호출

엔드포인트:
  GET  /health              → 서버 상태 확인
  POST /api/brand/vector    → 브랜드 벡터 생성 (brand_ai_engine.py)
  POST /api/brand/analyze   → 브랜드 분석 + 인사이트 (brand_dashboard.py)

실행 방법:
  uvicorn main:app --host 0.0.0.0 --port 8000 --reload
"""

from fastapi import FastAPI, HTTPException
from pydantic import BaseModel, Field
from typing import List
from dotenv import load_dotenv

from brand_ai_engine import build_user_brand_vector
from brand_dashboard import analyze_brand

load_dotenv()

app = FastAPI(
    title="BrandBeacon AI API",
    description="CLIP 기반 브랜드 벡터 생성 및 분석 서버",
    version="1.0.0"
)


# =====================================================
# Request / Response 모델
# Spring Boot에서 보내는 JSON 구조와 일치해야 함
# =====================================================

class BrandVectorRequest(BaseModel):
    """POST /api/brand/vector 요청 구조"""
    project_id: str = Field(..., description="Spring Boot에서 생성한 프로젝트 ID")
    user_text: str = Field(..., description="사용자가 입력한 브랜드 설명 원문")
    selected_tags: List[str] = Field(
        ...,
        min_length=1,
        max_length=4,
        description="선택한 태그 목록 (1~4개)"
    )
    image_urls: List[str] = Field(
        ...,
        min_length=8,
        max_length=32,
        description="선택한 이미지 URL 목록 (8~32개)"
    )


class BrandVectorResponse(BaseModel):
    """POST /api/brand/vector 응답 구조"""
    project_id: str
    brand_vector: List[float]    # 512차원 벡터 (Spring Boot가 /analyze에 그대로 전달)
    brand_profile: dict          # GPT 생성 브랜드 프로필
    brand_schema: dict           # 브랜드 스키마 전체
    image_alignments: List[dict] # 이미지별 텍스트 유사도


class BrandAnalyzeRequest(BaseModel):
    """POST /api/brand/analyze 요청 구조"""
    project_id: str = Field(..., description="프로젝트 ID")
    brand_vector: List[float] = Field(
        ...,
        description="/api/brand/vector 응답의 brand_vector 그대로 전달"
    )
    brand_profile: dict = Field(
        ...,
        description="/api/brand/vector 응답의 brand_profile 그대로 전달"
    )
    user_text: str = Field(..., description="사용자 원문 (인사이트 LLM에 전달)")
    selected_tags: List[str] = Field(..., description="선택한 태그 목록")
    image_alignments: List[dict] = Field(
        ...,
        description="/api/brand/vector 응답의 image_alignments 그대로 전달"
    )


# =====================================================
# 엔드포인트
# =====================================================

@app.get("/health")
async def health_check():
    """서버 상태 확인"""
    return {"status": "ok"}


@app.post("/api/brand/vector", response_model=BrandVectorResponse)
async def create_brand_vector(request: BrandVectorRequest):
    """
    사용자 입력 → 브랜드 벡터 생성
    오류 코드:
    - 400: 이미지 장수(8~12) 또는 태그 개수(1~4) 범위 오류
    - 500: CLIP/GPT 처리 중 오류
    """
    try:
        result = build_user_brand_vector(
            user_text=request.user_text,
            image_urls=request.image_urls,
            user_selected_tags=request.selected_tags,
        )
        return BrandVectorResponse(
            project_id=request.project_id,
            brand_vector=result["brand_vector"].tolist(),
            brand_profile=result["brand_profile"],
            brand_schema=result["brand_schema"],
            image_alignments=result["image_alignments"],
        )
    except ValueError as e:
        # 이미지 장수 / 태그 개수 범위 오류
        raise HTTPException(status_code=400, detail=str(e))
    except Exception as e:
        raise HTTPException(
            status_code=500,
            detail=f"벡터 생성 중 오류가 발생했습니다: {str(e)}"
        )


@app.post("/api/brand/analyze")
async def analyze_brand_endpoint(request: BrandAnalyzeRequest):
    """
    브랜드 벡터 → 포지셔닝 분석 + LLM 인사이트 생성
    /api/brand/vector 응답을 받은 후 호출합니다.
    오류 코드:
    - 500: 분석/LLM 처리 중 오류
    """
    try:
        result = analyze_brand(
            project_id=request.project_id,
            brand_vector=request.brand_vector,
            brand_profile=request.brand_profile,
            user_text=request.user_text,
            selected_tags=request.selected_tags,
            image_alignments=request.image_alignments,
        )
        return result
    except Exception as e:
        raise HTTPException(
            status_code=500,
            detail=f"분석 중 오류가 발생했습니다: {str(e)}"
        )
