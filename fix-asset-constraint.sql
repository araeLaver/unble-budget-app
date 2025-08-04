-- 기존 제약조건 삭제하고 ASSET 포함한 새 제약조건 추가
ALTER TABLE transactions DROP CONSTRAINT IF EXISTS transactions_transaction_type_check;
ALTER TABLE transactions ADD CONSTRAINT transactions_transaction_type_check CHECK (transaction_type IN ('INCOME', 'EXPENSE', 'ASSET'));

-- 확인
SELECT 'ASSET 거래 타입 추가 완료!' as result;