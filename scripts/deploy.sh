#!/bin/bash

echo "🚀 [DEPLOY] 배포 스크립트 시작"

# JAR 파일 경로: /home/ubuntu에 복사된 jar 중 가장 최신
APP_DIR=/home/ubuntu
JAR_FILE=$(ls $APP_DIR/*.jar | head -n 1)



if [ -z "$JAR_FILE" ]; then
  echo "❌ [DEPLOY] JAR 파일을 찾을 수 없습니다. 종료합니다." | tee -a $APP_DIR/deploy.log
  exit 1
fi

echo "🛑 [DEPLOY] 기존 애플리케이션 종료 중..." | tee -a $APP_DIR/deploy.log
pkill -f 'java -jar' || true

echo "✅ [DEPLOY] 새 JAR 실행 중: $JAR_FILE" | tee -a $APP_DIR/deploy.log
nohup java -jar $JAR_FILE > $APP_DIR/nohup.out 2>&1 &

echo "🎉 [DEPLOY] 배포 완료!" | tee -a $APP_DIR/deploy.log
