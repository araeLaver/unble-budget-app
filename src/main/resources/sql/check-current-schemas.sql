-- ================================================================
-- 현재 3개 스키마 상태 확인
-- dev_schema, unble_budget_dev, unble_dev
-- ================================================================

-- 🔍 1. 현재 3개 스키마 존재 확인
SELECT 
    schemaname,
    schemaowner
FROM pg_namespace 
WHERE nspname IN ('dev_schema', 'unble_budget_dev', 'unble_dev')
ORDER BY schemaname;

-- 📊 2. 각 스키마의 테이블 현황
SELECT 
    schemaname, 
    tablename,
    tableowner
FROM pg_tables 
WHERE schemaname IN ('dev_schema', 'unble_budget_dev', 'unble_dev')
ORDER BY schemaname, tablename;

-- 📈 3. 각 스키마의 데이터 개수 확인
-- dev_schema
SELECT 'dev_schema' as schema_name,
       COALESCE((SELECT COUNT(*) FROM dev_schema.users), 0) as users_count,
       COALESCE((SELECT COUNT(*) FROM dev_schema.categories), 0) as categories_count,
       COALESCE((SELECT COUNT(*) FROM dev_schema.transactions), 0) as transactions_count;

-- unble_budget_dev  
SELECT 'unble_budget_dev' as schema_name,
       COALESCE((SELECT COUNT(*) FROM unble_budget_dev.users), 0) as users_count,
       COALESCE((SELECT COUNT(*) FROM unble_budget_dev.categories), 0) as categories_count,
       COALESCE((SELECT COUNT(*) FROM unble_budget_dev.transactions), 0) as transactions_count;

-- unble_dev
SELECT 'unble_dev' as schema_name,
       COALESCE((SELECT COUNT(*) FROM unble_dev.users), 0) as users_count,
       COALESCE((SELECT COUNT(*) FROM unble_dev.categories), 0) as categories_count,
       COALESCE((SELECT COUNT(*) FROM unble_dev.transactions), 0) as transactions_count;

-- ================================================================
-- 🎯 결정 방안
-- ================================================================
/*
위 결과를 보고:
1. 어떤 스키마에 실제 데이터가 있는지 확인
2. 가장 최신/완전한 데이터가 있는 스키마를 선택
3. 나머지 2개 스키마 삭제

추천 방안:
- unble_dev를 최종 개발 스키마로 사용
- dev_schema, unble_budget_dev 삭제
*/