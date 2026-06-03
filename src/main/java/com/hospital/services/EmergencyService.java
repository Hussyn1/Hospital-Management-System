package com.hospital.services;

import com.hospital.dao.EmergencyDAO;
import com.hospital.dao.BedDAO;
import com.hospital.enums.BedStatus;
import com.hospital.enums.TriageLevel;
import com.hospital.models.Bed;
import com.hospital.models.EmergencyPatient;
import java.sql.SQLException;
import java.util.List;

public class EmergencyService {
    private final EmergencyDAO emergencyDAO = new EmergencyDAO();
    private final BedDAO bedDAO = new BedDAO();

    /**
     * Registers a new walk-in emergency case, reorders queue priorities, and handles instant bed assignment.
     */
    public boolean registerEmergency(EmergencyPatient ep) throws SQLException, IllegalArgumentException {
        if (ep.getPatientId() == null || ep.getPatientId().trim().isEmpty()) {
            throw new IllegalArgumentException("Patient ID is required.");
        }
        if (ep.getTriageLevel() == null) {
            throw new IllegalArgumentException("Triage priority level is required.");
        }

        ep.setStatus("WAITING");
        ep.setQueuePriority(ep.getTriageLevel().getPriority());

        // Instant Bed Assignment logic: check if there's any available ICU or General bed
        List<Bed> allBeds = bedDAO.getAll();
        for (Bed b : allBeds) {
            if (b.getStatus() == BedStatus.AVAILABLE && ("ICU".equals(b.getWardType()) || "GENERAL".equals(b.getWardType()))) {
                ep.setBedId(b.getId());
                ep.setStatus("ADMITTED");
                // Update bed status to OCCUPIED
                bedDAO.updateStatus(b.getId(), BedStatus.OCCUPIED);
                break;
            }
        }

        return emergencyDAO.save(ep);
    }

    /**
     * Triage Queue Reordering: returns active patients sorted automatically by priority.
     */
    public List<EmergencyPatient> getActiveWaitingQueue() throws SQLException {
        return emergencyDAO.getActiveQueue();
    }

    public boolean resolveEmergency(int id, String status) throws SQLException {
        EmergencyPatient ep = null;
        List<EmergencyPatient> list = emergencyDAO.getAll();
        for (EmergencyPatient item : list) {
            if (item.getId() == id) {
                ep = item;
                break;
            }
        }
        
        if (ep == null) {
            throw new IllegalArgumentException("Emergency record not found.");
        }

        // If discharged or removed, release their bed back to available!
        if (("DISCHARGED".equalsIgnoreCase(status) || "RESOLVED".equalsIgnoreCase(status)) && ep.getBedId() != null) {
            bedDAO.updateStatus(ep.getBedId(), BedStatus.AVAILABLE);
        }

        return emergencyDAO.updateStatus(id, status, null);
    }

    public List<EmergencyPatient> getAllEmergencyRecords() throws SQLException {
        return emergencyDAO.getAll();
    }
}
