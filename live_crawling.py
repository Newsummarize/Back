from flask import Flask, jsonify, request
import requests
from bs4 import BeautifulSoup
from flask_cors import CORS
from sqlalchemy import create_engine, Column, Integer, String, Text, DateTime
from sqlalchemy.orm import declarative_base, sessionmaker
from datetime import datetime
from urllib.parse import unquote
from concurrent.futures import ThreadPoolExecutor, as_completed


# Flask 앱 생성
app = Flask(__name__)
CORS(app, origins=["http://localhost:5173", "https://newsummarize.com"], supports_credentials=True)

# ✅ DB 연결
DATABASE_URL = "mysql+pymysql://songsungmin:password0419@news-db.cjg2aaai646f.ap-northeast-2.rds.amazonaws.com:3306/newsdb"
engine = create_engine(DATABASE_URL, echo=True)
Base = declarative_base()
Session = sessionmaker(bind=engine)

# ✅ 뉴스 모델 정의
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

# ✅ 유틸 함수들
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

# ✅ 뉴스 크롤링 함수
def crawl_news(category, max_articles=10):
    section_url = category_sections.get(category)
    if not section_url:
        return []

    res = requests.get(section_url, headers=headers)
    soup = BeautifulSoup(res.text, 'html.parser')

    article_links = list({
        clean_article_url(a['href']) for a in soup.find_all('a', href=True)
        if is_valid_article_link(a['href'])
    })

    # 최대 max_articles만 추림
    article_links = article_links[:max_articles]

    session = Session()
    article_list = []

    with ThreadPoolExecutor(max_workers=20) as executor:
        futures = {executor.submit(fetch_article_data, link, category): link for link in article_links}
        for future in as_completed(futures):
            data = future.result()
            if data:
                try:
                    news = News(**data)
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
                except Exception as e:
                    print(f"❌ DB 저장 실패 [{data.get('title', '무제')}] : {e}")
                    continue

    session.commit()
    session.close()
    return article_list

# ✅ API 엔드포인트: /news
@app.route('/news', methods=['GET'])
def get_news():
    raw_category = request.args.get('category', default='all')  
    category = unquote(raw_category)
    limit = int(request.args.get('limit', 10))

    if category not in category_sections:
        return jsonify({"error": "잘못된 카테고리입니다."}), 400

    data = crawl_news(category, max_articles=limit)
    return jsonify({
        "category": category,
        "total": len(data),
        "articles": data
    })
    
# ✅ 병렬 크롤링 함수
def fetch_article_data(link, category):
    try:
        article_res = requests.get(link, headers=headers, timeout=3)
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

        content_tag = article_soup.select_one('div#newsct_article') or article_soup.select_one('div#dic_area')
        content_text = sanitize(content_tag.get_text(separator=' ', strip=True)) if content_tag else ""

        return {
            "title": sanitize(title_tag.get_text(strip=True)) if title_tag else "제목 없음",
            "publisher": sanitize(press_tag['alt']) if press_tag and 'alt' in press_tag.attrs else "N/A",
            "published_at": parse_datetime(date_tag.get_text(strip=True)) if date_tag else datetime.now(),
            "url": link,
            "category": category,
            "content": content_text,
            "image_url": image_url
        }
    except Exception as e:
        print(f"❌ [{link}] 크롤링 실패: {e}")
        return None


# ✅ API 엔드포인트: /quick-news (본문 생략 버전)
@app.route('/quick-news', methods=['GET'])
def get_quick_news():
    category = unquote(request.args.get('category'))
    limit = int(request.args.get('limit', 1))

    section_url = category_sections.get(category)
    if not section_url:
        return jsonify({"error": "잘못된 카테고리입니다."}), 400

    res = requests.get(section_url, headers=headers)
    soup = BeautifulSoup(res.text, 'html.parser')

    article_links = {
        clean_article_url(a['href']) for a in soup.find_all('a', href=True)
        if is_valid_article_link(a['href'])
    }

    result = []

    for link in article_links:
        if len(result) >= limit:
            break
        try:
            article_res = requests.get(link, headers=headers, timeout=3)
            article_soup = BeautifulSoup(article_res.text, 'html.parser')

            title_tag = article_soup.select_one('h2.media_end_head_headline, div#title_area span')
            press_tag = article_soup.select_one('a.media_end_head_top_logo img')
            date_tag = article_soup.select_one('span.media_end_head_info_datestamp_time, span#date_text')

            og_image = article_soup.select_one('meta[property="og:image"]')
            image_url = og_image['content'] if og_image and og_image.has_attr('content') else "https://via.placeholder.com/300x200?text=No+Image"

            result.append({
                "news_id": None,
                "title": sanitize(title_tag.get_text(strip=True)) if title_tag else "제목 없음",
                "publisher": sanitize(press_tag['alt']) if press_tag and 'alt' in press_tag.attrs else "N/A",
                "published_at": str(parse_datetime(date_tag.get_text(strip=True))) if date_tag else str(datetime.now()),
                "url": link,
                "category": category,
                "image_url": image_url,
                "content": None
            })
        except Exception as e:
            print(f"❌ 빠른 크롤링 실패 [{link}]: {e}")
            continue

    return jsonify({
        "category": category,
        "total": len(result),
        "articles": result
    })

# ✅ 서버 실행
if __name__ == '__main__':
    print("📰 live_crawling.py 실행 중...")
    app.run(host='0.0.0.0', port=5001, debug=True)
