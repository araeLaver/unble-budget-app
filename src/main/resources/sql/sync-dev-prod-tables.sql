-- ================================================================
-- 개발/운영 환경 테이블 구조 동기화 스크립트
-- unble_dev와 unble_prod 스키마의 테이블 구조를 동일하게 맞춤
-- ================================================================

-- 🔍 1단계: 현재 테이블 구조 비교
-- 개발 환경 테이블 목록
SELECT 
    'unble_dev' as schema_name,
    tablename,
    tableowner
FROM pg_tables 
WHERE schemaname = 'unble_dev'
ORDER BY tablename;

-- 운영 환경 테이블 목록
SELECT 
    'unble_prod' as schema_name,
    tablename,
    tableowner
FROM pg_tables 
WHERE schemaname = 'unble_prod'
ORDER BY tablename;

-- 테이블별 컬럼 구조 비교
SELECT 
    table_schema,
    table_name,
    column_name,
    data_type,
    is_nullable,
    column_default
FROM information_schema.columns
WHERE table_schema IN ('unble_dev', 'unble_prod')
ORDER BY table_schema, table_name, ordinal_position;

-- ================================================================
-- 🚀 2단계: 운영 환경에 개발 환경과 동일한 테이블 생성
-- ================================================================

-- 스키마 생성 확인
CREATE SCHEMA IF NOT EXISTS unble_prod;

-- 권한 설정
GRANT ALL PRIVILEGES ON SCHEMA unble_prod TO untab;

-- ================================================================
-- 📋 3단계: 완전한 테이블 구조 생성 (개발환경 기준)
-- ================================================================

-- Users 테이블
CREATE TABLE IF NOT EXISTS unble_prod.users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(100) NOT NULL,
    is_active BOOLEAN DEFAULT true,
    last_login_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Categories 테이블  
CREATE TABLE IF NOT EXISTS unble_prod.categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    color VARCHAR(7) DEFAULT '#000000',
    icon VARCHAR(50) DEFAULT '💰',
    category_type VARCHAR(20) DEFAULT 'EXPENSE' CHECK (category_type IN ('INCOME', 'EXPENSE', 'ASSET')),
    is_default BOOLEAN DEFAULT false,
    sort_order INTEGER DEFAULT 0,
    user_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_category_user FOREIGN KEY (user_id) REFERENCES unble_prod.users(id) ON DELETE CASCADE,
    CONSTRAINT unique_category_per_user UNIQUE (name, COALESCE(user_id, 0))
);

-- Transactions 테이블
CREATE TABLE IF NOT EXISTS unble_prod.transactions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    description TEXT,
    transaction_type VARCHAR(20) NOT NULL CHECK (transaction_type IN ('INCOME', 'EXPENSE', 'ASSET')),
    transaction_date DATE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_transaction_user FOREIGN KEY (user_id) REFERENCES unble_prod.users(id) ON DELETE CASCADE,
    CONSTRAINT fk_transaction_category FOREIGN KEY (category_id) REFERENCES unble_prod.categories(id) ON DELETE RESTRICT
);

-- User Settings 테이블
CREATE TABLE IF NOT EXISTS unble_prod.user_settings (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    currency VARCHAR(3) DEFAULT 'KRW',
    theme VARCHAR(20) DEFAULT 'light',
    notification_enabled BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_settings_user FOREIGN KEY (user_id) REFERENCES unble_prod.users(id) ON DELETE CASCADE
);

-- Budget Plans 테이블
CREATE TABLE IF NOT EXISTS unble_prod.budget_plans (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    budget_month DATE NOT NULL,
    planned_amount DECIMAL(15,2) NOT NULL,
    actual_amount DECIMAL(15,2) DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_budget_user FOREIGN KEY (user_id) REFERENCES unble_prod.users(id) ON DELETE CASCADE,
    CONSTRAINT fk_budget_category FOREIGN KEY (category_id) REFERENCES unble_prod.categories(id) ON DELETE CASCADE,
    CONSTRAINT unique_budget_per_month UNIQUE (user_id, category_id, budget_month)
);

-- ================================================================
-- 🔧 4단계: 인덱스 생성
-- ================================================================

-- Users 인덱스
CREATE INDEX IF NOT EXISTS idx_users_email ON unble_prod.users(email);
CREATE INDEX IF NOT EXISTS idx_users_active ON unble_prod.users(is_active);

-- Categories 인덱스
CREATE INDEX IF NOT EXISTS idx_categories_user_id ON unble_prod.categories(user_id);
CREATE INDEX IF NOT EXISTS idx_categories_type ON unble_prod.categories(category_type);
CREATE INDEX IF NOT EXISTS idx_categories_default ON unble_prod.categories(is_default);

-- Transactions 인덱스  
CREATE INDEX IF NOT EXISTS idx_transactions_user_id ON unble_prod.transactions(user_id);
CREATE INDEX IF NOT EXISTS idx_transactions_category_id ON unble_prod.transactions(category_id);
CREATE INDEX IF NOT EXISTS idx_transactions_date ON unble_prod.transactions(transaction_date);
CREATE INDEX IF NOT EXISTS idx_transactions_type ON unble_prod.transactions(transaction_type);
CREATE INDEX IF NOT EXISTS idx_transactions_user_date ON unble_prod.transactions(user_id, transaction_date);

-- Budget Plans 인덱스
CREATE INDEX IF NOT EXISTS idx_budget_user_month ON unble_prod.budget_plans(user_id, budget_month);
CREATE INDEX IF NOT EXISTS idx_budget_category ON unble_prod.budget_plans(category_id);

-- ================================================================
-- 🎯 5단계: 기본 카테고리 데이터 삽입 (운영환경)
-- ================================================================

-- 기본 카테고리 삽입 (개발환경과 동일하게)
INSERT INTO unble_prod.categories (name, color, icon, category_type, is_default, sort_order) VALUES
-- 자산 관리
('현금', '#2ECC71', '💰', 'ASSET', true, 1),
('은행예금', '#3498DB', '🏦', 'ASSET', true, 2),
('적금', '#9B59B6', '💎', 'ASSET', true, 3),
('투자', '#E74C3C', '📈', 'ASSET', true, 4),
('카드잔액', '#F39C12', '💳', 'ASSET', true, 5),

-- 지출 카테고리
('식비', '#E67E22', '🍽️', 'EXPENSE', true, 10),
('교통비', '#3498DB', '🚗', 'EXPENSE', true, 11),
('생활용품', '#95A5A6', '🛒', 'EXPENSE', true, 12),
('의료비', '#E74C3C', '🏥', 'EXPENSE', true, 13),
('문화생활', '#9B59B6', '🎬', 'EXPENSE', true, 14),
('통신비', '#34495E', '📱', 'EXPENSE', true, 15),
('주거비', '#16A085', '🏠', 'EXPENSE', true, 16),
('보험료', '#2980B9', '🛡️', 'EXPENSE', true, 17),
('교육비', '#8E44AD', '📚', 'EXPENSE', true, 18),
('기타지출', '#BDC3C7', '💸', 'EXPENSE', true, 19),

-- 수입 카테고리  
('급여', '#27AE60', '💼', 'INCOME', true, 20),
('부업', '#F1C40F', '💻', 'INCOME', true, 21),
('투자수익', '#E74C3C', '📊', 'INCOME', true, 22),
('기타수입', '#95A5A6', '💰', 'INCOME', true, 23)

ON CONFLICT (name, COALESCE(user_id, 0)) DO NOTHING;

-- ================================================================
-- 🎯 6단계: 권한 설정
-- ================================================================

-- 테이블 권한
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA unble_prod TO untab;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA unble_prod TO untab;

-- 향후 생성될 테이블에 대한 기본 권한
ALTER DEFAULT PRIVILEGES IN SCHEMA unble_prod GRANT ALL PRIVILEGES ON TABLES TO untab;
ALTER DEFAULT PRIVILEGES IN SCHEMA unble_prod GRANT ALL PRIVILEGES ON SEQUENCES TO untab;

-- ================================================================
-- ✅ 7단계: 최종 확인
-- ================================================================

-- 양쪽 스키마 테이블 개수 확인
SELECT 
    'unble_dev' as schema_name,
    COUNT(*) as table_count
FROM pg_tables 
WHERE schemaname = 'unble_dev'
UNION ALL
SELECT 
    'unble_prod' as schema_name,
    COUNT(*) as table_count
FROM pg_tables 
WHERE schemaname = 'unble_prod';

-- 각 스키마의 테이블 목록
SELECT 
    schemaname,
    tablename
FROM pg_tables 
WHERE schemaname IN ('unble_dev', 'unble_prod')
ORDER BY schemaname, tablename;

-- 기본 카테고리 개수 확인
SELECT 
    'unble_dev' as schema_name,
    COUNT(*) as category_count
FROM unble_dev.categories 
WHERE is_default = true
UNION ALL
SELECT 
    'unble_prod' as schema_name,
    COUNT(*) as category_count
FROM unble_prod.categories 
WHERE is_default = true;

COMMENT ON SCHEMA unble_dev IS 'Unble Budget App - Development Environment (Synced)';
COMMENT ON SCHEMA unble_prod IS 'Unble Budget App - Production Environment (Synced)';