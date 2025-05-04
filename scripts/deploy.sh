#!/bin/bash

echo "🚀 [DEPLOY] 배포 스크립트 시작"

# JAR 파일 경로
JAR_PATH=/home/ubuntu/newsummarize.jar

# 기존 애플리케이션 종료
echo "🛑 [DEPLOY] 기존 실행 중인 Spring Boot 애플리케이션 종료 중..."
PID=$(pgrep -f "$JAR_PATH")
if [ -n "$PID" ]; then
  kill -9 "$PID"
  echo "🔻 종료된 PID: $PID"
fi

# JAR 복사 (빌드 결과물을 Ubuntu 홈 디렉토리로 이동)
cp /opt/codedeploy-agent/deployment-root/*/deployment-archive/build/libs/*.jar "$JAR_PATH"

# 실행
echo "✅ [DEPLOY] 새 JAR 실행 중: $JAR_PATH"
nohup java -jar "$JAR_PATH" > /home/ubuntu/nohup.out 2>&1 &
echo "🎉 [DEPLOY] 배포 완료!"
