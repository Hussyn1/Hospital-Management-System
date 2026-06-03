package com.hospital.dao;

import com.hospital.enums.TriageLevel;
import com.hospital.models.EmergencyPatient;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class EmergencyDAO {

    public boolean save(EmergencyPatient ep) throws SQLException {
        String query = "INSERT INTO emergency_queue (patient_id, triage_level, queue_priority, bed_id, status) VALUES (?, ?, ?, ?, ?);";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, ep.getPatientId());
            pstmt.setString(2, ep.getTriageLevel().name());
            pstmt.setInt(3, ep.getTriageLevel().getPriority());
            
            if (ep.getBedId() != null) {
                pstmt.setInt(4, ep.getBedId());
            } else {
                pstmt.setNull(4, java.sql.Types.INTEGER);
            }
            
            pstmt.setString(5, ep.getStatus());
            
            int affected = pstmt.executeUpdate();
            if (affected > 0) {
                ep.setId(DBConnection.getLastInsertId(conn));
                return true;
            }
        }
        return false;
    }

    public boolean updateStatus(int id, String status, Integer bedId) throws SQLException {
        String query = "UPDATE emergency_queue SET status = ?, bed_id = ? WHERE id = ?;";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, status);
            
            if (bedId != null) {
                pstmt.setInt(2, bedId);
            } else {
                pstmt.setNull(2, java.sql.Types.INTEGER);
            }
            
            pstmt.setInt(3, id);
            return pstmt.executeUpdate() > 0;
        }
    }

    public List<EmergencyPatient> getActiveQueue() throws SQLException {
        List<EmergencyPatient> list = new ArrayList<>();
        // Priority sorted! Critical (1) -> High (2) -> Medium (3) -> Low (4)
        String query = "SELECT eq.*, p.name AS patient_name, b.bed_number FROM emergency_queue eq " +
                       "JOIN patients p ON eq.patient_id = p.id " +
                       "LEFT JOIN beds b ON eq.bed_id = b.id " +
                       "WHERE eq.status = 'WAITING' " +
                       "ORDER BY eq.queue_priority ASC, eq.registered_at ASC;";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public List<EmergencyPatient> getAll() throws SQLException {
        List<EmergencyPatient> list = new ArrayList<>();
        String query = "SELECT eq.*, p.name AS patient_name, b.bed_number FROM emergency_queue eq " +
                       "JOIN patients p ON eq.patient_id = p.id " +
                       "LEFT JOIN beds b ON eq.bed_id = b.id " +
                       "ORDER BY eq.registered_at DESC;";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    private EmergencyPatient mapRow(ResultSet rs) throws SQLException {
        EmergencyPatient ep = new EmergencyPatient();
        ep.setId(rs.getInt("id"));
        ep.setPatientId(rs.getString("patient_id"));
        ep.setTriageLevel(TriageLevel.valueOf(rs.getString("triage_level")));
        ep.setQueuePriority(rs.getInt("queue_priority"));
        
        int bedId = rs.getInt("bed_id");
        ep.setBedId(rs.wasNull() ? null : bedId);
        
        ep.setStatus(rs.getString("status"));
        
        // Joined details
        ep.setPatientName(rs.getString("patient_name"));
        ep.setBedNumber(rs.getString("bed_number"));

        String dateStr = rs.getString("registered_at");
        if (dateStr != null) {
            try {
                dateStr = dateStr.replace(" ", "T");
                if (!dateStr.contains("T")) {
                    dateStr += "T00:00:00";
                }
                ep.setRegisteredAt(LocalDateTime.parse(dateStr));
            } catch (Exception e) {
                ep.setRegisteredAt(LocalDateTime.now());
            }
        }
        return ep;
    }
}
