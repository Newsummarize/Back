from flask import Flask, request, jsonify
import requests
from bs4 import BeautifulSoup
import pymysql
import time

app = Flask(__name__)

# ✅ DB 설정 (RDS 접속 정보)
DB_CONFIG = {
    'host': 'news-db.cjg2aaai646f.ap-northeast-2.rds.amazonaws.com',
    'user': 'songsungmin',
    'password': 'password0419',
    'database': 'newsdb',
    'port': 3306
}

headers = {
    'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 '
                  '(KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36'
}

article_contents = {}
failed_articles = {}

# ✅ 본문 크롤링 함수
def crawl_articles_by_list(id_url_list):
    conn = pymysql.connect(**DB_CONFIG)
    cursor = conn.cursor()
    result = []

    for item in id_url_list:
        news_id = item.get("news_id")  # ✅ 수정된 컬럼명
        url = item.get("url")

        if not news_id or not url:
            continue

        print(f"\n🌐 본문 요청 중 (news_id: {news_id}): {url}")

        for attempt in range(3):
            try:
                res = requests.get(url, headers=headers, timeout=10)
                res.raise_for_status()
                soup = BeautifulSoup(res.text, 'html.parser')
                content_tag = soup.select_one('#dic_area')
                content = content_tag.get_text(strip=True) if content_tag else '[본문 없음]'

                # ✅ DB에 본문 저장
                cursor.execute(
                    "UPDATE news SET content = %s WHERE news_id = %s",
                    (content, news_id)
                )
                article_contents[news_id] = {
                    "news_id": news_id,
                    "url": url,
                    "content": content
                }
                print(f"✅ 본문 저장 완료 - news_id: {news_id}")
                print(f"🧠 본문 미리보기: {content[:100]}...")
                result.append(news_id)
                break

            except Exception as e:
                print(f"⚠️ 재시도 {attempt + 1} 실패 - news_id: {news_id} | {e}")
                time.sleep(2)

        else:
            print(f"❌ 최종 실패 - news_id: {news_id}")
            failed_articles[news_id] = url

        time.sleep(2)

    conn.commit()
    conn.close()
    return result

# ✅ POST 요청 처리
@app.route('/trigger', methods=['POST'])
def trigger():
    data = request.get_json()
    id_url_list = data.get("articles", [])  # 예: [{ "news_id": 12, "url": "..." }]
    updated_ids = crawl_articles_by_list(id_url_list)

    return jsonify({
        "message": "본문 크롤링 완료",
        "count": len(updated_ids),
        "ids": updated_ids,
        "fail_count": len(failed_articles),
        "failed_ids": list(failed_articles.keys())
    })

# ✅ 상태 확인용
@app.route('/')
def status():
    return jsonify({
        "message": "본문 크롤러 서버 실행 중",
        "stored_news_ids": list(article_contents.keys()),
        "fail_count": len(failed_articles)
    })

# ✅ 실행
if __name__ == '__main__':
    print("🧠 본문 크롤러 서버 실행 중 (5003 포트)...")
    app.run(host='0.0.0.0', port=5003, debug=True)
