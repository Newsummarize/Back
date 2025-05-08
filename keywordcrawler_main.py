import json, datetime, urllib.request
from newspaper import Article
from sqlalchemy import create_engine, Column, Integer, String, Text, DateTime
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.orm import sessionmaker
from sqlalchemy.dialects.mysql import LONGTEXT

# DB 연결 설정
DATABASE_URL = "mysql+pymysql://songsungmin:password0419@news-db.cjg2aaai646f.ap-northeast-2.rds.amazonaws.com:3306/newsdb"
engine = create_engine(DATABASE_URL, echo=True)
Base = declarative_base()
Session = sessionmaker(bind=engine)

# 뉴스 모델
class News(Base):
    __tablename__ = 'news'
    news_id = Column(Integer, primary_key=True, autoincrement=True)
    title = Column(String(500), nullable=False)
    url = Column(String(500), nullable=False)
    publisher = Column(String(255), nullable=False)
    published_at = Column(DateTime, nullable=False)
    category = Column(String(255), nullable=False)
    image_url = Column(String(500), nullable=False)
    content = Column(Text),
    content_vector = Column(LONGTEXT)

# Naver API 설정
naver_api_id = 'OFp3h0R6Rz6IjtoLR6HI'
naver_api_pw = 'Q2AzgNx6Pq'

def getArticle(keyword):
    simpleResult = []
    parsedResult = []

    searchResponse = getNaverSearch(keyword, 1, 100)
    if searchResponse.get('display', 0) != 0:
        for item in searchResponse['items']:
            simpleResult.append(getItemData(item))
        setArticleInformation(simpleResult, parsedResult)

    return parsedResult

def getNaverSearch(srcText, start, display):
    base = "https://openapi.naver.com/v1/search/news.json"
    params = f"?query={urllib.parse.quote(srcText)}&start={start}&display={display}"
    url_link = base + params

    return json.loads(getRequestUrl(url_link) or '{}')

def getRequestUrl(url):
    req = urllib.request.Request(url)
    req.add_header("X-Naver-Client-Id", naver_api_id)
    req.add_header("X-Naver-Client-Secret", naver_api_pw)

    try:
        response = urllib.request.urlopen(req)
        return response.read().decode('utf-8') if response.getcode() == 200 else None
    except Exception as e:
        print(">>> [Error]: ", e)
        return None

def getItemData(item):
    published_at = datetime.datetime.strptime(item['pubDate'], '%a, %d %b %Y %H:%M:%S +0900')
    
    return {
        'news_id': -1,
        'title': '',
        'url': item['originallink'],
        'category': '',
        'publisher': '',
        'published_at': published_at.strftime('%Y-%m-%d %H:%M:%S'),
        'image_url': '',
        'content': None,
        'content_vector': None
    }

def setArticleInformation(before, after):
    session = Session()
    for article in before:
        try:
            if len(after) == 6:
                break
            
            parsed = Article(article['url'], language='ko')
            parsed.download()
            parsed.parse()

            title = parsed.extractor.get_meta_content(doc=parsed.clean_doc, metaname='meta[property="og:title"]')
            if not title:
                continue

            site = parsed.extractor.get_meta_content(doc=parsed.clean_doc, metaname='meta[property="og:site_name"]') or \
                   parsed.extractor.get_meta_content(doc=parsed.clean_doc, metaname='meta[name="Copyright"]')
            if not site:
                continue

            category = parsed.extractor.get_meta_content(doc=parsed.clean_doc, metaname='meta[property="article:section"]')
            if not category:
                continue

            image_url = parsed.extractor.get_meta_content(doc=parsed.clean_doc, metaname='meta[property="og:image"]') or 'Imageless Article'

            news = News(
                title=title,
                url=article['url'],
                category=category,
                publisher=site,
                published_at=article['published_at'],
                image_url=image_url,
                content=None,
                content_vector=None
            )
            session.add(news)
            session.flush()

            article.update({
                'news_id': news.news_id,
                'title': title,
                'category': category,
                'publisher': site,
                'image_url': image_url
            })
            after.append(article)
        except Exception as e:
            print(f">>> [Error]: Skipped article <{article['title']}> due to error:", e)
            continue
    session.commit()
    session.close()
