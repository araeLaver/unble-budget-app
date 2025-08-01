# 🛠️ 데이터베이스 스키마 정리 매뉴얼

PostgreSQL 클라이언트가 설치되어 있지 않아서 수동으로 실행해야 합니다.

## 🔧 실행 방법 옵션

### 옵션 1: Koyeb Dashboard 사용 (추천)
1. https://app.koyeb.com 접속
2. 데이터베이스 섹션으로 이동
3. SQL Query 도구 사용
4. 아래 스크립트들을 순서대로 실행

### 옵션 2: pgAdmin 4 설치
1. https://www.pgadmin.org/download/ 에서 다운로드
2. 설치 후 데이터베이스 연결:
   - 호스트: `ep-blue-unit-a2ev3s9x.eu-central-1.pg.koyeb.app`
   - 포트: `5432`
   - 데이터베이스: `untab`
   - 사용자: `untab`
   - 비밀번호: `[패스워드]`

### 옵션 3: PostgreSQL 클라이언트 설치
```bash
# Windows에서 PostgreSQL 설치 후
psql -h ep-blue-unit-a2ev3s9x.eu-central-1.pg.koyeb.app -U untab -d untab
```

## 📝 실행할 SQL 스크립트 순서

### 1단계: 현재 상태 확인
```sql
-- 현재 3개 스키마 존재 확인
SELECT 
    schemaname,
    schemaowner
FROM pg_namespace 
WHERE nspname IN ('dev_schema', 'unble_budget_dev', 'unble_dev')
ORDER BY schemaname;

-- 각 스키마의 테이블 현황
SELECT 
    schemaname, 
    tablename,
    tableowner
FROM pg_tables 
WHERE schemaname IN ('dev_schema', 'unble_budget_dev', 'unble_dev')
ORDER BY schemaname, tablename;
```

### 2단계: 데이터 개수 확인 (중요한 데이터 있는지 체크)
```sql
-- dev_schema 데이터 개수
SELECT 'dev_schema' as schema_name,
       COALESCE((SELECT COUNT(*) FROM dev_schema.users), 0) as users_count,
       COALESCE((SELECT COUNT(*) FROM dev_schema.categories), 0) as categories_count,
       COALESCE((SELECT COUNT(*) FROM dev_schema.transactions), 0) as transactions_count;

-- unble_budget_dev 데이터 개수
SELECT 'unble_budget_dev' as schema_name,
       COALESCE((SELECT COUNT(*) FROM unble_budget_dev.users), 0) as users_count,
       COALESCE((SELECT COUNT(*) FROM unble_budget_dev.categories), 0) as categories_count,
       COALESCE((SELECT COUNT(*) FROM unble_budget_dev.transactions), 0) as transactions_count;

-- unble_dev 데이터 개수
SELECT 'unble_dev' as schema_name,
       COALESCE((SELECT COUNT(*) FROM unble_dev.users), 0) as users_count,
       COALESCE((SELECT COUNT(*) FROM unble_dev.categories), 0) as categories_count,
       COALESCE((SELECT COUNT(*) FROM unble_dev.transactions), 0) as transactions_count;
```

### 3단계: 불필요한 스키마 삭제 (⚠️ 주의!)
```sql
-- dev_schema 삭제
DROP SCHEMA IF EXISTS dev_schema CASCADE;

-- unble_budget_dev 삭제  
DROP SCHEMA IF EXISTS unble_budget_dev CASCADE;
```

### 4단계: 운영 스키마 생성 및 권한 설정
```sql
-- unble_prod 스키마 생성 (운영용)
CREATE SCHEMA IF NOT EXISTS unble_prod;

-- 권한 설정
GRANT ALL PRIVILEGES ON SCHEMA unble_dev TO untab;
GRANT ALL PRIVILEGES ON SCHEMA unble_prod TO untab;

-- 기존 테이블 권한
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA unble_dev TO untab;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA unble_dev TO untab;
```

### 5단계: 최종 확인
```sql
-- 남은 스키마 목록 (unble_dev, unble_prod만 있어야 함)
SELECT 
    schemaname, 
    schemaowner
FROM pg_namespace 
WHERE nspname IN ('unble_dev', 'unble_prod')
ORDER BY schemaname;
```

## ✅ 완료 후 확인

1. **애플리케이션 실행**: `run.bat`
2. **접속 테스트**: http://localhost:9090
3. **로그 확인**: 데이터베이스 연결 오류 없는지 확인

## 🎯 최종 결과

- ✅ **unble_dev**: 개발 환경
- ✅ **unble_prod**: 운영 환경  
- ❌ **dev_schema**: 삭제됨
- ❌ **unble_budget_dev**: 삭제됨