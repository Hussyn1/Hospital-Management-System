package com.hospital.services;

import com.hospital.dao.AdmissionDAO;
import com.hospital.dao.BedDAO;
import com.hospital.dao.PatientDAO;
import com.hospital.dao.WardDAO;
import com.hospital.dao.DBConnection;
import com.hospital.enums.BedStatus;
import com.hospital.models.Admission;
import com.hospital.models.Bed;
import com.hospital.models.Patient;
import com.hospital.models.Ward;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class WardService {
    private final WardDAO wardDAO = new WardDAO();
    private final BedDAO bedDAO = new BedDAO();
    private final AdmissionDAO admissionDAO = new AdmissionDAO();
    private final PatientDAO patientDAO = new PatientDAO();

    public List<Ward> getAllWards() throws SQLException {
        return wardDAO.getAll();
    }

    public List<Bed> getBedsInWard(int wardId) throws SQLException {
        return bedDAO.getByWardId(wardId);
    }

    public List<Bed> getAllBeds() throws SQLException {
        return bedDAO.getAll();
    }

    public int getAvailableCount() throws SQLException {
        return bedDAO.getAvailableBedsCount();
    }

    public int getOccupiedCount() throws SQLException {
        return bedDAO.getOccupiedBedsCount();
    }

    /**
     * Transactional Admission: Toggles bed status, patient status, and saves check-in.
     */
    public synchronized boolean admitPatient(String patientId, int bedId, double dailyRate) throws SQLException, IllegalArgumentException {
        // Pre-flight check patient and bed
        Patient p = patientDAO.getById(patientId);
        if (p == null) {
            throw new IllegalArgumentException("Patient not found.");
        }
        if ("ADMITTED".equals(p.getStatus())) {
            throw new IllegalArgumentException("Patient is already admitted to another ward.");
        }

        Bed b = bedDAO.getById(bedId);
        if (b == null) {
            throw new IllegalArgumentException("Ward bed not found.");
        }
        if (b.getStatus() != BedStatus.AVAILABLE) {
            throw new IllegalArgumentException("Bed is currently " + b.getStatus().name() + " and cannot be occupied.");
        }

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); // Transaction Start

            // 1. Update Bed Status to Occupied
            String updateBed = "UPDATE beds SET status = 'OCCUPIED' WHERE id = ?;";
            try (java.sql.PreparedStatement ps = conn.prepareStatement(updateBed)) {
                ps.setInt(1, bedId);
                ps.executeUpdate();
            }

            // 2. Update Patient Status to Admitted
            String updatePatient = "UPDATE patients SET status = 'ADMITTED' WHERE id = ?;";
            try (java.sql.PreparedStatement ps = conn.prepareStatement(updatePatient)) {
                ps.setString(1, patientId);
                ps.executeUpdate();
            }

            // 3. Save Admission log
            String insertAdm = "INSERT INTO admissions (patient_id, bed_id, admission_date, daily_rate) VALUES (?, ?, ?, ?);";
            try (java.sql.PreparedStatement ps = conn.prepareStatement(insertAdm)) {
                ps.setString(1, patientId);
                ps.setInt(2, bedId);
                ps.setString(3, LocalDateTime.now().toString().replace("T", " "));
                ps.setDouble(4, dailyRate);
                ps.executeUpdate();
            }

            conn.commit(); // Transaction Success
            return true;
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            throw e;
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }

    /**
     * Transactional Discharge: Toggles bed status, patient status, and closes check-in.
     */
    public synchronized boolean dischargePatient(String patientId) throws SQLException, IllegalArgumentException {
        Admission activeAdm = admissionDAO.getActiveByPatientId(patientId);
        if (activeAdm == null) {
            throw new IllegalArgumentException("No active ward admission record found for patient: " + patientId);
        }

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); // Transaction Start

            // 1. Update Admission Discharge Date
            String closeAdm = "UPDATE admissions SET discharge_date = ? WHERE id = ?;";
            try (java.sql.PreparedStatement ps = conn.prepareStatement(closeAdm)) {
                ps.setString(1, LocalDateTime.now().toString().replace("T", " "));
                ps.setInt(2, activeAdm.getId());
                ps.executeUpdate();
            }

            // 2. Set Bed back to Available
            String updateBed = "UPDATE beds SET status = 'AVAILABLE' WHERE id = ?;";
            try (java.sql.PreparedStatement ps = conn.prepareStatement(updateBed)) {
                ps.setInt(1, activeAdm.getBedId());
                ps.executeUpdate();
            }

            // 3. Set Patient to Discharged
            String updatePatient = "UPDATE patients SET status = 'DISCHARGED' WHERE id = ?;";
            try (java.sql.PreparedStatement ps = conn.prepareStatement(updatePatient)) {
                ps.setString(1, patientId);
                ps.executeUpdate();
            }

            conn.commit(); // Transaction Success
            return true;
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            throw e;
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }

    public Admission getActiveAdmission(String patientId) throws SQLException {
        return admissionDAO.getActiveByPatientId(patientId);
    }

    public List<Admission> getAllActiveAdmissions() throws SQLException {
        return admissionDAO.getAllActive();
    }
    
}
