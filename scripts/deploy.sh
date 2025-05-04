#!/bin/bash

echo "🚀 [DEPLOY] 배포 스크립트 시작"

APP_DIR=/home/ec2-user/newsummarize  # 배포 디렉토리
JAR_NAME=newsummarize.jar            # 배포할 JAR 파일 이름
JAR_PATH=$APP_DIR/$JAR_NAME          # 전체 경로

echo "🛑 [DEPLOY] 기존 실행 중인 Spring Boot 애플리케이션 종료 중..."
pkill -f '.jar' || true              # 기존 jar 프로세스 모두 종료 (수동 실행 포함)

sleep 3                              # 3초 대기 (완전 종료 보장)

echo "✅ [DEPLOY] 새 JAR 실행 중: $JAR_PATH"
nohup java -jar "$JAR_PATH" > /home/ec2-user/newsummarize/nohup.out 2>&1 &

echo "🎉 [DEPLOY] 배포 완료!"
