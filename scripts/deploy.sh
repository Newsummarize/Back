#!/bin/bash

echo "🚀 [DEPLOY] 배포 스크립트 시작"

APP_DIR=/home/ec2-user/newsummarize
JAR_FILE=$(ls $APP_DIR/build/libs/*.jar | head -n 1)

echo "🛑 [DEPLOY] 기존 애플리케이션 종료 중..."
pkill -f 'java -jar' || true

echo "✅ [DEPLOY] 새 JAR 실행 중: $JAR_FILE"
nohup java -jar $JAR_FILE > $APP_DIR/nohup.out 2>&1 &

echo "🎉 [DEPLOY] 배포 완료!"
