package com.hospital.services;

import com.hospital.dao.EMRDAO;
import com.hospital.models.MedicalRecord;
import com.hospital.utils.Validator;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class EMRService {
    private final EMRDAO emrDAO = new EMRDAO();

    public boolean addMedicalRecord(MedicalRecord mr) throws SQLException, IllegalArgumentException {
        if (mr.getPatientId() == null || mr.getPatientId().trim().isEmpty()) {
            throw new IllegalArgumentException("Patient ID is required.");
        }
        if (mr.getDoctorId() <= 0) {
            throw new IllegalArgumentException("Valid Doctor selection is required.");
        }
        if (!Validator.isNotEmpty(mr.getDiagnosis())) {
            throw new IllegalArgumentException("Diagnosis details cannot be empty.");
        }
        
        mr.setVisitDate(LocalDate.now());
        return emrDAO.save(mr);
    }

    public MedicalRecord getRecord(int id) throws SQLException {
        return emrDAO.getById(id);
    }

    public List<MedicalRecord> getPatientHistory(String patientId) throws SQLException {
        return emrDAO.getByPatientId(patientId);
    }

    public List<MedicalRecord> getAllRecords() throws SQLException {
        return emrDAO.getAll();
    }
}
