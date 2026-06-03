package com.hospital.dao;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class DBConnectionTest {

    @Test
    public void testConnectionAndSeeding() {
        try (Connection conn = DBConnection.getConnection()) {
            assertNotNull(conn, "Database connection should not be null.");

            try (Statement stmt = conn.createStatement()) {
                // Verify users table exists and has mock data
                try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM users;")) {
                    assertTrue(rs.next());
                    int count = rs.getInt(1);
                    assertTrue(count > 0, "Users table should have pre-populated mock records.");
                    System.out.println("Test: Found " + count + " users in the database.");
                }

                // Verify medicines table has mock data
                try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM medicines;")) {
                    assertTrue(rs.next());
                    int count = rs.getInt(1);
                    assertTrue(count > 0, "Medicines table should have pre-populated mock records.");
                    System.out.println("Test: Found " + count + " medicines in the database.");
                }

                // Verify wards table has mock data
                try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM wards;")) {
                    assertTrue(rs.next());
                    int count = rs.getInt(1);
                    assertTrue(count > 0, "Wards table should have pre-populated mock records.");
                    System.out.println("Test: Found " + count + " wards in the database.");
                }
            }
        } catch (Exception e) {
            fail("Database connection or schema validation threw an exception: " + e.getMessage());
        }
    }
}
