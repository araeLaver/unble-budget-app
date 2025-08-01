package com.unble.budget.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Simple Database Schema Checker
 * Direct PostgreSQL connection to check and cleanup schemas
 */
public class SimpleDbChecker {
    
    private static final String DB_URL = "jdbc:postgresql://ep-blue-unit-a2ev3s9x.eu-central-1.pg.koyeb.app/untab?sslmode=require";
    private static final String USERNAME = "untab";
    private static final String PASSWORD = "0AbVNOIsl2dn";
    
    public static void main(String[] args) {
        System.out.println("=== Database Schema Checker ===");
        
        try {
            Class.forName("org.postgresql.Driver");
            
            try (Connection conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD)) {
                System.out.println("✓ Database connection successful!");
                
                // Check existing schemas
                checkSchemas(conn);
                
                // Cleanup unnecessary schemas
                cleanupSchemas(conn);
                
                // Create production schema
                createProdSchema(conn);
                
                System.out.println("✓ Database cleanup completed!");
                
            }
        } catch (Exception e) {
            System.err.println("✗ Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void checkSchemas(Connection conn) throws Exception {
        System.out.println("\n--- Current Schemas ---");
        
        String sql = "SELECT schemaname FROM pg_namespace n " +
                    "WHERE nspname LIKE '%unble%' OR nspname LIKE '%dev%' OR nspname LIKE '%budget%' " +
                    "ORDER BY schemaname";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                String schemaName = rs.getString("schemaname");
                System.out.println("  - " + schemaName);
            }
        }
    }
    
    private static void cleanupSchemas(Connection conn) throws Exception {
        System.out.println("\n--- Cleaning up unnecessary schemas ---");
        
        // Remove dev_schema if exists
        if (schemaExists(conn, "dev_schema")) {
            String sql = "DROP SCHEMA IF EXISTS dev_schema CASCADE";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.executeUpdate();
                System.out.println("✓ Removed dev_schema");
            }
        } else {
            System.out.println("- dev_schema not found");
        }
        
        // Remove unble_budget_dev if exists
        if (schemaExists(conn, "unble_budget_dev")) {
            String sql = "DROP SCHEMA IF EXISTS unble_budget_dev CASCADE";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.executeUpdate();
                System.out.println("✓ Removed unble_budget_dev");
            }
        } else {
            System.out.println("- unble_budget_dev not found");
        }
    }
    
    private static void createProdSchema(Connection conn) throws Exception {
        System.out.println("\n--- Creating production schema ---");
        
        // Create unble_prod schema
        String createSchema = "CREATE SCHEMA IF NOT EXISTS unble_prod";
        try (PreparedStatement stmt = conn.prepareStatement(createSchema)) {
            stmt.executeUpdate();
            System.out.println("✓ Created unble_prod schema");
        }
        
        // Grant privileges
        String grantPrivileges = "GRANT ALL PRIVILEGES ON SCHEMA unble_prod TO untab";
        try (PreparedStatement stmt = conn.prepareStatement(grantPrivileges)) {
            stmt.executeUpdate();
            System.out.println("✓ Granted privileges to untab user");
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
    
    private static void checkFinalState(Connection conn) throws Exception {
        System.out.println("\n--- Final Schema State ---");
        
        String sql = "SELECT schemaname FROM pg_namespace n " +
                    "WHERE nspname IN ('unble_dev', 'unble_prod') " +
                    "ORDER BY schemaname";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                String schemaName = rs.getString("schemaname");
                System.out.println("  ✓ " + schemaName + " (final)");
            }
        }
    }
}