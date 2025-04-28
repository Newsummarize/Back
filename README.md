# 📰 Newsummarize - AI 기반 뉴스 요약 & 추천 시스템

---

## 🔷 시스템 개요

- 실시간 네이버 뉴스 크롤링 및 키워드 기반 뉴스 수집
- 사용자가 **카테고리 기반** 또는 **키워드 기반**으로 뉴스 요청 가능
- 기사 메타데이터는 AWS RDS(MySQL)에 저장
- 본문 크롤링은 **비동기 방식**으로 별도 서버에서 처리
- 전체 시스템은 **AWS EC2 인스턴스** 상에서 운영
- 모든 API는 **Flask 기반 REST API 구조**

---

## 🔷 아키텍처 구성

| 구성 요소 | 설명 |
|-----------|------|
| 🧍 사용자 | `/news?category=정치`, `/search?keyword=AI` 등 요청 |
| 🌐 Flask 서버 1 (`live_crawling.py`, port `5001`) | 실시간 뉴스 크롤링 (카테고리 기반) + 메타데이터 저장 |
| 🧠 Flask 서버 2 (`news_body_crawler.py`, port `5003`) | 비동기 본문 크롤링 수행 (비동기 트리거 `/trigger`) |
| 🔍 Flask 서버 3 (`keywordcrawler.py`, port `5006`) | 키워드 기반 뉴스 검색, 키워드 검색량 데이터 제공 (네이버 Open API 활용) |
| 🔍 Flask 서버 4 (`search_news_server.py`, port `5009`) | 키워드 기반 추천 뉴스 제공 (`/search`, `/recommend` 등) |
| 🗃️ AWS RDS (MySQL) | 크롤링된 뉴스 메타데이터 저장 (title, url, publisher, content 등) |
| ☁️ AWS EC2 | Flask 서버들이 실행되는 인스턴스 환경 |
| 🔄 비동기 처리 | 본문 크롤링은 별도 쓰레드로 실행되어 응답 속도 최적화 |

---

## 🔷 데이터 흐름 (카테고리 기반)

```yaml
📱 사용자
   │
   ▼
🌐 /main 요청 (EC2 서버, Flask 서버: port 5001)
   └── live_crawling.py
         ├─ Naver 뉴스 섹션 페이지에서 기사 목록 크롤링
         ├─ MySQL(RDS)에 기사 메타데이터 저장
         └─ [Flask API] /news 엔드포인트 (카테고리별 크롤링)
             |
             ▼
         🧠 EC2 서버 (Flask 서버: port 5003)
             └── news_body_crawler.py
                   ├─ 기사 URL 기반 본문 크롤링
                   ├─ 메모리(article_contents) 또는 DB에 저장
                   └─ [Flask API] /trigger 엔드포인트
```
## 🔷 데이터 흐름 (키워드 기반)

```yaml
📱 사용자
   │
   ├─ 🔍 /search?keyword=AI         (Flask: 5006)
   │     └── 네이버 뉴스 검색 API 호출
   │     └── 최대 6개의 뉴스 수집 및 본문 파싱
   │     └── 기사 메타데이터 저장 (RDS)
   │     └── 뉴스 6개 응답
   │
   └─ 🎯 /recommend?keyword=AI      (Flask: 5009)
         └── 네이버 뉴스 검색 API 호출
         └── 가장 관련성 높은 뉴스 1개만 파싱
         └── 기사 메타데이터 저장 (RDS)
         └── 뉴스 1개만 응답 (추천용)
```

---

### 🔄 전체 통신 흐름 요약

1. 사용자가 `/news?category=정치` 또는 `/search?keywrod=AI` 요청
2. Flask 서버 (`5001` 또는 `5006` 또는 `5009`)가 네이버 뉴스 수집
3. 크롤링한 뉴스 메타데이터를 AWS RDS(MySQL)에 저장
4. Flask(`5001`)가 Flask(`5003`)에 `/trigger` 요청 전송
5. Flask(`5003`)이 해당 URL 본문을 크롤링하여 메모리에 저장

---

### 🔷 기술 스택

| 항목         | 사용 기술                           |
|--------------|------------------------------------ |
| 웹 서버      | Python Flask                        |
| 비동기 처리  | Python Thread (본문 크롤러)         |
| 크롤링       | newspaper3k, urllib, BeautifulSoup  |
| 데이터 저장  | AWS RDS (MySQL)                     |
| API 구조     | REST API 기반, 비동기 본문 처리 (Thread 사용) |
| 인프라       | AWS EC2 (Ubuntu)                    |
| 인증         | JWT (Spring Boot에서 처리)          |

---

### 📦 Flask 서버 포트 및 역할

| 포트 번호  | 역할                       | 실행 파일명                | 설명                                    |
|------------|----------------------------|----------------------------|-----------------------------------------|
| `5001`     | 카테고리 뉴스 수집         | `live_crawling.py`         | `/news?category=` 요청 처리             |
| `5003`     | 본문 수집 비동기 서버      | `news_body_crawler.py`     | `/trigger` 본문 크롤링 비동기 실행      |
| `5006`     | 키워드 연관 뉴스 수집      | `keywordcralwer.py`        | `/search?keyword=` 요청 처리            |
| `5009`     | 추천 뉴스 수집             | `recommend_add_crawler.py` | `/recommend?keyword=` 요청 처리         |
