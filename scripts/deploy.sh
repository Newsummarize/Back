#!/bin/bash

echo "🚀 [DEPLOY] 배포 스크립트 시작"

APP_DIR=/home/ec2-user/newsummarize
JAR_NAME=newsummarize.jar
JAR_PATH=$APP_DIR/$JAR_NAME

echo "🛑 [DEPLOY] 기존 실행 중인 Spring Boot 애플리케이션 종료 중..."
# 기존 수동 실행 포함 모든 Java -jar 프로세스 중지
pkill -f '.jar' || true

# 3초 대기 (완전히 종료되도록)
sleep 3

echo "✅ [DEPLOY] 새 JAR 실행 중: $JAR_PATH"
# 새 JAR 백그라운드 실행
nohup java -Xms1g -Xmx2g -jar $JAR_PATH > $APP_DIR/nohup.out 2>&1 &

echo "🎉 [DEPLOY] 배포 완료!"
