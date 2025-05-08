import json
import datetime
import urllib.request
from flask import Flask, request, Response
from newspaper import Article
from sqlalchemy import create_engine, Column, Integer, String, Text, DateTime
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.orm import sessionmaker

# Flask App 설정
app = Flask(__name__)

# DB 연결 정보
DATABASE_URL = "mysql+pymysql://songsungmin:password0419@news-db.cjg2aaai646f.ap-northeast-2.rds.amazonaws.com:3306/newsdb"
engine = create_engine(DATABASE_URL, echo=True)
Base = declarative_base()
Session = sessionmaker(bind=engine)

# 뉴스 모델 정의
class News(Base):
    __tablename__ = 'news'

    news_id = Column(Integer, primary_key=True, autoincrement=True)
    title = Column(String(500), nullable=False)
    url = Column(String(500), nullable=False)
    publisher = Column(String(255), nullable=False)
    published_at = Column(DateTime, nullable=False)
    category = Column(String(255), nullable=False)
    image_url = Column(String(500), nullable=False)
    content = Column(Text)

Base.metadata.create_all(engine)

# Naver Open API 설정
naver_api_id = 'OFp3h0R6Rz6IjtoLR6HI'
naver_api_pw = 'Q2AzgNx6Pq'

# 기사 가져오기
def getArticle(keyword):
    simpleResult = []
    parsedResult = []

    searchResponse = getNaverSearch(keyword, 1, 100)
    if searchResponse and searchResponse['display'] != 0:
        for item in searchResponse['items']:
            simpleResult.append(getItemData(item))
    else:
        return parsedResult

    if simpleResult:
        setArticleInformation(simpleResult, parsedResult, keyword)

    return parsedResult

# 네이버 뉴스 검색
def getNaverSearch(srcText, start, display):
    base = "https://openapi.naver.com/v1/search/news.json"
    params = f"?query={urllib.parse.quote(srcText)}&start={start}&display={display}"
    url_link = base + params
    return json.loads(getRequestUrl(url_link)) if getRequestUrl(url_link) else None

# API 요청
def getRequestUrl(url):
    req = urllib.request.Request(url)
    req.add_header("X-Naver-Client-Id", naver_api_id)
    req.add_header("X-Naver-Client-Secret", naver_api_pw)

    try:
        response = urllib.request.urlopen(req)
        return response.read().decode('utf-8') if response.getcode() == 200 else None
    except Exception as e:
        print(">>> [Error]:", e)
        return None

# 기사 기본 데이터 추출
def getItemData(item):
    return {
        'news_id': -1,
        'title': '',
        'url': item['originallink'],
        'category': '',
        'publisher': '',
        'published_at': datetime.datetime.strptime(item['pubDate'], '%a, %d %b %Y %H:%M:%S +0900').strftime('%Y-%m-%d %H:%M:%S'),
        'image_url': '',
        'content': None
    }

# 기사 정보 세부 파싱 및 DB 저장
def setArticleInformation(before, after, keyword):
    session = Session()
    request_path = request.path
    is_recommend_mode = request_path.startswith('/api/news/recommend')

    for article in before:
        try:
            if is_recommend_mode and len(after) >= 2:
                break

            articleOriginal = Article(article['url'], language='ko')
            articleOriginal.download()
            articleOriginal.parse()

            articleTitle = articleOriginal.extractor.get_meta_content(doc=articleOriginal.clean_doc, metaname='meta[property="og:title"]')
            if not articleTitle:
                continue

            siteName = articleOriginal.extractor.get_meta_content(doc=articleOriginal.clean_doc, metaname='meta[property="og:site_name"]')
            copyright = articleOriginal.extractor.get_meta_content(doc=articleOriginal.clean_doc, metaname='meta[name="Copyright"]')
            publisher = siteName or copyright
            if not publisher:
                continue

            articleImageURL = articleOriginal.extractor.get_meta_content(doc=articleOriginal.clean_doc, metaname='meta[property="og:image"]') or 'Imageless Article'

            news = News(
                title=articleTitle,
                url=article['url'],
                category=keyword,
                publisher=publisher,
                published_at=article['published_at'],
                image_url=articleImageURL,
                content=None
            )
            session.add(news)
            session.flush()

            article['news_id'] = news.news_id
            article['title'] = articleTitle
            article['category'] = keyword
            article['publisher'] = publisher
            article['image_url'] = articleImageURL

            after.append(article)

        except Exception as e:
            print(f">>> [Error]: 스킵된 기사 | 이유: {e}")
            continue

    session.commit()
    session.close()

# ✅ 추천 뉴스 API
@app.route('/api/news/recommend', methods=['GET'])
def getRecommendedNews():
    keyword = request.args.get('keyword', '')
    if not keyword:
        return Response(
            response=json.dumps({'error': '검색어(keyword)가 필요합니다.'}),
            status=400,
            mimetype='application/json'
        )
    
    data = getArticle(keyword)
    return Response(
        response=json.dumps({
            "keyword": keyword,
            "total": len(data),
            "articles": data
        }, ensure_ascii=False),
        status=200,
        mimetype="application/json"
    )

# Flask 실행
if __name__ == '__main__':
    app.run(host="0.0.0.0", port=5009, debug=True)
