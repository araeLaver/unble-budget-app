-- ================================================================
-- H2 Database - Initial Data (개발환경용)
-- ================================================================

-- 기본 카테고리 삽입
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
('기타', '#DDA0DD', '📝', 'EXPENSE', true, 99);