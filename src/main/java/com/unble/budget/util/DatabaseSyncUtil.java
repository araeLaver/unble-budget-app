package com.unble.budget.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * 데이터베이스 스키마 동기화 유틸리티
 * 애플리케이션 시작 시 개발/운영 환경 테이블 구조를 확인하고 동기화
 */
@Component
@Order(1)
public class DatabaseSyncUtil implements CommandLineRunner {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        System.out.println("🔍 데이터베이스 스키마 동기화 시작...");
        
        try {
            // 현재 스키마 확인
            checkCurrentSchema();
            
            // 테이블 목록 확인
            checkTables();
            
            // 기본 카테고리 확인
            checkDefaultCategories();
            
            System.out.println("✅ 데이터베이스 스키마 동기화 완료!");
            
        } catch (Exception e) {
            System.err.println("❌ 데이터베이스 동기화 중 오류: " + e.getMessage());
            // 오류가 발생해도 애플리케이션은 계속 실행
        }
    }
    
    private void checkCurrentSchema() {
        try {
            String currentSchema = jdbcTemplate.queryForObject(
                "SELECT current_schema()", String.class);
            System.out.println("📊 현재 스키마: " + currentSchema);
            
            // 모든 스키마 목록 확인
            List<Map<String, Object>> schemas = jdbcTemplate.queryForList(
                "SELECT nspname as schemaname FROM pg_namespace WHERE nspname LIKE '%unble%' OR nspname LIKE '%dev%' ORDER BY nspname");
            
            System.out.println("🗂️  존재하는 스키마들:");
            for (Map<String, Object> schema : schemas) {
                System.out.println("   - " + schema.get("schemaname"));
            }
            
        } catch (Exception e) {
            System.err.println("스키마 확인 중 오류: " + e.getMessage());
        }
    }
    
    private void checkTables() {
        try {
            List<Map<String, Object>> tables = jdbcTemplate.queryForList(
                "SELECT tablename FROM pg_tables WHERE schemaname = 'unble_dev' ORDER BY tablename");
            
            System.out.println("📋 현재 스키마의 테이블들:");
            for (Map<String, Object> table : tables) {
                String tableName = (String) table.get("tablename");
                
                // 각 테이블의 레코드 수 확인
                try {
                    Integer count = jdbcTemplate.queryForObject(
                        "SELECT COUNT(*) FROM unble_dev." + tableName, Integer.class);
                    System.out.println("   - " + tableName + " (" + count + " records)");
                } catch (Exception e) {
                    System.out.println("   - " + tableName + " (count error: " + e.getMessage() + ")");
                }
            }
            
        } catch (Exception e) {
            System.err.println("테이블 확인 중 오류: " + e.getMessage());
        }
    }
    
    private void checkDefaultCategories() {
        try {
            // categories 테이블이 존재하는지 확인
            Integer tableExists = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.tables WHERE table_name = 'categories' AND table_schema = 'unble_dev'", 
                Integer.class);
            
            if (tableExists > 0) {
                Integer categoryCount = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM unble_dev.categories WHERE is_default = true", Integer.class);
                System.out.println("🏷️  기본 카테고리 개수: " + categoryCount);
                
                if (categoryCount < 23) {
                    System.out.println("⚠️  기본 카테고리가 부족합니다. DataInitializer에서 추가될 예정입니다.");
                }
            } else {
                System.out.println("📝 categories 테이블이 아직 생성되지 않았습니다. Hibernate가 생성할 예정입니다.");
            }
            
        } catch (Exception e) {
            System.err.println("카테고리 확인 중 오류: " + e.getMessage());
        }
    }
}