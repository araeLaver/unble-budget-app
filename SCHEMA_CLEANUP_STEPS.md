# 🧹 스키마 정리 실행 단계

## 📋 현재 상황
- **실제 DB에 존재**: `dev_schema`, `unble_budget_dev`, `unble_dev`
- **목표**: `unble_dev`, `unble_prod`만 남기기

## 🚀 실행 순서

### 1. 데이터베이스 접속
```bash
# PostgreSQL 접속
psql -h ep-blue-unit-a2ev3s9x.eu-central-1.pg.koyeb.app -U untab -d untab
```

### 2. 현재 상태 확인
```sql
-- execute-cleanup.sql의 1단계 쿼리 실행
SELECT 
    schemaname,
    schemaowner,
    (SELECT COUNT(*) FROM pg_tables WHERE schemaname = n.nspname) as table_count
FROM pg_namespace n
WHERE nspname IN ('dev_schema', 'unble_budget_dev', 'unble_dev', 'unble_prod')
ORDER BY schemaname;
```

### 3. 스키마 정리 실행
```sql
-- 최종 스키마 생성 확인
CREATE SCHEMA IF NOT EXISTS unble_dev;
CREATE SCHEMA IF NOT EXISTS unble_prod;

-- 권한 부여
GRANT ALL PRIVILEGES ON SCHEMA unble_dev TO untab;
GRANT ALL PRIVILEGES ON SCHEMA unble_prod TO untab;
```

### 4. 불필요한 스키마 삭제
```sql
-- ⚠️ 주의: 실제 데이터 확인 후 실행
DROP SCHEMA IF EXISTS dev_schema CASCADE;
DROP SCHEMA IF EXISTS unble_budget_dev CASCADE;
```

### 5. 최종 확인
```sql
-- 남은 스키마 확인 (unble_dev, unble_prod만 있어야 함)
SELECT schemaname FROM pg_namespace 
WHERE nspname LIKE '%unble%' OR nspname LIKE '%dev%' OR nspname LIKE '%prod%'
ORDER BY schemaname;
```

## ✅ 완료 후 확인사항

1. **애플리케이션 실행**: `run.bat` 또는 `run-prod.bat`
2. **연결 테스트**: http://localhost:9090
3. **로그 확인**: 데이터베이스 연결 오류 없는지 확인

## 🔙 롤백 방법

문제 발생 시:
```sql
-- 필요시 스키마 재생성
CREATE SCHEMA IF NOT EXISTS unble_dev;
CREATE SCHEMA IF NOT EXISTS unble_prod;
GRANT ALL PRIVILEGES ON SCHEMA unble_dev TO untab;
GRANT ALL PRIVILEGES ON SCHEMA unble_prod TO untab;
```

## 📞 문제 해결

- **연결 실패**: application-dev.properties의 스키마 설정 확인
- **테이블 없음**: Spring Boot 시작 시 자동 생성됨 (spring.jpa.hibernate.ddl-auto=update)
- **권한 오류**: GRANT 명령 재실행