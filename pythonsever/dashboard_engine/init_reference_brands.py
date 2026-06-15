import sys
import os
import mysql.connector
from dotenv import load_dotenv

load_dotenv(dotenv_path=os.path.join(os.path.dirname(os.path.abspath(__file__)), '..', '.env'))

sys.path.append(os.path.dirname(os.path.abspath(__file__)))

from brand_dashboard import df_meta

conn = mysql.connector.connect(
    host=os.getenv("DB_HOST"),
    port=int(os.getenv("DB_PORT", "3306")),
    user=os.getenv("DB_USER"),
    password=os.getenv("DB_PASSWORD"),
    database=os.getenv("DB_NAME")
)

cursor = conn.cursor()

cursor.execute("DELETE FROM REFERENCE_BRAND")

for _, row in df_meta.iterrows():
    cursor.execute(
        "INSERT INTO REFERENCE_BRAND (BRAND_NAME, CATEGORY, BRAND_X, BRAND_Y) VALUES (%s, %s, %s, %s)",
        (row["brand_name"], row["category"], float(row["mds_d1"]), float(row["mds_d2"]))
    )

conn.commit()
cursor.close()
conn.close()
print(f"{len(df_meta)}개 브랜드 삽입 완료")
