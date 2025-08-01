-- ================================================================
-- 스키마 정리 및 데이터 마이그레이션 스크립트
-- ================================================================

-- 🔍 1단계: 현재 스키마 상황 확인
SELECT 
    schemaname,
    schemaowner,
    (SELECT COUNT(*) FROM pg_tables WHERE schemaname = n.nspname) as table_count
FROM pg_namespace n
WHERE nspname IN ('dev_schema', 'unble_budget_dev', 'unble_dev', 'prod_schema', 'unble_budget_prod', 'unble_prod')
ORDER BY schemaname;

-- 📊 각 스키마별 테이블 및 데이터 확인
SELECT 
    schemaname, 
    tablename,
    (SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = schemaname AND table_name = tablename) as exists
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
-- 🔄 2단계: 데이터 마이그레이션 (dev_schema → unble_dev)
-- ================================================================

-- 최종 스키마 생성 확인
CREATE SCHEMA IF NOT EXISTS unble_dev;
CREATE SCHEMA IF NOT EXISTS unble_prod;

-- 권한 부여
GRANT ALL PRIVILEGES ON SCHEMA unble_dev TO untab;
GRANT ALL PRIVILEGES ON SCHEMA unble_prod TO untab;

-- dev_schema에 데이터가 있다면 마이그레이션
-- (실행 전 데이터 존재 여부 확인 필요)

/*
-- 사용자 데이터 마이그레이션 (dev_schema → unble_dev)
INSERT INTO unble_dev.users (email, password, name, created_at, updated_at, is_active, last_login_at)
SELECT email, password, name, created_at, updated_at, 
       COALESCE(is_active, true), last_login_at
FROM dev_schema.users
ON CONFLICT (email) DO UPDATE SET
    name = EXCLUDED.name,
    updated_at = EXCLUDED.updated_at;

-- 사용자 정의 카테고리 마이그레이션 (기본 카테고리 제외)
INSERT INTO unble_dev.categories (name, color, icon, category_type, is_default, user_id, created_at, sort_order)
SELECT name, color, icon, 
       CASE 
           WHEN category_type IS NULL THEN 'EXPENSE'::varchar
           ELSE category_type 
       END,
       COALESCE(is_default, false),
       user_id, created_at,
       COALESCE(sort_order, 0)
FROM dev_schema.categories
WHERE is_default = false OR user_id IS NOT NULL
ON CONFLICT (name, COALESCE(user_id, 0)) DO NOTHING;

-- 거래 데이터 마이그레이션
INSERT INTO unble_dev.transactions (user_id, category_id, amount, description, transaction_type, transaction_date, created_at, updated_at)
SELECT 
    u_new.id as user_id,
    c_new.id as category_id,
    t.amount, t.description, t.transaction_type, t.transaction_date, t.created_at, t.updated_at
FROM dev_schema.transactions t
JOIN dev_schema.users u_old ON t.user_id = u_old.id
JOIN unble_dev.users u_new ON u_old.email = u_new.email
LEFT JOIN dev_schema.categories c_old ON t.category_id = c_old.id
LEFT JOIN unble_dev.categories c_new ON (
    c_old.name = c_new.name AND 
    ((c_old.user_id IS NULL AND c_new.user_id IS NULL) OR 
     (c_old.user_id = u_old.id AND c_new.user_id = u_new.id))
);
*/

-- ================================================================
-- 🗑️ 3단계: 불필요한 스키마 제거
-- ================================================================

-- ⚠️ 주의: 데이터 마이그레이션 완료 및 검증 후 실행하세요!

-- 마이그레이션 완료 후 기존 스키마 삭제
-- DROP SCHEMA IF EXISTS dev_schema CASCADE;
-- DROP SCHEMA IF EXISTS unble_budget_dev CASCADE;
-- DROP SCHEMA IF EXISTS prod_schema CASCADE;  
-- DROP SCHEMA IF EXISTS unble_budget_prod CASCADE;

-- ================================================================
-- ✅ 4단계: 최종 확인
-- ================================================================

-- 최종 스키마 목록
SELECT schemaname, schemaowner 
FROM pg_namespace 
WHERE nspname IN ('unble_dev', 'unble_prod')
ORDER BY schemaname;

-- 각 스키마의 테이블 및 데이터 개수
SELECT 
    schemaname,
    tablename,
    (xpath('//row/c/text()', query_to_xml(format('SELECT COUNT(*) as c FROM %I.%I', schemaname, tablename), false, true, '')))[1]::text::int as row_count
FROM pg_tables 
WHERE schemaname IN ('unble_dev', 'unble_prod')
ORDER BY schemaname, tablename;

-- 권한 확인
SELECT 
    nspname as schema_name,
    nspowner::regrole as owner,
    array_to_string(nspacl, ', ') as privileges
FROM pg_namespace 
WHERE nspname IN ('unble_dev', 'unble_prod');

COMMENT ON SCHEMA unble_dev IS 'Unble Budget App - Development Environment';
COMMENT ON SCHEMA unble_prod IS 'Unble Budget App - Production Environment';