-- ================================================================
-- 스키마 정리 실행 스크립트
-- 현재 DB 상태: dev_schema, unble_budget_dev, unble_dev 존재
-- 목표: unble_dev, unble_prod만 남기기
-- ================================================================

-- 🔍 1단계: 현재 스키마 확인
SELECT 
    schemaname,
    schemaowner,
    (SELECT COUNT(*) FROM pg_tables WHERE schemaname = n.nspname) as table_count
FROM pg_namespace n
WHERE nspname IN ('dev_schema', 'unble_budget_dev', 'unble_dev', 'unble_prod')
ORDER BY schemaname;

-- 📊 각 스키마의 테이블 및 데이터 확인
SELECT 
    schemaname, 
    tablename,
    (SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = schemaname AND table_name = tablename) as table_exists
FROM (
    VALUES 
        ('dev_schema', 'users'),
        ('dev_schema', 'categories'), 
        ('dev_schema', 'transactions'),
        ('unble_budget_dev', 'users'),
        ('unble_budget_dev', 'categories'),
        ('unble_budget_dev', 'transactions'),
        ('unble_dev', 'users'),
        ('unble_dev', 'categories'),
        ('unble_dev', 'transactions')
) AS t(schemaname, tablename)
ORDER BY schemaname, tablename;

-- ================================================================
-- 🚀 2단계: 최종 스키마 생성 및 권한 설정
-- ================================================================

-- 최종 스키마 생성 확인
CREATE SCHEMA IF NOT EXISTS unble_dev;
CREATE SCHEMA IF NOT EXISTS unble_prod;

-- 권한 부여
GRANT ALL PRIVILEGES ON SCHEMA unble_dev TO untab;
GRANT ALL PRIVILEGES ON SCHEMA unble_prod TO untab;

-- 기존 테이블에 대한 권한 부여 (있다면)
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA unble_dev TO untab;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA unble_dev TO untab;

GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA unble_prod TO untab;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA unble_prod TO untab;

-- ================================================================
-- 🗑️ 3단계: 불필요한 스키마 제거 (주의!)
-- ================================================================

-- ⚠️ 경고: 아래 명령은 데이터를 완전히 삭제합니다!
-- 실행 전 중요한 데이터가 있는지 확인하세요.

-- dev_schema 제거 (있다면)
-- DROP SCHEMA IF EXISTS dev_schema CASCADE;

-- unble_budget_dev 제거 (중복 스키마)
-- DROP SCHEMA IF EXISTS unble_budget_dev CASCADE;

-- 기타 불필요한 스키마들 제거
-- DROP SCHEMA IF EXISTS unble_budget CASCADE;
-- DROP SCHEMA IF EXISTS unble_budget_prod CASCADE;

-- ================================================================
-- ✅ 4단계: 최종 확인
-- ================================================================

-- 남은 스키마 목록 (unble_dev, unble_prod만 있어야 함)
SELECT 
    schemaname, 
    schemaowner,
    (SELECT COUNT(*) FROM pg_tables WHERE schemaname = n.nspname) as table_count
FROM pg_namespace n
WHERE nspname LIKE '%unble%' OR nspname LIKE '%dev%' OR nspname LIKE '%prod%'
ORDER BY schemaname;

-- 최종 테이블 목록
SELECT schemaname, tablename, tableowner
FROM pg_tables 
WHERE schemaname IN ('unble_dev', 'unble_prod')
ORDER BY schemaname, tablename;

COMMENT ON SCHEMA unble_dev IS 'Unble Budget App - Development Environment (Final)';
COMMENT ON SCHEMA unble_prod IS 'Unble Budget App - Production Environment (Final)';

-- ================================================================
-- 🎯 실행 가이드
-- ================================================================
/*
1. 위의 1단계 쿼리를 먼저 실행하여 현재 상태 확인
2. 중요한 데이터가 있다면 백업 수행
3. 2단계 스키마 생성 및 권한 설정 실행
4. 3단계 DROP SCHEMA 명령의 주석(--) 제거 후 실행
5. 4단계로 최종 확인
*/