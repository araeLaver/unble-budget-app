package com.unble.budget.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * 데이터베이스 제약조건 업데이터
 * ASSET 거래 타입을 지원하도록 CHECK 제약조건을 업데이트합니다.
 */
@Component
public class DatabaseConstraintUpdater implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseConstraintUpdater.class);
    
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) throws Exception {
        logger.info("=== 데이터베이스 제약조건 업데이트 시작 ===");
        
        try {
            // 1. 기존 제약조건 삭제
            logger.info("기존 transaction_type CHECK 제약조건 삭제 중...");
            jdbcTemplate.execute("ALTER TABLE transactions DROP CONSTRAINT IF EXISTS transactions_transaction_type_check");
            
            // 2. ASSET을 포함한 새 제약조건 추가
            logger.info("ASSET을 포함한 새로운 CHECK 제약조건 추가 중...");
            jdbcTemplate.execute("ALTER TABLE transactions ADD CONSTRAINT transactions_transaction_type_check CHECK (transaction_type IN ('INCOME', 'EXPENSE', 'ASSET'))");
            
            // 3. 제약조건 확인
            String result = jdbcTemplate.queryForObject(
                "SELECT constraint_name FROM information_schema.check_constraints WHERE constraint_name = 'transactions_transaction_type_check'", 
                String.class
            );
            
            if (result != null) {
                logger.info("✅ ASSET 거래 타입 제약조건이 성공적으로 적용되었습니다!");
                logger.info("이제 자산 관리 기능을 사용할 수 있습니다.");
            } else {
                logger.warn("⚠️ 제약조건 적용 상태를 확인할 수 없습니다.");
            }
            
        } catch (Exception e) {
            logger.error("❌ 데이터베이스 제약조건 업데이트 실패: {}", e.getMessage());
            logger.info("📝 수동으로 다음 SQL을 실행해주세요:");
            logger.info("ALTER TABLE transactions DROP CONSTRAINT IF EXISTS transactions_transaction_type_check;");
            logger.info("ALTER TABLE transactions ADD CONSTRAINT transactions_transaction_type_check CHECK (transaction_type IN ('INCOME', 'EXPENSE', 'ASSET'));");
        }
        
        logger.info("=== 데이터베이스 제약조건 업데이트 완료 ===");
    }
}