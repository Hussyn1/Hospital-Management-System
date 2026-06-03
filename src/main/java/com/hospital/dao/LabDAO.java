package com.hospital.dao;

import com.hospital.models.LabRequest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class LabDAO {

    public boolean save(LabRequest req) throws SQLException {
        String query = "INSERT INTO lab_requests (record_id, patient_id, test_name, requested_date, flag_abnormal, status) VALUES (?, ?, ?, ?, ?, ?);";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            if (req.getRecordId() != null) {
                pstmt.setInt(1, req.getRecordId());
            } else {
                pstmt.setNull(1, java.sql.Types.INTEGER);
            }
            pstmt.setString(2, req.getPatientId());
            pstmt.setString(3, req.getTestName());
            pstmt.setString(4, req.getRequestedDate().toString());
            pstmt.setInt(5, req.isFlagAbnormal() ? 1 : 0);
            pstmt.setString(6, req.getStatus());
            
            int affected = pstmt.executeUpdate();
            if (affected > 0) {
                req.setId(DBConnection.getLastInsertId(conn));
                return true;
            }
        }
        return false;
    }

    public boolean updateResults(int id, String resultText, boolean flagAbnormal, String reportFilePath) throws SQLException {
        String query = "UPDATE lab_requests SET result_text = ?, flag_abnormal = ?, status = 'COMPLETED', report_file_path = ? WHERE id = ?;";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, resultText);
            pstmt.setInt(2, flagAbnormal ? 1 : 0);
            pstmt.setString(3, reportFilePath);
            pstmt.setInt(4, id);
            return pstmt.executeUpdate() > 0;
        }
    }

    public LabRequest getById(int id) throws SQLException {
        String query = "SELECT lr.*, p.name AS patient_name, u.full_name AS doctor_name FROM lab_requests lr " +
                       "JOIN patients p ON lr.patient_id = p.id " +
                       "LEFT JOIN medical_records mr ON lr.record_id = mr.id " +
                       "LEFT JOIN doctors d ON mr.doctor_id = d.id " +
                       "LEFT JOIN users u ON d.user_id = u.id " +
                       "WHERE lr.id = ?;";
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

    public List<LabRequest> getByPatientId(String patientId) throws SQLException {
        List<LabRequest> list = new ArrayList<>();
        String query = "SELECT lr.*, p.name AS patient_name, u.full_name AS doctor_name FROM lab_requests lr " +
                       "JOIN patients p ON lr.patient_id = p.id " +
                       "LEFT JOIN medical_records mr ON lr.record_id = mr.id " +
                       "LEFT JOIN doctors d ON mr.doctor_id = d.id " +
                       "LEFT JOIN users u ON d.user_id = u.id " +
                       "WHERE lr.patient_id = ? ORDER BY lr.requested_date DESC;";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, patientId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    public List<LabRequest> getAllPending() throws SQLException {
        List<LabRequest> list = new ArrayList<>();
        String query = "SELECT lr.*, p.name AS patient_name, u.full_name AS doctor_name FROM lab_requests lr " +
                       "JOIN patients p ON lr.patient_id = p.id " +
                       "LEFT JOIN medical_records mr ON lr.record_id = mr.id " +
                       "LEFT JOIN doctors d ON mr.doctor_id = d.id " +
                       "LEFT JOIN users u ON d.user_id = u.id " +
                       "WHERE lr.status = 'PENDING' ORDER BY lr.requested_date DESC;";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public List<LabRequest> getAll() throws SQLException {
        List<LabRequest> list = new ArrayList<>();
        String query = "SELECT lr.*, p.name AS patient_name, u.full_name AS doctor_name FROM lab_requests lr " +
                       "JOIN patients p ON lr.patient_id = p.id " +
                       "LEFT JOIN medical_records mr ON lr.record_id = mr.id " +
                       "LEFT JOIN doctors d ON mr.doctor_id = d.id " +
                       "LEFT JOIN users u ON d.user_id = u.id " +
                       "ORDER BY lr.requested_date DESC;";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    private LabRequest mapRow(ResultSet rs) throws SQLException {
        LabRequest lr = new LabRequest();
        lr.setId(rs.getInt("id"));
        
        int recordId = rs.getInt("record_id");
        lr.setRecordId(rs.wasNull() ? null : recordId);
        
        lr.setPatientId(rs.getString("patient_id"));
        lr.setTestName(rs.getString("test_name"));
        lr.setRequestedDate(LocalDate.parse(rs.getString("requested_date")));
        lr.setResultText(rs.getString("result_text"));
        lr.setFlagAbnormal(rs.getInt("flag_abnormal") == 1);
        lr.setStatus(rs.getString("status"));
        lr.setReportFilePath(rs.getString("report_file_path"));
        
        // Joined details
        lr.setPatientName(rs.getString("patient_name"));
        
        String doctorName = rs.getString("doctor_name");
        lr.setDoctorName(rs.wasNull() ? "Direct Order" : doctorName);
        return lr;
    }
}
