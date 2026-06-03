package com.hospital.dao;

import com.hospital.models.Prescription;
import com.hospital.models.PrescriptionItem;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PrescriptionDAO {

    /**
     * Transactional save: inserts prescription header and its detail lines.
     */
    public boolean save(Prescription p) throws SQLException {
        String headerQuery = "INSERT INTO prescriptions (record_id, doctor_id, patient_id, status) VALUES (?, ?, ?, ?);";
        String itemQuery = "INSERT INTO prescription_items (prescription_id, medicine_id, dosage, frequency, quantity) VALUES (?, ?, ?, ?, ?);";

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); // Begin Transaction

            int prescriptionId;
            try (PreparedStatement hStmt = conn.prepareStatement(headerQuery)) {
                hStmt.setInt(1, p.getRecordId());
                hStmt.setInt(2, p.getDoctorId());
                hStmt.setString(3, p.getPatientId());
                hStmt.setString(4, p.getStatus());
                
                int affected = hStmt.executeUpdate();
                if (affected == 0) {
                    throw new SQLException("Creating prescription failed, no rows affected.");
                }
                prescriptionId = DBConnection.getLastInsertId(conn);
                p.setId(prescriptionId);
            }

            try (PreparedStatement iStmt = conn.prepareStatement(itemQuery)) {
                for (PrescriptionItem item : p.getItems()) {
                    iStmt.setInt(1, prescriptionId);
                    iStmt.setInt(2, item.getMedicineId());
                    iStmt.setString(3, item.getDosage());
                    iStmt.setString(4, item.getFrequency());
                    iStmt.setInt(5, item.getQuantity());
                    iStmt.addBatch();
                }
                iStmt.executeBatch();
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

    public boolean updateStatus(int id, String status) throws SQLException {
        String query = "UPDATE prescriptions SET status = ? WHERE id = ?;";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, status);
            pstmt.setInt(2, id);
            return pstmt.executeUpdate() > 0;
        }
    }

    public Prescription getById(int id) throws SQLException {
        String query = "SELECT pr.*, p.name AS patient_name, u.full_name AS doctor_name FROM prescriptions pr " +
                       "JOIN patients p ON pr.patient_id = p.id " +
                       "JOIN doctors d ON pr.doctor_id = d.id " +
                       "JOIN users u ON d.user_id = u.id " +
                       "WHERE pr.id = ?;";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Prescription p = mapRow(rs);
                    p.setItems(getItemsForPrescription(id));
                    return p;
                }
            }
        }
        return null;
    }

    public List<Prescription> getByPatientId(String patientId) throws SQLException {
        List<Prescription> list = new ArrayList<>();
        String query = "SELECT pr.*, p.name AS patient_name, u.full_name AS doctor_name FROM prescriptions pr " +
                       "JOIN patients p ON pr.patient_id = p.id " +
                       "JOIN doctors d ON pr.doctor_id = d.id " +
                       "JOIN users u ON d.user_id = u.id " +
                       "WHERE pr.patient_id = ? ORDER BY pr.created_at DESC;";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, patientId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Prescription p = mapRow(rs);
                    list.add(p);
                }
            }
        }
        // Fetch items for each prescription
        for (Prescription p : list) {
            p.setItems(getItemsForPrescription(p.getId()));
        }
        return list;
    }

    public List<Prescription> getAllPending() throws SQLException {
        List<Prescription> list = new ArrayList<>();
        String query = "SELECT pr.*, p.name AS patient_name, u.full_name AS doctor_name FROM prescriptions pr " +
                       "JOIN patients p ON pr.patient_id = p.id " +
                       "JOIN doctors d ON pr.doctor_id = d.id " +
                       "JOIN users u ON d.user_id = u.id " +
                       "WHERE pr.status = 'PENDING' ORDER BY pr.created_at DESC;";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        for (Prescription p : list) {
            p.setItems(getItemsForPrescription(p.getId()));
        }
        return list;
    }

    public List<Prescription> getAll() throws SQLException {
        List<Prescription> list = new ArrayList<>();
        String query = "SELECT pr.*, p.name AS patient_name, u.full_name AS doctor_name FROM prescriptions pr " +
                       "JOIN patients p ON pr.patient_id = p.id " +
                       "JOIN doctors d ON pr.doctor_id = d.id " +
                       "JOIN users u ON d.user_id = u.id " +
                       "ORDER BY pr.created_at DESC;";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        for (Prescription p : list) {
            p.setItems(getItemsForPrescription(p.getId()));
        }
        return list;
    }

    private List<PrescriptionItem> getItemsForPrescription(int prescriptionId) throws SQLException {
        List<PrescriptionItem> list = new ArrayList<>();
        String query = "SELECT pi.*, m.name AS medicine_name, m.generic_name, m.price FROM prescription_items pi " +
                       "JOIN medicines m ON pi.medicine_id = m.id WHERE pi.prescription_id = ?;";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, prescriptionId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    PrescriptionItem item = new PrescriptionItem();
                    item.setId(rs.getInt("id"));
                    item.setPrescriptionId(rs.getInt("prescription_id"));
                    item.setMedicineId(rs.getInt("medicine_id"));
                    item.setDosage(rs.getString("dosage"));
                    item.setFrequency(rs.getString("frequency"));
                    item.setQuantity(rs.getInt("quantity"));
                    
                    // Helpers
                    item.setMedicineName(rs.getString("medicine_name"));
                    item.setGenericName(rs.getString("generic_name"));
                    item.setPrice(rs.getDouble("price"));
                    
                    list.add(item);
                }
            }
        }
        return list;
    }

    private Prescription mapRow(ResultSet rs) throws SQLException {
        Prescription p = new Prescription();
        p.setId(rs.getInt("id"));
        p.setRecordId(rs.getInt("record_id"));
        p.setDoctorId(rs.getInt("doctor_id"));
        p.setPatientId(rs.getString("patient_id"));
        p.setStatus(rs.getString("status"));
        
        // Joined details
        p.setPatientName(rs.getString("patient_name"));
        p.setDoctorName(rs.getString("doctor_name"));

        String createdStr = rs.getString("created_at");
        if (createdStr != null) {
            try {
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
