from flask import Flask, request, jsonify
from pytrends.request import TrendReq

app = Flask(__name__)

@app.route('/trend')
def trend():
    keyword = request.args.get('keyword', '탄소중립')
    pytrends = TrendReq(hl='ko', tz=540)
    pytrends.build_payload([keyword], timeframe='today 3-m')
    data = pytrends.interest_over_time().reset_index()
    return jsonify(data[['date', keyword]].to_dict(orient='records'))
