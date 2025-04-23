# 📰 Newsummarize - AI 기반 뉴스 요약 & 추천 시스템

---

## 🔷 시스템 개요

- 실시간 네이버 뉴스 크롤링 및 키워드 기반 뉴스 수집
- 사용자가 카테고리 또는 검색어 기반으로 뉴스 요청 가능
- 기사 메타데이터는 AWS RDS(MySQL)에 저장
- 본문 크롤링은 비동기 Flask 서버에서 처리
- 전체 시스템은 AWS EC2 인스턴스 상에서 운영
- 백엔드는 Flask 기반 REST API 구조

---

## 🔷 아키텍처 구성

| 구성 요소 | 설명 |
|-----------|------|
| 🧍 사용자 | `/news?category=정치`, `/search?query=AI` 등 요청 |
| 🌐 Flask 서버 1 (`live_crawling.py`, port `5001`) | 카테고리 기반 실시간 뉴스 크롤링 및 메타데이터 저장 |
| 🧠 Flask 서버 2 (`news_body_crawler.py`, port `5003`) | 비동기 본문 크롤링 처리 (요청 `/trigger`) |
| 🔍 Flask 서버 3 (`/search` API, port `5006`) | 키워드 기반 뉴스 검색 (네이버 API 사용) |
| 🗃️ AWS RDS (MySQL) | 기사 메타데이터 저장 (title, url, publisher, content 등) |
| ☁️ AWS EC2 | Flask 서버들이 실행되는 인스턴스 환경 |
| 🔄 비동기 처리 | 본문은 별도 쓰레드에서 처리, 응답 성능 최적화 |

---

## 🔷 데이터 흐름 (카테고리 기반)

```yaml
📱 사용자
   │
   ▼
🌐 EC2 (Flask: 5001)
   └── live_crawling.py
         ├─ 네이버 뉴스 목록 크롤링
         ├─ 기사 메타데이터를 RDS에 저장
         └─ /news 요청 처리
             │
             ▼
         🧠 Flask 서버 (5003)
             └── news_body_crawler.py
                   ├─ URL 기반 본문 크롤링
                   └─ 메모리 or DB 저장
