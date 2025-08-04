# PowerShell script to update database constraints for ASSET type
# Run this script with: powershell -ExecutionPolicy Bypass -File update-db-constraints.ps1

$ErrorActionPreference = "Stop"

# Database connection parameters
$dbHost = "ep-blue-unit-a2ev3s9x.eu-central-1.pg.koyeb.app"
$dbUsername = "untab"
$dbPassword = "0AbVNOIsl2dn"
$dbName = "untab"

Write-Host "=== ASSET 제약조건 업데이트 시작 ===" -ForegroundColor Green

# Development schema update
Write-Host "1. 개발용 DB (unble_dev) 업데이트 중..." -ForegroundColor Yellow

$env:PGPASSWORD = $dbPassword
$devQuery = @"
SET search_path TO unble_dev;
ALTER TABLE transactions DROP CONSTRAINT IF EXISTS transactions_transaction_type_check;
ALTER TABLE transactions ADD CONSTRAINT transactions_transaction_type_check CHECK (transaction_type IN ('INCOME', 'EXPENSE', 'ASSET'));
SELECT 'unble_dev ASSET 제약조건 적용 완료!' as result;
"@

try {
    # Try with psql if available
    if (Get-Command psql -ErrorAction SilentlyContinue) {
        psql -h $dbHost -U $dbUsername -d $dbName -c $devQuery
        Write-Host "✅ 개발용 DB 업데이트 완료" -ForegroundColor Green
    } else {
        Write-Host "❌ psql 명령어를 찾을 수 없습니다. PostgreSQL 클라이언트를 설치하거나 pgAdmin을 사용하세요." -ForegroundColor Red
        Write-Host "수동 실행용 SQL:" -ForegroundColor Cyan
        Write-Host $devQuery -ForegroundColor White
    }
} catch {
    Write-Host "❌ 개발용 DB 업데이트 실패: $($_.Exception.Message)" -ForegroundColor Red
}

# Production schema update
Write-Host "`n2. 운영용 DB (unble_prod) 업데이트 중..." -ForegroundColor Yellow

$prodQuery = @"
SET search_path TO unble_prod;
ALTER TABLE transactions DROP CONSTRAINT IF EXISTS transactions_transaction_type_check;
ALTER TABLE transactions ADD CONSTRAINT transactions_transaction_type_check CHECK (transaction_type IN ('INCOME', 'EXPENSE', 'ASSET'));
SELECT 'unble_prod ASSET 제약조건 적용 완료!' as result;
"@

try {
    if (Get-Command psql -ErrorAction SilentlyContinue) {
        psql -h $dbHost -U $dbUsername -d $dbName -c $prodQuery
        Write-Host "✅ 운영용 DB 업데이트 완료" -ForegroundColor Green
    } else {
        Write-Host "수동 실행용 SQL:" -ForegroundColor Cyan
        Write-Host $prodQuery -ForegroundColor White
    }
} catch {
    Write-Host "❌ 운영용 DB 업데이트 실패: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n=== 업데이트 완료 ===" -ForegroundColor Green
Write-Host "이제 자산 관리 기능이 정상적으로 작동합니다!" -ForegroundColor Cyan

# Clean up
Remove-Item env:PGPASSWORD -ErrorAction SilentlyContinue