package com.hospital.dao;

import com.hospital.models.Admission;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AdmissionDAO {

    public boolean save(Admission adm) throws SQLException {
        String query = "INSERT INTO admissions (patient_id, bed_id, admission_date, daily_rate) VALUES (?, ?, ?, ?);";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, adm.getPatientId());
            pstmt.setInt(2, adm.getBedId());
            pstmt.setString(3, adm.getAdmissionDate().toString().replace("T", " "));
            pstmt.setDouble(4, adm.getDailyRate());
            
            int affected = pstmt.executeUpdate();
            if (affected > 0) {
                adm.setId(DBConnection.getLastInsertId(conn));
                return true;
            }
        }
        return false;
    }

    public boolean discharge(int id, LocalDateTime dischargeDate) throws SQLException {
        String query = "UPDATE admissions SET discharge_date = ? WHERE id = ?;";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, dischargeDate.toString().replace("T", " "));
            pstmt.setInt(2, id);
            return pstmt.executeUpdate() > 0;
        }
    }

    public Admission getById(int id) throws SQLException {
        String query = "SELECT a.*, p.name AS patient_name, b.bed_number, w.name AS ward_name FROM admissions a " +
                       "JOIN patients p ON a.patient_id = p.id " +
                       "JOIN beds b ON a.bed_id = b.id " +
                       "JOIN wards w ON b.ward_id = w.id " +
                       "WHERE a.id = ?;";
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

    public Admission getActiveByPatientId(String patientId) throws SQLException {
        String query = "SELECT a.*, p.name AS patient_name, b.bed_number, w.name AS ward_name FROM admissions a " +
                       "JOIN patients p ON a.patient_id = p.id " +
                       "JOIN beds b ON a.bed_id = b.id " +
                       "JOIN wards w ON b.ward_id = w.id " +
                       "WHERE a.patient_id = ? AND a.discharge_date IS NULL LIMIT 1;";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, patientId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    public List<Admission> getAllActive() throws SQLException {
        List<Admission> list = new ArrayList<>();
        String query = "SELECT a.*, p.name AS patient_name, b.bed_number, w.name AS ward_name FROM admissions a " +
                       "JOIN patients p ON a.patient_id = p.id " +
                       "JOIN beds b ON a.bed_id = b.id " +
                       "JOIN wards w ON b.ward_id = w.id " +
                       "WHERE a.discharge_date IS NULL ORDER BY a.admission_date DESC;";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }
    

    private Admission mapRow(ResultSet rs) throws SQLException {
        Admission a = new Admission();
        a.setId(rs.getInt("id"));
        a.setPatientId(rs.getString("patient_id"));
        a.setBedId(rs.getInt("bed_id"));
        a.setDailyRate(rs.getDouble("daily_rate"));
        
        // Joined details
        a.setPatientName(rs.getString("patient_name"));
        a.setBedNumber(rs.getString("bed_number"));
        a.setWardName(rs.getString("ward_name"));

        String admDateStr = rs.getString("admission_date");
        if (admDateStr != null) {
            a.setAdmissionDate(LocalDateTime.parse(admDateStr.replace(" ", "T")));
        }
        
        String disDateStr = rs.getString("discharge_date");
        if (disDateStr != null) {
            a.setDischargeDate(LocalDateTime.parse(disDateStr.replace(" ", "T")));
        }
        return a;
    }
}
