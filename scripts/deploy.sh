#!/bin/bash

APP_DIR=/home/ec2-user/newsummarize
JAR_FILE=$APP_DIR/newsummarize.jar

echo "🛑 [DEPLOY] 기존 애플리케이션 종료 중..."
pkill -f 'newsummarize.jar' || true

echo "✅ [DEPLOY] 새 JAR 실행 중: $JAR_FILE"
nohup java -jar $JAR_FILE > $APP_DIR/nohup.out 2>&1 &
