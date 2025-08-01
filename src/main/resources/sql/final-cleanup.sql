-- ================================================================
-- 3개 스키마 중 2개 제거 (최종 정리)
-- 현재: dev_schema, unble_budget_dev, unble_dev
-- 목표: unble_dev만 남기고 나머지 삭제
-- ================================================================

-- 🔍 1. 마지막 확인 (삭제 전)
SELECT 
    schemaname,
    (SELECT COUNT(*) FROM pg_tables WHERE schemaname = n.nspname) as table_count
FROM pg_namespace n
WHERE nspname IN ('dev_schema', 'unble_budget_dev', 'unble_dev')
ORDER BY schemaname;

-- ================================================================
-- 🗑️ 2. 불필요한 스키마 삭제 실행
-- ================================================================

-- ⚠️ 경고: 아래 명령은 스키마와 모든 데이터를 완전히 삭제합니다!

-- dev_schema 삭제
DROP SCHEMA IF EXISTS dev_schema CASCADE;

-- unble_budget_dev 삭제  
DROP SCHEMA IF EXISTS unble_budget_dev CASCADE;

-- ================================================================
-- ✅ 3. 최종 확인
-- ================================================================

-- unble_dev만 남았는지 확인
SELECT schemaname, schemaowner
FROM pg_namespace 
WHERE nspname LIKE '%dev%' OR nspname LIKE '%budget%' OR nspname LIKE '%unble%'
ORDER BY schemaname;

-- unble_prod 스키마 생성 (운영용)
CREATE SCHEMA IF NOT EXISTS unble_prod;

-- 권한 설정
GRANT ALL PRIVILEGES ON SCHEMA unble_dev TO untab;
GRANT ALL PRIVILEGES ON SCHEMA unble_prod TO untab;

-- 기존 테이블 권한
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA unble_dev TO untab;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA unble_dev TO untab;

-- 스키마 설명 추가
COMMENT ON SCHEMA unble_dev IS 'Unble Budget App - Development (Final Clean)';
COMMENT ON SCHEMA unble_prod IS 'Unble Budget App - Production (Final Clean)';

-- ================================================================
-- 🎯 최종 결과
-- ================================================================

-- 남은 스키마 목록 (unble_dev, unble_prod만 있어야 함)
SELECT 
    schemaname, 
    schemaowner,
    obj_description(oid) as description
FROM pg_namespace 
WHERE nspname IN ('unble_dev', 'unble_prod')
ORDER BY schemaname;