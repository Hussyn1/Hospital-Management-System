package com.hospital.services;

import com.hospital.dao.DBConnection;
import com.hospital.enums.Role;
import com.hospital.models.User;
import com.hospital.utils.HashHelper;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

public class AuthService {
    private static User currentUser;

    public static User getCurrentUser() {
        return currentUser;
    }

    public static void logout() {
        currentUser = null;
    }

    /**
     * Authenticates the user by verifying username and SHA-256 password hash.
     * @param username The login username
     * @param password The login raw text password
     * @return User object if successful, null if failed
     * @throws SQLException If database access error occurs
     */
    public boolean login(String username, String password) throws SQLException {
        String query = "SELECT * FROM users WHERE username = ?;";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String storedHash = rs.getString("password_hash");
                    String inputHash = HashHelper.hash(password);
                    
                    if (storedHash.equals(inputHash)) {
                        User user = new User();
                        user.setId(rs.getInt("id"));
                        user.setUsername(rs.getString("username"));
                        user.setFullName(rs.getString("full_name"));
                        user.setRole(Role.valueOf(rs.getString("role")));
                        user.setContact(rs.getString("contact"));
                        user.setEmail(rs.getString("email"));
                        
                        String createdStr = rs.getString("created_at");
                        if (createdStr != null) {
                            try {
                                createdStr = createdStr.replace(" ", "T");
                                if (!createdStr.contains("T")) {
                                    createdStr += "T00:00:00";
                                }
                                user.setCreatedAt(LocalDateTime.parse(createdStr));
                            } catch (Exception e) {
                                user.setCreatedAt(LocalDateTime.now());
                            }
                        }
                        
                        currentUser = user;
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
