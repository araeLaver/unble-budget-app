package com.unble.budget.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * Database Schema Checker Utility
 * Direct PostgreSQL connection to check and cleanup schemas
 */
public class DatabaseChecker {
    
    private static final String DB_URL = "jdbc:postgresql://ep-blue-unit-a2ev3s9x.eu-central-1.pg.koyeb.app/untab?sslmode=require";
    private static final String USERNAME = "untab";
    private static final String PASSWORD = "0AbVNOIsl2dn";
    
    public static void main(String[] args) {
        System.out.println("Database connection and schema check starting...");
        
        try {
            Class.forName("org.postgresql.Driver");
            
            try (Connection conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD)) {
                System.out.println("✅ 데이터베이스 연결 성공!");
                
                // 1. 현재 존재하는 스키마들 확인
                checkExistingSchemas(conn);
                
                // 2. 각 스키마의 테이블 확인
                checkSchemaTables(conn);
                
                // 3. 불필요한 스키마 정리 실행
                if (shouldCleanupSchemas()) {
                    cleanupUnnecessarySchemas(conn);
                }
                
                // 4. 운영 스키마 생성 및 동기화
                syncProductionSchema(conn);
                
                System.out.println("🎯 데이터베이스 정리 및 동기화 완료!");
                
            }
        } catch (Exception e) {
            System.err.println("❌ 오류 발생: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void checkExistingSchemas(Connection conn) throws Exception {
        System.out.println("\n📊 현재 존재하는 스키마들:");
        
        String sql = "SELECT schemaname, schemaowner FROM pg_namespace n " +
                    "LEFT JOIN pg_user u ON n.nspowner = u.usesysid " +
                    "WHERE nspname LIKE '%unble%' OR nspname LIKE '%dev%' OR nspname LIKE '%budget%' " +
                    "ORDER BY schemaname";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                String schemaName = rs.getString("schemaname");
                String owner = rs.getString("schemaowner");
                System.out.println("   - " + schemaName + " (owner: " + owner + ")");
            }
        }
    }
    
    private static void checkSchemaTables(Connection conn) throws Exception {
        System.out.println("\n📋 각 스키마의 테이블 현황:");
        
        String[] schemas = {"dev_schema", "unble_budget_dev", "unble_dev", "unble_prod"};
        
        for (String schema : schemas) {
            if (schemaExists(conn, schema)) {
                System.out.println("\n🗂️  " + schema + " 스키마:");
                
                String sql = "SELECT tablename FROM pg_tables WHERE schemaname = ? ORDER BY tablename";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, schema);
                    try (ResultSet rs = stmt.executeQuery()) {
                        while (rs.next()) {
                            String tableName = rs.getString("tablename");
                            int recordCount = getTableRecordCount(conn, schema, tableName);
                            System.out.println("     - " + tableName + " (" + recordCount + " records)");
                        }
                    }
                }
            }
        }
    }
    
    private static boolean schemaExists(Connection conn, String schemaName) throws Exception {
        String sql = "SELECT COUNT(*) FROM pg_namespace WHERE nspname = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, schemaName);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }
    
    private static int getTableRecordCount(Connection conn, String schema, String table) {
        try {
            String sql = "SELECT COUNT(*) FROM " + schema + "." + table;
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (Exception e) {
            return -1; // 오류 시 -1 반환
        }
    }
    
    private static boolean shouldCleanupSchemas() {
        System.out.println("\n⚠️  불필요한 스키마를 정리하시겠습니까?");
        System.out.println("   삭제 대상: dev_schema, unble_budget_dev");
        System.out.println("   보존 대상: unble_dev, unble_prod");
        System.out.println("   (y/n): ");
        
        // 자동으로 y 반환 (배치 실행을 위해)
        System.out.println("y (자동 승인)");
        return true;
    }
    
    private static void cleanupUnnecessarySchemas(Connection conn) throws Exception {
        System.out.println("\n🗑️  불필요한 스키마 정리 중...");
        
        // dev_schema 삭제
        if (schemaExists(conn, "dev_schema")) {
            String sql = "DROP SCHEMA IF EXISTS dev_schema CASCADE";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.executeUpdate();
                System.out.println("   ✅ dev_schema 삭제 완료");
            }
        }
        
        // unble_budget_dev 삭제
        if (schemaExists(conn, "unble_budget_dev")) {
            String sql = "DROP SCHEMA IF EXISTS unble_budget_dev CASCADE";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.executeUpdate();
                System.out.println("   ✅ unble_budget_dev 삭제 완료");
            }
        }
    }
    
    private static void syncProductionSchema(Connection conn) throws Exception {
        System.out.println("\n🚀 운영 스키마 동기화 중...");
        
        // unble_prod 스키마 생성
        String createSchema = "CREATE SCHEMA IF NOT EXISTS unble_prod";
        try (PreparedStatement stmt = conn.prepareStatement(createSchema)) {
            stmt.executeUpdate();
            System.out.println("   ✅ unble_prod 스키마 생성 완료");
        }
        
        // 권한 부여
        String grantPrivileges = "GRANT ALL PRIVILEGES ON SCHEMA unble_prod TO untab";
        try (PreparedStatement stmt = conn.prepareStatement(grantPrivileges)) {
            stmt.executeUpdate();
            System.out.println("   ✅ 권한 설정 완료");
        }
        
        System.out.println("   📝 참고: 테이블 구조는 Spring Boot 애플리케이션에서 자동 생성됩니다.");
    }
}