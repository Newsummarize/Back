import json, datetime, io
import urllib.request
import matplotlib.pyplot as plt
from sqlalchemy import create_engine, Column, Integer, Text, VARCHAR, DateTime, UniqueConstraint
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.orm import sessionmaker
from sqlalchemy.sql import func

DATABASE_URL = "mysql+pymysql://songsungmin:password0419@news-db.cjg2aaai646f.ap-northeast-2.rds.amazonaws.com:3306/newsdb"
engine = create_engine(DATABASE_URL, echo=True)
Base = declarative_base()
Session = sessionmaker(bind=engine)

class Keyword(Base):
    __tablename__ = 'keyword'
    keyword_id = Column(Integer, primary_key=True, autoincrement=True)
    keyword_text = Column(VARCHAR(255), unique=True)
    period = Column(VARCHAR(10), nullable=False, default='daily')
    trend_image = Column(VARCHAR(1000))
    created_at = Column(DateTime, server_default=func.now())
    updated_at = Column(DateTime, server_default=func.now(), onupdate=func.now())
    ai_summary = Column(Text)
    
    __table_args__ = (
        UniqueConstraint('keyword_text', 'period', name='_keyword_period_uc'),
    )

naver_api_id = "OFp3h0R6Rz6IjtoLR6HI"
naver_api_secret = "Q2AzgNx6Pq"

def getOrCreateKeywordTrendImage(keyword, period):
    session = Session()
    keyword_entry = session.query(Keyword).filter_by(
        keyword_text=keyword,
        period=period
    ).first()

    now = datetime.datetime.now()
    expiry_days = {
        'daily': 1,
        'weekly': 7,
        'monthly': 30
    }

    image_filename = f"/home/ubuntu/trend_images/{keyword}_{period}.png"
    if keyword_entry and (keyword_entry.trend_image == image_filename):
        delta = now - keyword_entry.updated_at
        if delta.days <= expiry_days[period]:
            try:
                with open(keyword_entry.trend_image, 'rb') as f:
                    img_io = io.BytesIO(f.read())
                    img_io.seek(0)
                    session.close()
                    return img_io
            except FileNotFoundError:
                pass

    json_string = getNaverDataLab(getKeywordTrendRequestBody(keyword, period))
    if not json_string:
        session.close()
        return None

    img_io, file_path = generateVisualTrendImage(json_string, image_filename)

    if keyword_entry:
        keyword_entry.trend_image = file_path
        keyword_entry.updated_at = now
    else:
        keyword_entry = Keyword(
            keyword_text=keyword,
            period=period,
            trend_image=file_path,
            created_at=now,
            updated_at=now,
            ai_summary=None
        )
        session.add(keyword_entry)

    session.commit()
    session.close()

    return img_io

def getNumercialTrendData(keyword, period):
    json_string = getNaverDataLab(getKeywordTrendRequestBody(keyword, period))
    if not json_string:
        data = []
    else:
        decoded = json.loads(json_string)
        data = decoded['results'][0]['data']

    result_dict = {
        "keyword": keyword,
        "period": period,
        "results": data
    }

    return result_dict

def getKeywordTrendRequestBody(keyword, period):
    match period:
        case "daily":
            period_day = 7
            timeUnit = "date"
        case "weekly":
            period_day = 35
            timeUnit = "week"
        case "monthly":
            period_day = 365
            timeUnit = "month"
        case _:
            raise ValueError("지원하지 않는 보기 유형입니다: " + period)
    
    startDate = (datetime.datetime.today() - datetime.timedelta(days=period_day)).strftime("%Y-%m-%d")
    endDate = datetime.datetime.today().strftime("%Y-%m-%d")

    body_dict = {
        "startDate": startDate,
        "endDate": endDate,
        "timeUnit": timeUnit,
        "keywordGroups": [
            {
                "groupName": keyword,
                "keywords": [keyword]
            }
        ]
    }

    return body_dict

def getNaverDataLab(body):
    req = urllib.request.Request("https://openapi.naver.com/v1/datalab/search")
    req.add_header("X-Naver-Client-Id", naver_api_id)
    req.add_header("X-Naver-Client-Secret", naver_api_secret)
    req.add_header("Content-Type","application/json")
    
    try:
        response = urllib.request.urlopen(req, data=json.dumps(body).encode("utf-8"))
        return response.read().decode('utf-8') if response.getcode() == 200 else None
    except Exception as e:
        print(">>> [Error]: ", e)
        return None

def generateVisualTrendImage(json_string, file_path):
    decoded = json.loads(json_string)
    result = decoded['results'][0]
    dates = [d['period'] for d in result['data']]
    values = [d['ratio'] for d in result['data']]

    if not dates or not values:
        return None, None

    plt.rc('font', family='NanumGothic')
    plt.plot(dates, values, marker='o', color='green')
    plt.title(result['title'] + "의 트렌드 데이터")
    plt.xlabel("기간")
    plt.ylabel("상대 검색 비율")
    plt.xticks(rotation=45)
    plt.grid(True)
    plt.tight_layout()

    plt.savefig(file_path, format='png')

    img_io = io.BytesIO()
    plt.savefig(img_io, format='png')
    plt.close()
    img_io.seek(0)

    return img_io, file_path
