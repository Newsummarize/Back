import json
from flask_cors import CORS
from flask import Flask, request, Response

from python_modules.keywordcrawler_main import getArticle
from python_modules.keywordcrawler_trend import getOrCreateKeywordTrendImage, getNumercialTrendData

# Flask App 설정
app = Flask(__name__)
CORS(app, origins=["http://localhost:5173", "https://newsummarize.com"], supports_credentials=True)  # CORS 설정

# API 라우트
@app.route('/search', methods=['GET'])
def getKeywordNews():
    try:
        keyword = request.args.get('keyword', '')
        if not keyword:
            return Response(
                response=json.dumps({'error': '검색어(keyword)가 필요합니다.'}, ensure_ascii=False, indent=4),
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
    except Exception as e:
        return Response(
            response=json.dumps({'error': '서버 내부에 문제가 발생했습니다.'}, ensure_ascii=False, indent=4),
            status=500,
            mimetype='application/json'
        )

@app.route('/search/analytics', methods=['GET'])
def getAnalyticData():
    try:
        keyword = request.args.get('keyword', '')
        if not keyword:
            return Response(
                response=json.dumps({'error': '검색어(keyword)가 필요합니다.'}, ensure_ascii=False, indent=4),
                status=400,
                mimetype='application/json'
            )
        
        valid_periods = {'daily', 'weekly', 'monthly'}
        period = request.args.get('period')
        if not period:
            return Response(
                response=json.dumps({'error': '기간(period) 값이 필요합니다.'}, ensure_ascii=False, indent=4),
                status=400,
                mimetype='application/json'
            )
        elif period not in valid_periods:
            return Response(
                response=json.dumps({'error': f"기간(period)은 다음 중 하나여야 합니다: {', '.join(valid_periods)}."}, ensure_ascii=False, indent=4),
                status=400,
                mimetype='application/json'
            )
        
        data = getOrCreateKeywordTrendImage(keyword, period)
        if not data:
            return Response(
                response = json.dumps({
                    "keyword": keyword,
                    "description": f"키워드 \"{keyword}\"에 대한 검색 결과가 없습니다."
                }, ensure_ascii=False),
                status = 200,
                mimetype='application/json'
            )
        else:
            return Response(
                response = data.getvalue(),
                status = 200,
                mimetype="image/png"
            )
    except Exception as e:
        return Response(
            response=json.dumps({'error': '서버 내부에 문제가 발생했습니다.'}, ensure_ascii=False, indent=4),
            status=500,
            mimetype='application/json'
        )

@app.route('/search/analytics_num', methods=['GET'])
def getNumericalAnalyticData():
    try:
        keyword = request.args.get('keyword')
        period = request.args.get('period')
        data = getNumercialTrendData(keyword, period)
        if not data['results']:
            return Response(
                response = json.dumps({
                    "keyword": keyword,
                    "description": f"키워드 \"{keyword}\"에 대한 검색 결과가 없습니다."
                }, ensure_ascii=False),
                status = 200,
                mimetype='application/json'
            )
        else:
            return Response(
                response = json.dumps(data, ensure_ascii=False, indent=4),
                status = 200,
                mimetype="application/json"
            )
    except Exception as e:
        print(e)
        return Response(
            response=json.dumps({'error': '서버 내부에 문제가 발생했습니다.'}, ensure_ascii=False, indent=4),
            status=500,
            mimetype='application/json'
        )

# Flask 실행
if __name__ == '__main__':
    app.run(host="0.0.0.0", port=5006, debug=True)
