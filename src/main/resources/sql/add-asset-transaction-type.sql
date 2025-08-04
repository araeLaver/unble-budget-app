-- ================================================================
-- 기존 데이터베이스에 ASSET 거래 타입 추가 마이그레이션 스크립트
-- ================================================================

-- 1. 기존 CHECK 제약조건 삭제
ALTER TABLE unble_dev.transactions DROP CONSTRAINT IF EXISTS transactions_transaction_type_check;
ALTER TABLE unble_prod.transactions DROP CONSTRAINT IF EXISTS transactions_transaction_type_check;

-- 2. ASSET을 포함한 새로운 CHECK 제약조건 추가
ALTER TABLE unble_dev.transactions 
ADD CONSTRAINT transactions_transaction_type_check 
CHECK (transaction_type IN ('INCOME', 'EXPENSE', 'ASSET'));

ALTER TABLE unble_prod.transactions 
ADD CONSTRAINT transactions_transaction_type_check 
CHECK (transaction_type IN ('INCOME', 'EXPENSE', 'ASSET'));

-- 확인용 쿼리
SELECT constraint_name, check_clause 
FROM information_schema.check_constraints 
WHERE constraint_name = 'transactions_transaction_type_check';

-- 성공 메시지
SELECT 'ASSET 거래 타입이 성공적으로 추가되었습니다!' as status;