package org.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

class DatabaseManager {
    private Connection conn;

    public DatabaseManager() throws SQLException {
        conn = DriverManager.getConnection("jdbc:h2:mem:test", "sa", "");
        createTables();
    }

    private void createTables() throws SQLException {
        String[] sqlStatements = {
                "CREATE TABLE IF NOT EXISTS Applicants (id INT PRIMARY KEY AUTO_INCREMENT, name VARCHAR(255), email VARCHAR(255))",
                "CREATE TABLE IF NOT EXISTS Education (id INT PRIMARY KEY AUTO_INCREMENT, university VARCHAR(100), degree VARCHAR(100))",
                "CREATE TABLE IF NOT EXISTS Experience (id INT PRIMARY KEY AUTO_INCREMENT, company VARCHAR(100), position VARCHAR(100))",
                "CREATE TABLE IF NOT EXISTS Companies (id INT PRIMARY KEY AUTO_INCREMENT, company_name VARCHAR(100))",
                "CREATE TABLE IF NOT EXISTS Results (id INT PRIMARY KEY AUTO_INCREMENT, applicant_id INT, key_skills VARCHAR(255))"
        };

        try (Statement stmt = conn.createStatement()) {
            for (String sql : sqlStatements) {
                stmt.execute(sql);
            }
        }
    }

    public Connection getConnection() {
        return conn;
    }
}