#!/bin/bash

echo "🚀 [DEPLOY] 배포 스크립트 시작"

# 기존 프로세스 종료
echo "🛑 [DEPLOY] 기존 애플리케이션 종료 중..."
pkill -f 'backend-0.0.1-SNAPSHOT.jar'

# 새 애플리케이션 실행
echo "✅ [DEPLOY] 새 JAR 실행 중:"
nohup java -jar /home/ubuntu/backend-0.0.1-SNAPSHOT.jar > /home/ubuntu/nohup.out 2>&1 &

echo "🎉 [DEPLOY] 배포 완료!"

exit 0
