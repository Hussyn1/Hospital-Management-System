package com.hospital.services;

import com.hospital.dao.LabDAO;
import com.hospital.models.LabRequest;
import com.hospital.utils.Validator;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class LabService {
    private final LabDAO labDAO = new LabDAO();

    public boolean requestLabTest(LabRequest req) throws SQLException, IllegalArgumentException {
        if (req.getPatientId() == null || req.getPatientId().trim().isEmpty()) {
            throw new IllegalArgumentException("Patient ID is required.");
        }
        if (!Validator.isNotEmpty(req.getTestName())) {
            throw new IllegalArgumentException("Lab test name cannot be empty.");
        }
        
        req.setRequestedDate(LocalDate.now());
        req.setStatus("PENDING");
        req.setFlagAbnormal(false);
        return labDAO.save(req);
    }

    public boolean recordResults(int requestId, String resultText, boolean flagAbnormal, String reportFilePath) throws SQLException, IllegalArgumentException {
        if (!Validator.isNotEmpty(resultText)) {
            throw new IllegalArgumentException("Result report details must be specified.");
        }
        return labDAO.updateResults(requestId, resultText, flagAbnormal, reportFilePath);
    }

    public LabRequest getRequest(int id) throws SQLException {
        return labDAO.getById(id);
    }

    public List<LabRequest> getPatientLabHistory(String patientId) throws SQLException {
        return labDAO.getByPatientId(patientId);
    }

    public List<LabRequest> getPendingRequests() throws SQLException {
        return labDAO.getAllPending();
    }

    public List<LabRequest> getAllRequests() throws SQLException {
        return labDAO.getAll();
    }
}
