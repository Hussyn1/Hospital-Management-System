package com.hospital.services;

import com.hospital.dao.PatientDAO;
import com.hospital.models.Patient;
import com.hospital.utils.Validator;
import java.sql.SQLException;
import java.util.List;

public class PatientService {
    private final PatientDAO patientDAO = new PatientDAO();

    public String registerPatient(Patient p) throws SQLException, IllegalArgumentException {
        // Validate demographics
        if (!Validator.isNotEmpty(p.getName())) {
            throw new IllegalArgumentException("Patient name cannot be empty.");
        }
        if (p.getDob() == null || !Validator.isValidDOB(p.getDob())) {
            throw new IllegalArgumentException("Invalid date of birth. Must be a past date.");
        }
        if (!Validator.isValidPhone(p.getContact())) {
            throw new IllegalArgumentException("Invalid contact phone number format.");
        }
        if (!Validator.isNotEmpty(p.getEmergencyContact())) {
            throw new IllegalArgumentException("Emergency contact details are required.");
        }

        // Generate patient ID
        String nextId = patientDAO.getNextPatientId();
        p.setId(nextId);
        p.setStatus("ACTIVE");
        
        if (patientDAO.save(p)) {
            return nextId;
        } else {
            throw new SQLException("Failed to save patient record.");
        }
    }

    public boolean updatePatient(Patient p) throws SQLException, IllegalArgumentException {
        if (!Validator.isNotEmpty(p.getName())) {
            throw new IllegalArgumentException("Patient name cannot be empty.");
        }
        if (p.getDob() == null || !Validator.isValidDOB(p.getDob())) {
            throw new IllegalArgumentException("Invalid date of birth.");
        }
        if (!Validator.isValidPhone(p.getContact())) {
            throw new IllegalArgumentException("Invalid contact phone number.");
        }
        
        return patientDAO.update(p);
    }

    public boolean softDeletePatient(String id) throws SQLException {
        return patientDAO.delete(id);
    }

    public Patient getPatient(String id) throws SQLException {
        return patientDAO.getById(id);
    }

    public List<Patient> getAllPatients() throws SQLException {
        return patientDAO.getAllActive();
    }

    public List<Patient> searchPatients(String keyword) throws SQLException {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllPatients();
        }
        return patientDAO.searchActive(keyword.trim());
    }
}
