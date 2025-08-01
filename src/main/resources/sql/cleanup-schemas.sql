-- ================================================================
-- 불필요한 스키마 정리 스크립트
-- ================================================================

-- 주의: 이 스크립트는 데이터가 있는 스키마를 삭제할 수 있습니다.
-- 실행 전 반드시 백업을 수행하세요.

-- 1. 기존 불필요 스키마 목록 확인
SELECT schemaname, schemaowner 
FROM pg_catalog.pg_namespace n
LEFT JOIN pg_catalog.pg_user u ON n.nspowner = u.usesysid
WHERE schemaname NOT IN ('information_schema', 'pg_catalog', 'pg_toast', 'public', 'unble_dev', 'unble_prod')
ORDER BY schemaname;

-- 2. dev_schema가 존재한다면 삭제 (데이터 확인 후)
-- DROP SCHEMA IF EXISTS dev_schema CASCADE;

-- 3. prod_schema가 존재한다면 삭제 (데이터 확인 후)  
-- DROP SCHEMA IF EXISTS prod_schema CASCADE;

-- 4. 기타 불필요한 스키마들 삭제 (필요시)
-- DROP SCHEMA IF EXISTS unble_budget CASCADE;
-- DROP SCHEMA IF EXISTS unble_budget_dev CASCADE;
-- DROP SCHEMA IF EXISTS unble_budget_prod CASCADE;

-- 5. 새로운 스키마 생성 확인
CREATE SCHEMA IF NOT EXISTS unble_dev;
CREATE SCHEMA IF NOT EXISTS unble_prod;

-- 스키마 권한 설정
GRANT ALL PRIVILEGES ON SCHEMA unble_dev TO untab;
GRANT ALL PRIVILEGES ON SCHEMA unble_prod TO untab;

-- 스키마별 테이블 목록 확인
SELECT schemaname, tablename 
FROM pg_tables 
WHERE schemaname IN ('unble_dev', 'unble_prod', 'dev_schema', 'prod_schema', 'unble_budget_dev', 'unble_budget_prod')
ORDER BY schemaname, tablename;