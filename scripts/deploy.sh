#!/bin/bash

# 배포 시작 로그 출력
echo "🚀 [DEPLOY] 배포 스크립트 시작"

# 애플리케이션 디렉토리 경로 지정
APP_DIR=/home/ec2-user/newsummarize

# build/libs 폴더 내 첫 번째 .jar 파일 경로 추출
JAR_FILE=$(ls $APP_DIR/build/libs/*.jar | head -n 1)

# 기존 실행 중인 Spring Boot 애플리케이션 종료 시도
echo "🛑 [DEPLOY] 기존 애플리케이션 종료 중..."
pkill -f 'java -jar' || true  # 실행 중인 프로세스가 없을 경우에도 에러 없이 통과

# 새 JAR 파일을 백그라운드로 실행하고 로그는 nohup.out에 출력
echo "✅ [DEPLOY] 새 JAR 실행 중: $JAR_FILE"
nohup java -jar $JAR_FILE > $APP_DIR/nohup.out 2>&1 &

# 배포 완료 메시지 출력
echo "🎉 [DEPLOY] 배포 완료!"
