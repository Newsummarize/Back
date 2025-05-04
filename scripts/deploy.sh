#!/bin/bash

echo "🚀 [DEPLOY] 배포 스크립트 시작"

cd /home/ubuntu

JAR_NAME=backend-0.0.1-SNAPSHOT.jar

# 기존 프로세스 종료
echo "🛑 [DEPLOY] 기존 애플리케이션 종료 중..."
PID=$(pgrep -f "$JAR_NAME")
if [ -n "$PID" ]; then
  kill -9 "$PID"
  echo "🔻 종료된 PID: $PID"
fi

# 실행
echo "✅ [DEPLOY] 새 JAR 실행 중: $JAR_NAME"
nohup java -jar "$JAR_NAME" > /home/ubuntu/nohup.out 2>&1 &
echo "🎉 [DEPLOY] 배포 완료!"
