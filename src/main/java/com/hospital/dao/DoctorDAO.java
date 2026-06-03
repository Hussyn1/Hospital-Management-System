package com.hospital.dao;

import com.hospital.models.Doctor;
import com.hospital.enums.Role;
import com.hospital.utils.HashHelper;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class DoctorDAO {

    /**
     * Transactional save: creates user record first, then doctors record.
     */
    public boolean save(Doctor doctor, String rawPassword, String username, String email) throws SQLException {
        String userQuery = "INSERT INTO users (username, password_hash, role, full_name, contact, email) VALUES (?, ?, ?, ?, ?, ?);";
        String docQuery = "INSERT INTO doctors (user_id, specialization, license_number, consultation_fee, availability_schedule, department) VALUES (?, ?, ?, ?, ?, ?);";
        
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); // Begin Transaction

            int userId;
            try (PreparedStatement uStmt = conn.prepareStatement(userQuery)) {
                uStmt.setString(1, username);
                uStmt.setString(2, HashHelper.hash(rawPassword));
                uStmt.setString(3, Role.DOCTOR.name());
                uStmt.setString(4, doctor.getDoctorName());
                uStmt.setString(5, doctor.getContact());
                uStmt.setString(6, email);
                
                int affected = uStmt.executeUpdate();
                if (affected == 0) {
                    throw new SQLException("Creating user failed, no rows affected.");
                }
                userId = DBConnection.getLastInsertId(conn);
            }

            try (PreparedStatement dStmt = conn.prepareStatement(docQuery)) {
                dStmt.setInt(1, userId);
                dStmt.setString(2, doctor.getSpecialization());
                dStmt.setString(3, doctor.getLicenseNumber());
                dStmt.setDouble(4, doctor.getConsultationFee());
                dStmt.setString(5, doctor.getAvailabilitySchedule());
                dStmt.setString(6, doctor.getDepartment());
                
                dStmt.executeUpdate();
            }

            conn.commit(); // Commit Transaction
            return true;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback(); // Rollback Transaction on error
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            throw e;
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }

    /**
     * Transactional update: updates users table and doctors table.
     */
    public boolean update(Doctor doctor, String email) throws SQLException {
        String userQuery = "UPDATE users SET full_name = ?, contact = ?, email = ? WHERE id = ?;";
        String docQuery = "UPDATE doctors SET specialization = ?, license_number = ?, consultation_fee = ?, availability_schedule = ?, department = ? WHERE id = ?;";
        
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); // Begin Transaction

            // 1. Update user info
            try (PreparedStatement uStmt = conn.prepareStatement(userQuery)) {
                uStmt.setString(1, doctor.getDoctorName());
                uStmt.setString(2, doctor.getContact());
                uStmt.setString(3, email);
                uStmt.setInt(4, doctor.getUserId());
                uStmt.executeUpdate();
            }

            // 2. Update doctor info
            try (PreparedStatement dStmt = conn.prepareStatement(docQuery)) {
                dStmt.setString(1, doctor.getSpecialization());
                dStmt.setString(2, doctor.getLicenseNumber());
                dStmt.setDouble(3, doctor.getConsultationFee());
                dStmt.setString(4, doctor.getAvailabilitySchedule());
                dStmt.setString(5, doctor.getDepartment());
                dStmt.setInt(6, doctor.getId());
                dStmt.executeUpdate();
            }

            conn.commit(); // Commit Transaction
            return true;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            throw e;
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }

    public Doctor getById(int id) throws SQLException {
        String query = "SELECT d.*, u.full_name, u.contact, u.email FROM doctors d " +
                       "JOIN users u ON d.user_id = u.id WHERE d.id = ?;";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    public Doctor getByUserId(int userId) throws SQLException {
        String query = "SELECT d.*, u.full_name, u.contact, u.email FROM doctors d " +
                       "JOIN users u ON d.user_id = u.id WHERE d.user_id = ?;";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    public List<Doctor> getAll() throws SQLException {
        List<Doctor> list = new ArrayList<>();
        String query = "SELECT d.*, u.full_name, u.contact, u.email FROM doctors d " +
                       "JOIN users u ON d.user_id = u.id ORDER BY u.full_name ASC;";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public List<Doctor> search(String keyword) throws SQLException {
        List<Doctor> list = new ArrayList<>();
        String query = "SELECT d.*, u.full_name, u.contact, u.email FROM doctors d " +
                       "JOIN users u ON d.user_id = u.id " +
                       "WHERE u.full_name LIKE ? OR d.specialization LIKE ? OR d.department LIKE ? " +
                       "ORDER BY u.full_name ASC;";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            String wildcard = "%" + keyword + "%";
            pstmt.setString(1, wildcard);
            pstmt.setString(2, wildcard);
            pstmt.setString(3, wildcard);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    public List<Doctor> getBySpecialty(String specialty) throws SQLException {
        List<Doctor> list = new ArrayList<>();
        String query = "SELECT d.*, u.full_name, u.contact, u.email FROM doctors d " +
                       "JOIN users u ON d.user_id = u.id WHERE d.specialization = ? " +
                       "ORDER BY u.full_name ASC;";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, specialty);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    private Doctor mapRow(ResultSet rs) throws SQLException {
        Doctor d = new Doctor();
        d.setId(rs.getInt("id"));
        d.setUserId(rs.getInt("user_id"));
        d.setSpecialization(rs.getString("specialization"));
        d.setLicenseNumber(rs.getString("license_number"));
        d.setConsultationFee(rs.getDouble("consultation_fee"));
        d.setAvailabilitySchedule(rs.getString("availability_schedule"));
        d.setDepartment(rs.getString("department"));
        
        // Joined fields
        d.setDoctorName(rs.getString("full_name"));
        d.setContact(rs.getString("contact"));
        d.setEmail(rs.getString("email"));
        return d;
    }
}
