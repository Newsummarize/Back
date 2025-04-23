import urllib.request
import datetime
import json
from newspaper import Article
from flask import Flask, request, Response
from sqlalchemy import create_engine, Column, Integer, String, Text
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.orm import sessionmaker

# Flask App 설정
app = Flask(__name__)

# DB 연결 정보
DATABASE_URL = "mysql+pymysql://songsungmin:password0419@news-db.cjg2aaai646f.ap-northeast-2.rds.amazonaws.com:3306/newsdb"
# DATABASE_URL = "mysql+pymysql://admin:(###secret###)@crawlertestdb.chai8smy2qkx.ap-northeast-2.rds.amazonaws.com:3306/crawlerTestDB" # Test DB URL

engine = create_engine(DATABASE_URL, echo=True)
Base = declarative_base()
Base.metadata.create_all(engine)
Session = sessionmaker(bind=engine)

# 뉴스 모델 정의
class News(Base):
    __tablename__ = 'news'

    id = Column(Integer, primary_key=True, autoincrement=True)
    title = Column(String(255))
    url = Column(String(255))
    publisher = Column(String(100))
    published_date = Column(String(100))
    category = Column(String(50))
    content = Column(Text)  # 요약문 자리

# 네이버 검색 API 요청 id, pw
naver_api_id = 'OFp3h0R6Rz6IjtoLR6HI'
naver_api_pw = 'Q2AzgNx6Pq'

# 기사 정보 추출
def getArticle(keyword):
    simpleResult = []
    parsedResult = []

    # 처리 속도에 따라서 Search량 조절
    searchResponse = getNaverSearch(keyword, 1, 100)
    if (searchResponse != None) and (searchResponse['display'] != 0):
        for item in searchResponse['items']:
            simpleResult.append(getItemData(item))
    else:
        return parsedResult
    
    if (simpleResult != None) and (len(simpleResult) > 0):
        setArticleInformation(simpleResult, parsedResult)

    return parsedResult

# 네이버 검색 API 호출, 결과 반환
def getNaverSearch(srcText, start, display):
    base = "https://openapi.naver.com/v1/search/news.json"
    params = "?query=%s&start=%s&display=%s" % (urllib.parse.quote(srcText), start, display)
    url_link = base + params
    decodedResponse = getRequestUrl(url_link)

    if (decodedResponse == None):
        return None
    else:
        return json.loads(decodedResponse)

# url request 실행 및 응답 반환
def getRequestUrl(url):
    req = urllib.request.Request(url)
    req.add_header("X-Naver-Client-Id", naver_api_id)
    req.add_header("X-Naver-Client-Secret", naver_api_pw)

    try:
        response = urllib.request.urlopen(req)
        if response.getcode() == 200:
            return response.read().decode('utf-8')
        else:
            return None
    except Exception as e:
        print(">>> [Error]: ", e)
        return None

# json에 담긴 기사 데이터 추출
def getItemData(item):
    orginal_link = item['originallink']
    published_date = datetime.datetime.strptime(item['pubDate'], '%a, %d %b %Y %H:%M:%S +0900')
    published_date = published_date.strftime('%Y-%m-%d %H:%M:%S')

    result = {'id': -1,
              'title': '',
              'url': orginal_link,
              'category': '',
              'publisher': '',
              'published_date': published_date,
              'content': None}
    
    return result

# 기사 파싱 및 반환, 추출된 기사 정보의 DB 데이터 삽입을 위한 처리 담당
def setArticleInformation(before, after):
    session = Session()

    for article in before:
        try:
            if len(after) == 6:
                break

            articleOriginal = Article(article['url'], language='ko')
            articleOriginal.download()
            articleOriginal.parse()

            articleTitle = articleOriginal.extractor.get_meta_content(doc=articleOriginal.clean_doc, metaname='meta[property="og:title"]')
            if (articleTitle == None) or (len(articleTitle) <= 0):
                continue

            siteName = articleOriginal.extractor.get_meta_content(doc=articleOriginal.clean_doc, metaname='meta[property="og:site_name"]')
            copyright = articleOriginal.extractor.get_meta_content(doc=articleOriginal.clean_doc, metaname='meta[name="Copyright"]')
            if (siteName != None) and (len(siteName) > 0):
                publisher = siteName
            elif (copyright != None) and (len(copyright) > 0):
                publisher = copyright
            else:
                continue

            # 만약 enum 에 맞추어 카테고리 반환값을 특정해야할 경우 이 부분 코드를 고칠 것
            articleCategory = articleOriginal.extractor.get_meta_content(doc=articleOriginal.clean_doc, metaname='meta[property="article:section"]')
            if (articleCategory == None) or (len(articleCategory) <= 0):
                continue

            news = News(
                title=articleTitle,
                url=article['url'],
                publisher=publisher,
                published_date=article['published_date'],
                category=articleCategory,
                content=None
            )
            session.add(news)
            session.flush()

            article['id'] = news.id
            article['title'] = articleTitle
            article['publisher'] = publisher
            article['category'] = articleCategory

            after.append(article)

        except Exception as e:
            print(">>> [Error]: Because of some exception happend, application skipped this article: ", article['title'])
            continue
    
    session.commit()
    session.close()

# API 라우트
@app.route('/search', methods=['GET'])
def getKeywordNews():
    keyword = request.args.get('query', '')
    if not keyword:
        return Response(
            response=json.dumps({'error': '검색어(keyword)가 필요합니다.'}),
            status=400,
            mimetype='application/json'
        )
    
    data = getArticle(keyword)
    return Response(
        response = json.dumps({
            "keyword": keyword,
            "total": len(data),
            "articles": data
        }, ensure_ascii=False),
        status = 200,
        mimetype="application/json"
    )

# Flask 실행, 포트 설정 요청해야 함, 실 배포 시 debug=False 처리
if __name__ == '__main__':
    app.run(host="0.0.0.0", port=5006, debug=True)