package com.hospital.dao;

import com.hospital.enums.AppointmentStatus;
import com.hospital.models.Appointment;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AppointmentDAO {

    public boolean save(Appointment appt) throws SQLException {
        String query = "INSERT INTO appointments (patient_id, doctor_id, appointment_date, appointment_time, status, notes) VALUES (?, ?, ?, ?, ?, ?);";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, appt.getPatientId());
            pstmt.setInt(2, appt.getDoctorId());
            pstmt.setString(3, appt.getAppointmentDate().toString());
            pstmt.setString(4, appt.getAppointmentTime().toString());
            pstmt.setString(5, appt.getStatus().name());
            pstmt.setString(6, appt.getNotes());
            
            int affected = pstmt.executeUpdate();
            if (affected > 0) {
                appt.setId(DBConnection.getLastInsertId(conn));
                return true;
            }
        }
        return false;
    }

    public boolean update(Appointment appt) throws SQLException {
        String query = "UPDATE appointments SET appointment_date = ?, appointment_time = ?, status = ?, notes = ? WHERE id = ?;";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, appt.getAppointmentDate().toString());
            pstmt.setString(2, appt.getAppointmentTime().toString());
            pstmt.setString(3, appt.getStatus().name());
            pstmt.setString(4, appt.getNotes());
            pstmt.setInt(5, appt.getId());
            return pstmt.executeUpdate() > 0;
        }
    }

    public boolean updateStatus(int id, AppointmentStatus status) throws SQLException {
        String query = "UPDATE appointments SET status = ? WHERE id = ?;";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, status.name());
            pstmt.setInt(2, id);
            return pstmt.executeUpdate() > 0;
        }
    }

    public Appointment getById(int id) throws SQLException {
        String query = "SELECT a.*, p.name AS patient_name, u.full_name AS doctor_name, d.specialization FROM appointments a " +
                       "JOIN patients p ON a.patient_id = p.id " +
                       "JOIN doctors d ON a.doctor_id = d.id " +
                       "JOIN users u ON d.user_id = u.id " +
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

    public List<Appointment> getAll() throws SQLException {
        List<Appointment> list = new ArrayList<>();
        String query = "SELECT a.*, p.name AS patient_name, u.full_name AS doctor_name, d.specialization FROM appointments a " +
                       "JOIN patients p ON a.patient_id = p.id " +
                       "JOIN doctors d ON a.doctor_id = d.id " +
                       "JOIN users u ON d.user_id = u.id " +
                       "ORDER BY a.appointment_date DESC, a.appointment_time DESC;";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public List<Appointment> getByPatientId(String patientId) throws SQLException {
        List<Appointment> list = new ArrayList<>();
        String query = "SELECT a.*, p.name AS patient_name, u.full_name AS doctor_name, d.specialization FROM appointments a " +
                       "JOIN patients p ON a.patient_id = p.id " +
                       "JOIN doctors d ON a.doctor_id = d.id " +
                       "JOIN users u ON d.user_id = u.id " +
                       "WHERE a.patient_id = ? ORDER BY a.appointment_date DESC;";
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

    /**
     * Retrieve doctor appointments on a specific day to validate and reject double-booking conflicts.
     */
    public List<Appointment> getByDoctorIdAndDate(int doctorId, LocalDate date) throws SQLException {
        List<Appointment> list = new ArrayList<>();
        String query = "SELECT a.*, p.name AS patient_name, u.full_name AS doctor_name, d.specialization FROM appointments a " +
                       "JOIN patients p ON a.patient_id = p.id " +
                       "JOIN doctors d ON a.doctor_id = d.id " +
                       "JOIN users u ON d.user_id = u.id " +
                       "WHERE a.doctor_id = ? AND a.appointment_date = ? AND a.status != 'CANCELLED';";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, doctorId);
            pstmt.setString(2, date.toString());
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    private Appointment mapRow(ResultSet rs) throws SQLException {
        Appointment a = new Appointment();
        a.setId(rs.getInt("id"));
        a.setPatientId(rs.getString("patient_id"));
        a.setDoctorId(rs.getInt("doctor_id"));
        a.setAppointmentDate(LocalDate.parse(rs.getString("appointment_date")));
        a.setAppointmentTime(LocalTime.parse(rs.getString("appointment_time")));
        a.setStatus(AppointmentStatus.valueOf(rs.getString("status")));
        a.setNotes(rs.getString("notes"));
        
        // Joined details
        a.setPatientName(rs.getString("patient_name"));
        a.setDoctorName(rs.getString("doctor_name"));
        a.setSpecialization(rs.getString("specialization"));

        String createdStr = rs.getString("created_at");
        if (createdStr != null) {
            try {
                createdStr = createdStr.replace(" ", "T");
                if (!createdStr.contains("T")) {
                    createdStr += "T00:00:00";
                }
                a.setCreatedAt(LocalDateTime.parse(createdStr));
            } catch (Exception e) {
                a.setCreatedAt(LocalDateTime.now());
            }
        }
        return a;
    }
}
