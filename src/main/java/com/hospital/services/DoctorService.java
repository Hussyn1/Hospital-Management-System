package com.hospital.services;

import com.hospital.dao.DoctorDAO;
import com.hospital.models.Doctor;
import com.hospital.utils.Validator;
import java.sql.SQLException;
import java.util.List;

public class DoctorService {
    private final DoctorDAO doctorDAO = new DoctorDAO();

    public boolean registerDoctor(Doctor doctor, String rawPassword, String username, String email) throws SQLException, IllegalArgumentException {
        if (!Validator.isNotEmpty(doctor.getDoctorName())) {
            throw new IllegalArgumentException("Doctor name cannot be empty.");
        }
        if (!Validator.isNotEmpty(username) || username.length() < 3) {
            throw new IllegalArgumentException("Username must be at least 3 characters.");
        }
        if (!Validator.isNotEmpty(rawPassword) || rawPassword.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters.");
        }
        if (!Validator.isValidEmail(email)) {
            throw new IllegalArgumentException("Invalid email format.");
        }
        if (doctor.getConsultationFee() < 0) {
            throw new IllegalArgumentException("Consultation fee cannot be negative.");
        }
        if (!Validator.isNotEmpty(doctor.getLicenseNumber())) {
            throw new IllegalArgumentException("Professional medical license number is required.");
        }
        if (!Validator.isNotEmpty(doctor.getSpecialization())) {
            throw new IllegalArgumentException("Specialization field is required.");
        }

        return doctorDAO.save(doctor, rawPassword, username, email);
    }

    public boolean updateDoctor(Doctor doctor, String email) throws SQLException, IllegalArgumentException {
        if (!Validator.isNotEmpty(doctor.getDoctorName())) {
            throw new IllegalArgumentException("Doctor name cannot be empty.");
        }
        if (!Validator.isValidEmail(email)) {
            throw new IllegalArgumentException("Invalid email format.");
        }
        if (doctor.getConsultationFee() < 0) {
            throw new IllegalArgumentException("Consultation fee cannot be negative.");
        }
        
        return doctorDAO.update(doctor, email);
    }

    public Doctor getDoctor(int id) throws SQLException {
        return doctorDAO.getById(id);
    }

    public Doctor getDoctorByUserId(int userId) throws SQLException {
        return doctorDAO.getByUserId(userId);
    }

    public List<Doctor> getAllDoctors() throws SQLException {
        return doctorDAO.getAll();
    }

    public List<Doctor> searchDoctors(String keyword) throws SQLException {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllDoctors();
        }
        return doctorDAO.search(keyword.trim());
    }

    public List<Doctor> getDoctorsBySpecialty(String specialty) throws SQLException {
        if (specialty == null || specialty.trim().isEmpty()) {
            return getAllDoctors();
        }
        return doctorDAO.getBySpecialty(specialty.trim());
    }
}
