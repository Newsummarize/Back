from flask import Flask, jsonify, request
import requests
from bs4 import BeautifulSoup
from sqlalchemy import create_engine, Column, Integer, String, Text, DateTime
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.orm import sessionmaker
from datetime import datetime
import threading

app = Flask(__name__)

# ✅ DB 연결
DATABASE_URL = "mysql+pymysql://songsungmin:password0419@news-db.cjg2aaai646f.ap-northeast-2.rds.amazonaws.com:3306/newsdb"
engine = create_engine(DATABASE_URL, echo=True)
Base = declarative_base()
Session = sessionmaker(bind=engine)

# ✅ 모델 정의
class News(Base):
    __tablename__ = 'news'
    news_id = Column(Integer, primary_key=True, autoincrement=True)
    title = Column(String(500), nullable=False)
    content = Column(Text)
    publisher = Column(String(255), nullable=False)
    published_at = Column(DateTime, nullable=False)
    url = Column(String(500), nullable=False)
    category = Column(String(255), nullable=False)
    image_url = Column(String(500), nullable=False)

Base.metadata.create_all(engine)

# ✅ 카테고리 URL
category_sections = {
    "정치": "https://news.naver.com/section/100",
    "경제": "https://news.naver.com/section/101",
    "사회": "https://news.naver.com/section/102",
    "생활/문화": "https://news.naver.com/section/103",
    "세계": "https://news.naver.com/section/104",
    "IT/과학": "https://news.naver.com/section/105",
    "all": "https://news.naver.com/"
}

headers = {'User-Agent': 'Mozilla/5.0'}

def sanitize(text):
    return text.replace('\n', ' ').replace('\r', ' ').replace('\t', ' ').strip() if text else text

def parse_datetime(text):
    try:
        return datetime.strptime(text, "%Y.%m.%d. %p %I:%M")
    except:
        return datetime.now()

def is_valid_article_link(href):
    return '/article/' in href and '/comment/' not in href and '/hotissue/' not in href

def clean_article_url(href):
    href = href.replace('/comment', '').split('?')[0]
    return href if href.startswith('http') else 'https://news.naver.com' + href

def trigger_body_crawler(id_url_list):
    try:
        print("🚀 본문 크롤러 호출 중...")
        res = requests.post(
            "http://localhost:5003/trigger",
            json={"articles": id_url_list}
        )
        print("🧠 본문 크롤링 결과:", res.json())
    except Exception as e:
        print("❌ 본문 크롤링 트리거 실패:", e)

# ✅ 뉴스 크롤러
def crawl_news(category, max_articles=10):
    section_url = category_sections[category]
    res = requests.get(section_url, headers=headers)
    soup = BeautifulSoup(res.text, 'html.parser')

    # ✅ all인 경우 메인에서 주요 기사 링크 수집
    if category == 'all':
        article_links = {
            clean_article_url(a['href']) for a in soup.find_all('a', href=True)
            if is_valid_article_link(a['href'])
        }
    else:
        article_links = {
            clean_article_url(a['href']) for a in soup.find_all('a', href=True)
            if is_valid_article_link(a['href'])
        }

    session = Session()
    id_url_list = []
    article_list = []

    for link in article_links:
        if len(article_list) >= max_articles:
            break

        try:
            article_res = requests.get(link, headers=headers)
            article_soup = BeautifulSoup(article_res.text, 'html.parser')

            title_tag = article_soup.select_one('h2.media_end_head_headline, div#title_area span')
            press_tag = article_soup.select_one('a.media_end_head_top_logo img')
            date_tag = article_soup.select_one('span.media_end_head_info_datestamp_time, span#date_text')

            og_image = article_soup.select_one('meta[property="og:image"]')
            image_url = og_image['content'] if og_image and og_image.has_attr('content') else ''
            if not image_url:
                img_tag = article_soup.select_one('figure img, #img1, .newsct_article img')
                if img_tag:
                    image_url = img_tag.get('src') or img_tag.get('data-src') or ''

            news = News(
                title=sanitize(title_tag.get_text(strip=True)) if title_tag else "제목 없음",
                publisher=sanitize(press_tag['alt']) if press_tag and 'alt' in press_tag.attrs else "N/A",
                published_at=parse_datetime(date_tag.get_text(strip=True)) if date_tag else datetime.now(),
                url=link,
                category=category,
                content=None,
                image_url=image_url
            )
            session.add(news)
            session.flush()

            article_list.append({
                "news_id": news.news_id,  
                "title": news.title,
                "url": news.url,
                "publisher": news.publisher,
                "published_at": str(news.published_at),
                "category": news.category,
                "content": news.content,
                "image_url": news.image_url
            })


            id_url_list.append({
                "news_id": news.news_id,
                "url": news.url
            })

        except Exception as e:
            print(f"❌ [{link}] 오류: {e}")
            continue

    session.commit()
    session.close()

    threading.Thread(target=trigger_body_crawler, args=(id_url_list,)).start()

    return article_list

# ✅ API 라우트
@app.route('/news', methods=['GET'])
def get_news():
    category = request.args.get('category', default='all')
    limit = int(request.args.get('limit', 10))  # default = 10 → 추천 시에는 2

    if category not in category_sections:
        return jsonify({"error": "잘못된 카테고리입니다."}), 400

    data = crawl_news(category, max_articles=limit)
    return jsonify({
        "category": category,
        "total": len(data),
        "articles": data
    })


# ✅ 실행
if __name__ == '__main__':
    print("📰 live_crawling.py 실행 중...")
    app.run(host='0.0.0.0', port=5001, debug=True)
