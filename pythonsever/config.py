# config.py
import os
from dotenv import load_dotenv

# .env 파일을 로드합니다.
load_dotenv()

# 환경 변수에서 가져옵니다.
UNSPLASH_KEY = os.getenv("UNSPLASH_KEY")
PEXELS_API_KEY = os.getenv("PEXELS_API_KEY")
OPENAI_API_KEY = os.getenv("OPENAI_API_KEY")

FRONTEND_URL = os.getenv("FRONTEND_URL", "http://localhost:8089")
SPRINGBOOT_URL = os.getenv("SPRINGBOOT_URL", "http://localhost:8089")