#!/bin/bash

echo "🚀 [DEPLOY] 배포 스크립트 시작"

APP_DIR=/home/ubuntu
JAR_FILE=$(ls $APP_DIR/*.jar | head -n 1)

if [ -z "$JAR_FILE" ]; then
  echo "❌ [DEPLOY] JAR 파일을 찾을 수 없습니다. 종료합니다." | tee -a $APP_DIR/deploy.log
  exit 1
fi

echo "🧹 [DEPLOY] 이전 nohup 로그 정리 중..." | tee -a $APP_DIR/deploy.log
sudo rm -f $APP_DIR/nohup.out || true

echo "🛑 [DEPLOY] 기존 애플리케이션 종료 중..." | tee -a $APP_DIR/deploy.log
pkill -f 'java -jar' || true

echo "✅ [DEPLOY] 새 JAR 실행 중: $JAR_FILE" | tee -a $APP_DIR/deploy.log
nohup java -jar "$JAR_FILE" > $APP_DIR/nohup.out 2>&1 &

echo "🎉 [DEPLOY] 배포 완료!" | tee -a $APP_DIR/deploy.log
