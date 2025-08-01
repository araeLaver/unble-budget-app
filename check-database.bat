@echo off
echo 🔍 데이터베이스 상태 확인 중...
echo.

REM 개발 환경으로 Spring Boot 실행하여 데이터베이스 상태 확인
echo 📊 개발 환경 (unble_dev) 확인:
echo.

REM Maven으로 애플리케이션 실행 (짧은 시간만)
if exist mvnw.cmd (
    echo Maven Wrapper를 사용하여 실행합니다...
    timeout /t 2 >nul
    mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=dev -Dspring-boot.run.jvmArguments="-Dspring.main.web-application-type=none"
) else (
    echo Maven을 사용하여 실행합니다...
    timeout /t 2 >nul
    mvn spring-boot:run -Dspring-boot.run.profiles=dev -Dspring-boot.run.jvmArguments="-Dspring.main.web-application-type=none"
)

echo.
echo ✅ 데이터베이스 상태 확인 완료!
echo.
pause