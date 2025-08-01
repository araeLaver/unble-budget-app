@echo off
echo 🚀 Unble 가계부 서버를 운영 모드로 시작합니다...
echo.

REM 환경변수 설정
set SPRING_PROFILES_ACTIVE=prod

echo 🐘 운영 데이터베이스: PostgreSQL (unble_prod)
echo 🌍 연결 정보: ep-blue-unit-a2ev3s9x.eu-central-1.pg.koyeb.app
echo 📊 스키마: unble_prod
echo.

REM Maven으로 실행
if exist mvnw.cmd (
    echo Maven Wrapper를 사용하여 실행합니다...
    mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=prod
) else (
    echo Maven을 사용하여 실행합니다...
    mvn spring-boot:run -Dspring-boot.run.profiles=prod
)

echo.
echo ✅ 서버가 시작되었습니다!
echo 📱 접속: http://localhost:9090
echo 🛠️ 관리자: http://localhost:9090/admin.html
echo 📊 스키마: unble_prod (운영용)
echo.
pause