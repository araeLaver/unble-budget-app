-- ================================================================
-- Unble Budget App - 초기 데이터 삽입
-- ================================================================

-- 기본 카테고리 삽입 (시스템 기본 - user_id는 NULL)
INSERT INTO categories (name, color, icon, category_type, is_default, sort_order) VALUES

-- 자산 관리 카테고리
('현금', '#2ECC71', '💰', 'ASSET', true, 1),
('은행예금', '#3498DB', '🏦', 'ASSET', true, 2),
('적금', '#9B59B6', '💎', 'ASSET', true, 3),
('투자', '#E67E22', '📈', 'ASSET', true, 4),
('카드잔액', '#E74C3C', '💳', 'ASSET', true, 5),

-- 지출 카테고리
('식비', '#FF6B6B', '🍽️', 'EXPENSE', true, 10),
('교통비', '#4ECDC4', '🚗', 'EXPENSE', true, 11),
('생활용품', '#45B7D1', '🛒', 'EXPENSE', true, 12),
('의료비', '#96CEB4', '🏥', 'EXPENSE', true, 13),
('문화/여가', '#FFEAA7', '🎬', 'EXPENSE', true, 14),
('주거비', '#FF7675', '🏠', 'EXPENSE', true, 15),
('교육비', '#6C5CE7', '📚', 'EXPENSE', true, 16),
('의류/미용', '#FD79A8', '👕', 'EXPENSE', true, 17),
('통신비', '#00B894', '📱', 'EXPENSE', true, 18),
('공과금', '#FDCB6E', '⚡', 'EXPENSE', true, 19),
('보험료', '#A29BFE', '🛡️', 'EXPENSE', true, 20),
('세금', '#636E72', '🏛️', 'EXPENSE', true, 21),

-- 수입 카테고리
('급여', '#27AE60', '💼', 'INCOME', true, 30),
('용돈', '#E17055', '💵', 'INCOME', true, 31),
('투자수익', '#00B894', '📊', 'INCOME', true, 32),
('선물/보너스', '#FD79A8', '🎁', 'INCOME', true, 33),
('부업소득', '#6C5CE7', '💰', 'INCOME', true, 34),
('이자수익', '#00CEC9', '💹', 'INCOME', true, 35),

-- 기타
('기타', '#DDA0DD', '📝', 'EXPENSE', true, 99)

ON CONFLICT (name, category_type, is_default) WHERE is_default = true DO NOTHING;

-- ================================================================
-- 샘플 사용자 데이터 (개발용 - 운영에서는 제거)
-- ================================================================

-- 테스트 사용자 1
INSERT INTO users (email, password, name) VALUES 
('test@unble.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z2EuHsVpNMzM.ppmhVIHhOGG', '테스트 사용자')
ON CONFLICT (email) DO NOTHING;

-- 테스트 사용자 설정
INSERT INTO user_settings (user_id, currency, theme) 
SELECT u.id, 'KRW', 'light' 
FROM users u 
WHERE u.email = 'test@unble.com'
ON CONFLICT (user_id) DO NOTHING;

-- ================================================================
-- 유용한 함수들
-- ================================================================

-- 사용자별 월간 통계 함수
CREATE OR REPLACE FUNCTION get_monthly_stats(p_user_id BIGINT, p_year INT, p_month INT)
RETURNS TABLE (
    total_income DECIMAL(15,2),
    total_expense DECIMAL(15,2),
    net_amount DECIMAL(15,2),
    transaction_count BIGINT
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        COALESCE(SUM(CASE WHEN t.transaction_type = 'INCOME' THEN t.amount ELSE 0 END), 0) as total_income,
        COALESCE(SUM(CASE WHEN t.transaction_type = 'EXPENSE' THEN t.amount ELSE 0 END), 0) as total_expense,
        COALESCE(SUM(CASE WHEN t.transaction_type = 'INCOME' THEN t.amount ELSE -t.amount END), 0) as net_amount,
        COUNT(*) as transaction_count
    FROM transactions t
    WHERE t.user_id = p_user_id
    AND EXTRACT(YEAR FROM t.transaction_date) = p_year
    AND EXTRACT(MONTH FROM t.transaction_date) = p_month;
END;
$$ LANGUAGE plpgsql;

-- 카테고리별 지출 비율 함수
CREATE OR REPLACE FUNCTION get_expense_breakdown(p_user_id BIGINT, p_start_date DATE, p_end_date DATE)
RETURNS TABLE (
    category_name VARCHAR(100),
    category_icon VARCHAR(50),
    category_color VARCHAR(7),
    total_amount DECIMAL(15,2),
    percentage DECIMAL(5,2)
) AS $$
BEGIN
    RETURN QUERY
    WITH total_expense AS (
        SELECT SUM(amount) as total
        FROM transactions 
        WHERE user_id = p_user_id 
        AND transaction_type = 'EXPENSE'
        AND transaction_date BETWEEN p_start_date AND p_end_date
    )
    SELECT 
        c.name as category_name,
        c.icon as category_icon,
        c.color as category_color,
        SUM(t.amount) as total_amount,
        ROUND((SUM(t.amount) * 100.0 / te.total), 2) as percentage
    FROM transactions t
    JOIN categories c ON t.category_id = c.id
    CROSS JOIN total_expense te
    WHERE t.user_id = p_user_id
    AND t.transaction_type = 'EXPENSE'
    AND t.transaction_date BETWEEN p_start_date AND p_end_date
    GROUP BY c.id, c.name, c.icon, c.color, te.total
    ORDER BY total_amount DESC;
END;
$$ LANGUAGE plpgsql;

-- ================================================================
-- 데이터 무결성 검증 쿼리
-- ================================================================

-- 중복 이메일 검증
CREATE OR REPLACE FUNCTION check_duplicate_emails()
RETURNS TABLE (email VARCHAR(255), count_duplicate BIGINT) AS $$
BEGIN
    RETURN QUERY
    SELECT u.email, COUNT(*) as count_duplicate
    FROM users u
    GROUP BY u.email
    HAVING COUNT(*) > 1;
END;
$$ LANGUAGE plpgsql;

-- 고아 거래 검증 (카테고리가 삭제된 거래)
CREATE OR REPLACE FUNCTION check_orphan_transactions()
RETURNS TABLE (transaction_id BIGINT, user_email VARCHAR(255), amount DECIMAL(15,2)) AS $$
BEGIN
    RETURN QUERY
    SELECT t.id, u.email, t.amount
    FROM transactions t
    JOIN users u ON t.user_id = u.id
    WHERE t.category_id IS NOT NULL 
    AND NOT EXISTS (SELECT 1 FROM categories c WHERE c.id = t.category_id);
END;
$$ LANGUAGE plpgsql;