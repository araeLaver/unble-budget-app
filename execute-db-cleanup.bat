@echo off
echo 🗄️ PostgreSQL 스키마 정리 실행
echo.

REM PostgreSQL 설치 확인
where psql >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ PostgreSQL 클라이언트가 설치되어 있지 않습니다.
    echo.
    echo 📥 PostgreSQL 설치 방법:
    echo 1. https://www.postgresql.org/download/windows/ 에서 다운로드
    echo 2. 또는 PostgreSQL Portable 사용
    echo 3. 또는 pgAdmin 4 설치 후 SQL 도구 사용
    echo.
    echo 🔧 임시 해결책:
    echo - 웹 브라우저에서 Koyeb Dashboard 접속
    echo - Database 섹션에서 SQL 쿼리 실행
    echo.
    pause
    exit /b 1
)

echo 🔍 1단계: 현재 스키마 상태 확인
echo.
psql -h ep-blue-unit-a2ev3s9x.eu-central-1.pg.koyeb.app -U untab -d untab -f "src\main\resources\sql\check-current-schemas.sql"

echo.
echo ⚠️  위 결과를 확인하고 계속 진행하시겠습니까?
echo    y: 계속 진행 (스키마 삭제)
echo    n: 중단
set /p choice="선택 (y/n): "

if /i "%choice%" neq "y" (
    echo 중단되었습니다.
    pause
    exit /b 0
)

echo.
echo 🗑️  2단계: 불필요한 스키마 삭제 실행
echo.
psql -h ep-blue-unit-a2ev3s9x.eu-central-1.pg.koyeb.app -U untab -d untab -f "src\main\resources\sql\final-cleanup.sql"

echo.
echo ✅ 스키마 정리 완료!
echo 📊 최종 결과: unble_dev (개발), unble_prod (운영)
echo.
pause