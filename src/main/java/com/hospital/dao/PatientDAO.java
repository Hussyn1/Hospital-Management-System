package com.hospital.dao;

import com.hospital.models.Patient;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PatientDAO {

    /**
     * Generates a sequential patient ID (e.g. PT-10001, PT-10002).
     */
    public synchronized String getNextPatientId() {
        String query = "SELECT id FROM patients ORDER BY id DESC LIMIT 1;";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            if (rs.next()) {
                String lastId = rs.getString(1);
                if (lastId != null && lastId.startsWith("PT-")) {
                    try {
                        int numericPart = Integer.parseInt(lastId.substring(3));
                        return "PT-" + (numericPart + 1);
                    } catch (NumberFormatException e) {
                        // Fallback in case of parsing anomaly
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "PT-10001"; // Default start ID
    }

    public boolean save(Patient patient) throws SQLException {
        String query = "INSERT INTO patients (id, name, dob, blood_group, contact, emergency_contact, medical_history, status, is_deleted) VALUES (?, ?, ?, ?, ?, ?, ?, ?, 0);";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, patient.getId());
            pstmt.setString(2, patient.getName());
            pstmt.setString(3, patient.getDob().toString());
            pstmt.setString(4, patient.getBloodGroup());
            pstmt.setString(5, patient.getContact());
            pstmt.setString(6, patient.getEmergencyContact());
            pstmt.setString(7, patient.getMedicalHistory());
            pstmt.setString(8, patient.getStatus());
            return pstmt.executeUpdate() > 0;
        }
    }

    public boolean update(Patient patient) throws SQLException {
        String query = "UPDATE patients SET name = ?, dob = ?, blood_group = ?, contact = ?, emergency_contact = ?, medical_history = ?, status = ? WHERE id = ? AND is_deleted = 0;";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, patient.getName());
            pstmt.setString(2, patient.getDob().toString());
            pstmt.setString(3, patient.getBloodGroup());
            pstmt.setString(4, patient.getContact());
            pstmt.setString(5, patient.getEmergencyContact());
            pstmt.setString(6, patient.getMedicalHistory());
            pstmt.setString(7, patient.getStatus());
            pstmt.setString(8, patient.getId());
            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * Soft delete patient.
     */
    public boolean delete(String id) throws SQLException {
        String query = "UPDATE patients SET is_deleted = 1 WHERE id = ?;";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, id);
            return pstmt.executeUpdate() > 0;
        }
    }

    public Patient getById(String id) throws SQLException {
        String query = "SELECT * FROM patients WHERE id = ? AND is_deleted = 0;";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    public List<Patient> getAllActive() throws SQLException {
        List<Patient> list = new ArrayList<>();
        String query = "SELECT * FROM patients WHERE is_deleted = 0 ORDER BY created_at DESC;";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public List<Patient> searchActive(String keyword) throws SQLException {
        List<Patient> list = new ArrayList<>();
        String query = "SELECT * FROM patients WHERE is_deleted = 0 AND (name LIKE ? OR id LIKE ? OR contact LIKE ? OR blood_group LIKE ?) ORDER BY created_at DESC;";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            String wildcard = "%" + keyword + "%";
            pstmt.setString(1, wildcard);
            pstmt.setString(2, wildcard);
            pstmt.setString(3, wildcard);
            pstmt.setString(4, wildcard);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    private Patient mapRow(ResultSet rs) throws SQLException {
        Patient p = new Patient();
        p.setId(rs.getString("id"));
        p.setName(rs.getString("name"));
        p.setDob(LocalDate.parse(rs.getString("dob")));
        p.setBloodGroup(rs.getString("blood_group"));
        p.setContact(rs.getString("contact"));
        p.setEmergencyContact(rs.getString("emergency_contact"));
        p.setMedicalHistory(rs.getString("medical_history"));
        p.setStatus(rs.getString("status"));
        p.setDeleted(rs.getInt("is_deleted") == 1);
        
        // Handle SQLite timestamp parsing
        String createdStr = rs.getString("created_at");
        if (createdStr != null) {
            try {
                // SQLite timestamps can be in yyyy-MM-dd HH:mm:ss or ISO format
                createdStr = createdStr.replace(" ", "T");
                if (!createdStr.contains("T")) {
                    createdStr += "T00:00:00";
                }
                p.setCreatedAt(LocalDateTime.parse(createdStr));
            } catch (Exception e) {
                p.setCreatedAt(LocalDateTime.now());
            }
        }
        return p;
    }
}
