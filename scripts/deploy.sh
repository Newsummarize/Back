#!/bin/bash

echo "🚀 [DEPLOY] 배포 스크립트 시작"

# jar 이름 정확히 지정
JAR_NAME=backend-0.0.1-SNAPSHOT.jar
JAR_PATH=/home/ubuntu/$JAR_NAME

# 기존 실행 중인 앱 종료
echo "🛑 [DEPLOY] 기존 애플리케이션 종료 중..."
PID=$(pgrep -f "$JAR_NAME")
if [ -n "$PID" ]; then
  kill -9 "$PID"
  echo "🔻 종료된 PID: $PID"
fi

# jar 복사
cp /opt/codedeploy-agent/deployment-root/*/deployment-archive/build/libs/$JAR_NAME "$JAR_PATH"

# 실행
echo "✅ [DEPLOY] 새 JAR 실행 중: $JAR_PATH"
nohup java -jar "$JAR_PATH" > /home/ubuntu/nohup.out 2>&1 &
echo "🎉 [DEPLOY] 배포 완료!"
