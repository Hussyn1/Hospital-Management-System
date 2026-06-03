package com.hospital.dao;

import com.hospital.models.Notification;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class NotificationDAO {

    public boolean save(Notification notif) throws SQLException {
        String query = "INSERT INTO notifications (user_id, message, is_read) VALUES (?, ?, ?);";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, notif.getUserId());
            pstmt.setString(2, notif.getMessage());
            pstmt.setInt(3, notif.isRead() ? 1 : 0);
            
            int affected = pstmt.executeUpdate();
            if (affected > 0) {
                notif.setId(DBConnection.getLastInsertId(conn));
                return true;
            }
        }
        return false;
    }

    public boolean markAsRead(int id) throws SQLException {
        String query = "UPDATE notifications SET is_read = 1 WHERE id = ?;";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        }
    }

    public List<Notification> getByUserId(int userId) throws SQLException {
        List<Notification> list = new ArrayList<>();
        String query = "SELECT * FROM notifications WHERE user_id = ? ORDER BY created_at DESC;";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    public int getUnreadCount(int userId) throws SQLException {
        String query = "SELECT COUNT(*) FROM notifications WHERE user_id = ? AND is_read = 0;";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    private Notification mapRow(ResultSet rs) throws SQLException {
        Notification n = new Notification();
        n.setId(rs.getInt("id"));
        n.setUserId(rs.getInt("user_id"));
        n.setMessage(rs.getString("message"));
        n.setRead(rs.getInt("is_read") == 1);

        String createdStr = rs.getString("created_at");
        if (createdStr != null) {
            try {
                createdStr = createdStr.replace(" ", "T");
                if (!createdStr.contains("T")) {
                    createdStr += "T00:00:00";
                }
                n.setCreatedAt(LocalDateTime.parse(createdStr));
            } catch (Exception e) {
                n.setCreatedAt(LocalDateTime.now());
            }
        }
        return n;
    }
}
