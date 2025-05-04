#!/bin/bash

echo "🚀 [DEPLOY] 배포 스크립트 시작"

APP_DIR=/home/ec2-user/newsummarize
JAR_NAME=backend-0.0.1-SNAPSHOT.jar
JAR_PATH=$APP_DIR/$JAR_NAME
LOG_FILE=$APP_DIR/nohup.out

echo "🛑 [DEPLOY] 기존 실행 중인 Spring Boot 애플리케이션 종료 중..."
pkill -f '.jar' || true

sleep 3

echo "✅ [DEPLOY] 새 JAR 실행 중: $JAR_PATH"
nohup java -jar "$JAR_PATH" > "$LOG_FILE" 2>&1 &

echo "🎉 [DEPLOY] 배포 완료!"
