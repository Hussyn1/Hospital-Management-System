package com.hospital.dao;

import com.hospital.models.StaffShift;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class HRDAO {

    public boolean saveShift(StaffShift shift) throws SQLException {
        String query = "INSERT INTO staff_shifts (user_id, shift_start, shift_end, role_assigned) VALUES (?, ?, ?, ?);";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, shift.getUserId());
            pstmt.setString(2, shift.getShiftStart().toString().replace("T", " "));
            pstmt.setString(3, shift.getShiftEnd().toString().replace("T", " "));
            pstmt.setString(4, shift.getRoleAssigned());
            
            int affected = pstmt.executeUpdate();
            if (affected > 0) {
                shift.setId(DBConnection.getLastInsertId(conn));
                return true;
            }
        }
        return false;
    }

    public boolean deleteShift(int shiftId) throws SQLException {
        String query = "DELETE FROM staff_shifts WHERE id = ?;";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, shiftId);
            return pstmt.executeUpdate() > 0;
        }
    }

    public List<StaffShift> getShiftsForUser(int userId) throws SQLException {
        List<StaffShift> list = new ArrayList<>();
        String query = "SELECT s.*, u.full_name AS staff_name FROM staff_shifts s " +
                       "JOIN users u ON s.user_id = u.id WHERE s.user_id = ? " +
                       "ORDER BY s.shift_start DESC;";
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

    public List<StaffShift> getAllShifts() throws SQLException {
        List<StaffShift> list = new ArrayList<>();
        String query = "SELECT s.*, u.full_name AS staff_name FROM staff_shifts s " +
                       "JOIN users u ON s.user_id = u.id ORDER BY s.shift_start DESC;";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    private StaffShift mapRow(ResultSet rs) throws SQLException {
        StaffShift s = new StaffShift();
        s.setId(rs.getInt("id"));
        s.setUserId(rs.getInt("user_id"));
        s.setRoleAssigned(rs.getString("role_assigned"));
        s.setStaffName(rs.getString("staff_name"));

        String startStr = rs.getString("shift_start").replace(" ", "T");
        String endStr = rs.getString("shift_end").replace(" ", "T");
        s.setShiftStart(LocalDateTime.parse(startStr));
        s.setShiftEnd(LocalDateTime.parse(endStr));
        return s;
    }
}
