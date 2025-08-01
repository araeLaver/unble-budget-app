@echo off
echo 🚀 Unble 가계부 서버를 개발 모드로 시작합니다...
echo.

REM 환경변수 설정
set SPRING_PROFILES_ACTIVE=dev

echo 🐘 개발 데이터베이스: PostgreSQL (unble_dev)  
echo 🌍 연결 정보: ep-blue-unit-a2ev3s9x.eu-central-1.pg.koyeb.app
echo 📊 스키마: unble_dev
echo.

REM Maven으로 실행
if exist mvnw.cmd (
    echo Maven Wrapper를 사용하여 실행합니다...
    mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=dev
) else if exist mvn (
    echo Maven을 사용하여 실행합니다...
    mvn spring-boot:run -Dspring-boot.run.profiles=dev
) else (
    echo.
    echo ⚠️ Maven이 설치되지 않았습니다.
    echo 권장사항: IDE(IntelliJ, Eclipse, VS Code)를 사용해서 실행하세요.
    echo.
    echo 또는 Maven을 설치한 후 이 스크립트를 다시 실행하세요.
    echo Maven 다운로드: https://maven.apache.org/download.cgi
)

echo.
echo ✅ 서버가 시작되었습니다!
echo 📱 접속: http://localhost:9090
echo 🛠️ 관리자: http://localhost:9090/admin.html
echo 📊 스키마: unble_dev (개발용)
echo.
pause