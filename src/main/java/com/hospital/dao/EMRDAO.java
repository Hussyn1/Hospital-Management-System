package com.hospital.dao;

import com.hospital.models.MedicalRecord;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class EMRDAO {

    public boolean save(MedicalRecord mr) throws SQLException {
        String query = "INSERT INTO medical_records (appointment_id, patient_id, doctor_id, diagnosis, visit_date, treatment_plan) VALUES (?, ?, ?, ?, ?, ?);";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            if (mr.getAppointmentId() != null) {
                pstmt.setInt(1, mr.getAppointmentId());
            } else {
                pstmt.setNull(1, java.sql.Types.INTEGER);
            }
            pstmt.setString(2, mr.getPatientId());
            pstmt.setInt(3, mr.getDoctorId());
            pstmt.setString(4, mr.getDiagnosis());
            pstmt.setString(5, mr.getVisitDate().toString());
            pstmt.setString(6, mr.getTreatmentPlan());
            
            int affected = pstmt.executeUpdate();
            if (affected > 0) {
                mr.setId(DBConnection.getLastInsertId(conn));
                return true;
            }
        }
        return false;
    }

    public MedicalRecord getById(int id) throws SQLException {
        String query = "SELECT mr.*, p.name AS patient_name, u.full_name AS doctor_name FROM medical_records mr " +
                       "JOIN patients p ON mr.patient_id = p.id " +
                       "JOIN doctors d ON mr.doctor_id = d.id " +
                       "JOIN users u ON d.user_id = u.id " +
                       "WHERE mr.id = ?;";
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

    public List<MedicalRecord> getByPatientId(String patientId) throws SQLException {
        List<MedicalRecord> list = new ArrayList<>();
        String query = "SELECT mr.*, p.name AS patient_name, u.full_name AS doctor_name FROM medical_records mr " +
                       "JOIN patients p ON mr.patient_id = p.id " +
                       "JOIN doctors d ON mr.doctor_id = d.id " +
                       "JOIN users u ON d.user_id = u.id " +
                       "WHERE mr.patient_id = ? ORDER BY mr.visit_date DESC;";
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

    public List<MedicalRecord> getAll() throws SQLException {
        List<MedicalRecord> list = new ArrayList<>();
        String query = "SELECT mr.*, p.name AS patient_name, u.full_name AS doctor_name FROM medical_records mr " +
                       "JOIN patients p ON mr.patient_id = p.id " +
                       "JOIN doctors d ON mr.doctor_id = d.id " +
                       "JOIN users u ON d.user_id = u.id " +
                       "ORDER BY mr.visit_date DESC;";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    private MedicalRecord mapRow(ResultSet rs) throws SQLException {
        MedicalRecord mr = new MedicalRecord();
        mr.setId(rs.getInt("id"));
        
        int apptId = rs.getInt("appointment_id");
        mr.setAppointmentId(rs.wasNull() ? null : apptId);
        
        mr.setPatientId(rs.getString("patient_id"));
        mr.setDoctorId(rs.getInt("doctor_id"));
        mr.setDiagnosis(rs.getString("diagnosis"));
        mr.setVisitDate(LocalDate.parse(rs.getString("visit_date")));
        mr.setTreatmentPlan(rs.getString("treatment_plan"));
        
        // Joined details
        mr.setPatientName(rs.getString("patient_name"));
        mr.setDoctorName(rs.getString("doctor_name"));
        return mr;
    }
}
