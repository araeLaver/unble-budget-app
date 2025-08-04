-- 즉시 실행용 ASSET 제약조건 수정 스크립트
-- PostgreSQL 클라이언트(pgAdmin, DBeaver 등)에서 직접 실행하세요

-- 1. 기존 제약조건 삭제
ALTER TABLE transactions DROP CONSTRAINT IF EXISTS transactions_transaction_type_check;

-- 2. ASSET을 포함한 새 제약조건 추가  
ALTER TABLE transactions ADD CONSTRAINT transactions_transaction_type_check 
CHECK (transaction_type IN ('INCOME', 'EXPENSE', 'ASSET'));

-- 3. 확인
SELECT 'ASSET 거래 타입이 성공적으로 추가되었습니다!' as status;

-- 4. 제약조건 확인
SELECT constraint_name, check_clause 
FROM information_schema.check_constraints 
WHERE constraint_name = 'transactions_transaction_type_check';