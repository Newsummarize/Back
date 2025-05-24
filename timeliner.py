import json, heapq, torch
import numpy as np
from sqlalchemy import create_engine, Column, Integer, String, Text, DateTime, desc
from sqlalchemy.dialects.mysql import LONGTEXT
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.orm import sessionmaker

from python_modules.summarizer import summarize_text
from python_modules.vectorizer import encode_text

DATABASE_URL = "mysql+pymysql://songsungmin:password0419@news-db.cjg2aaai646f.ap-northeast-2.rds.amazonaws.com:3306/newsdb"
engine = create_engine(DATABASE_URL, echo=True)
Base = declarative_base()
Session = sessionmaker(bind=engine)

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
    content_vector = Column(LONGTEXT)

def search_articles_vectorized(keyword, session, top_n=10, threshold=0.4):
    heap = []
    query_vector = encode_text(keyword)

    print(f"\n[Log] 키워드 벡터화 완료\n")
    for record in session.query(
        News.news_id, News.title, News.published_at, News.content, News.content_vector
    ).filter(
        News.content_vector.isnot(None)
    ).yield_per(100):
        try:
            article_vector = np.array(json.loads(record.content_vector), dtype=np.float32)
            similarity = compute_similarity(query_vector, article_vector)
            if similarity >= threshold:
                if len(heap) < top_n:
                    heapq.heappush(heap, (similarity, record))
                else:
                    heapq.heappushpop(heap, (similarity, record))
        except Exception as e:
            continue
    
    top_articles = [item for _, item in sorted(heap, key=lambda x: x[0], reverse=True)]
    top_articles.sort(key=lambda x: x[1].published_at)

    return top_articles

def compute_similarity(query_vector, doc_vector):
    query_tensor = torch.tensor(query_vector, dtype=torch.float32)
    doc_tensor = torch.tensor(doc_vector, dtype=torch.float32)
    return torch.nn.functional.cosine_similarity(query_tensor, doc_tensor, dim=0).item()

def generate_timeline(keyword):
    session = Session()
    try:
        print(f"[1] 타임라인 생성 시작")
        top_results = search_articles_vectorized(keyword=keyword, session=session, top_n=10, threshold=0.4)
        print(f"[2] 키워드 벡터와 유사한 벡터값 소유 기사 선별 완료: {top_results[:3]}")
        return {
            'keyword': keyword,
            'total': len(top_results),
            'events': [{
                "news_id": a[1][0],
                "title": a[1][1],
                "published_at": a[1][2].isoformat(),
                "content": a[1][3]
            } for a in top_results]
        }
    except Exception as e:
        print(f"[Log] timeliner stopped because: {e}")
        return None
    finally:
        session.close()
