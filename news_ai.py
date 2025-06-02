import json, datetime
from flask import Flask, request, Response
from flask_cors import CORS

from python_modules.summarizer import summarize_text
from python_modules.vectorizer import encode_text
from python_modules.timeliner import generate_timeline

app = Flask(__name__)
CORS(app, origins=["http://localhost:5173", "https://newsummarize.com"], supports_credentials=True)

@app.route('/search/timeline', methods=['GET'])
def generateKeywordTimeline():
    try:
        keyword = request.args.get('keyword', '')
        result_articles = generate_timeline(keyword)
        return Response(
            response = json.dumps(result_articles, ensure_ascii=False),
            status=200,
            mimetype='application/json'
        )
    except Exception as e:
        return Response(
            response=json.dumps({'error': '서버 내부에 문제가 발생했습니다.'}, ensure_ascii=False, indent=4),
            status=500,
            mimetype='application/json'
        )

@app.route('/ai/subsummary', methods=['POST'])
def generateArticleSummary():
    try:
        print(f"[{datetime.datetime.now()}] 처리 시작")
        article = request.get_json().get('article')
        if not article:
            raise AttributeError("내용 없는 기사")
        content = summarize_text(article)
        if not content:
            raise Exception("요약 생성 비정상 종료")
        resp = {
            'content': content,
            'vector': encode_text(content).tolist()
        }
        print(f"[{datetime.datetime.now()}] 처리 끝")
        return Response(
                response = json.dumps(resp, ensure_ascii=False, indent=4),
                status = 200,
                mimetype='application/json'
            )
    except Exception as e:
        print(f"[Error] {e}")
        return Response(
            response=json.dumps({'error': '서버 내부에 문제가 발생했습니다.'}, ensure_ascii=False, indent=4),
            status=500,
            mimetype='application/json'
        )

@app.route('/news_ai/test', methods=['GET'])
def test():
    return Response(
        response=json.dumps({'reponse': 'ok'}, ensure_ascii=False, indent=4),
        status=200,
        mimetype='application/json'
    )

if __name__ == '__main__':
    app.run(host="0.0.0.0", port=5011, debug=True)
