package com.hospital.services;

import com.hospital.dao.MedicineDAO;
import com.hospital.dao.PrescriptionDAO;
import com.hospital.models.Medicine;
import com.hospital.models.Prescription;
import com.hospital.models.PrescriptionItem;
import com.hospital.utils.Validator;
import java.sql.SQLException;
import java.util.List;

public class PharmacyService {
    private final MedicineDAO medicineDAO = new MedicineDAO();
    private final PrescriptionDAO prescriptionDAO = new PrescriptionDAO();

    // --- Medicine Inventory Operations ---

    public boolean addMedicine(Medicine m) throws SQLException, IllegalArgumentException {
        if (!Validator.isNotEmpty(m.getName())) {
            throw new IllegalArgumentException("Medicine name is required.");
        }
        if (!Validator.isNotEmpty(m.getGenericName())) {
            throw new IllegalArgumentException("Generic chemical name is required.");
        }
        if (m.getStockQty() < 0) {
            throw new IllegalArgumentException("Initial stock quantity cannot be negative.");
        }
        if (m.getPrice() <= 0) {
            throw new IllegalArgumentException("Price must be greater than zero.");
        }
        if (m.getExpiryDate() == null) {
            throw new IllegalArgumentException("Expiry date is required.");
        }

        return medicineDAO.save(m);
    }

    public boolean updateMedicine(Medicine m) throws SQLException, IllegalArgumentException {
        if (!Validator.isNotEmpty(m.getName())) {
            throw new IllegalArgumentException("Medicine name is required.");
        }
        if (m.getStockQty() < 0) {
            throw new IllegalArgumentException("Stock quantity cannot be negative.");
        }
        if (m.getPrice() <= 0) {
            throw new IllegalArgumentException("Price must be greater than zero.");
        }
        return medicineDAO.update(m);
    }

    public boolean deleteMedicine(int id) throws SQLException {
        return medicineDAO.delete(id);
    }

    public List<Medicine> getAllMedicines() throws SQLException {
        return medicineDAO.getAll();
    }

    public List<Medicine> getLowStockAlerts() throws SQLException {
        return medicineDAO.getLowStockMedicines();
    }

    public List<Medicine> searchMedicines(String keyword) throws SQLException {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllMedicines();
        }
        return medicineDAO.search(keyword.trim());
    }

    // --- Prescription Operations ---

    public boolean createPrescription(Prescription p) throws SQLException, IllegalArgumentException {
        if (p.getRecordId() <= 0) {
            throw new IllegalArgumentException("Prescription must be linked to a valid medical record.");
        }
        if (p.getItems() == null || p.getItems().isEmpty()) {
            throw new IllegalArgumentException("Prescription must contain at least one medicine item.");
        }

        p.setStatus("PENDING");
        return prescriptionDAO.save(p);
    }

    /**
     * Transactional Dispense: Checks stock availability for all items, deducts stock, and marks filled.
     */
    public synchronized boolean dispensePrescription(int prescriptionId) throws SQLException, IllegalArgumentException {
        Prescription p = prescriptionDAO.getById(prescriptionId);
        if (p == null) {
            throw new IllegalArgumentException("Prescription not found.");
        }
        if ("FILLED".equals(p.getStatus())) {
            throw new IllegalArgumentException("Prescription has already been dispensed.");
        }

        // 1. Validation Step: Pre-flight check stock for all items
        for (PrescriptionItem item : p.getItems()) {
            Medicine med = medicineDAO.getById(item.getMedicineId());
            if (med == null) {
                throw new IllegalArgumentException("Medicine not found in catalog: ID " + item.getMedicineId());
            }
            if (med.getStockQty() < item.getQuantity()) {
                throw new IllegalArgumentException(
                    String.format("Fulfillment Failure: Insufficient stock for '%s'. Required: %d, Available: %d.", 
                                  med.getName(), item.getQuantity(), med.getStockQty())
                );
            }
        }

        // 2. Execution Step: Deduct stock and finalize status
        for (PrescriptionItem item : p.getItems()) {
            medicineDAO.updateStock(item.getMedicineId(), -item.getQuantity());
        }

        return prescriptionDAO.updateStatus(prescriptionId, "FILLED");
    }

    public Prescription getPrescription(int id) throws SQLException {
        return prescriptionDAO.getById(id);
    }

    public List<Prescription> getPendingPrescriptions() throws SQLException {
        return prescriptionDAO.getAllPending();
    }

    public List<Prescription> getAllPrescriptions() throws SQLException {
        return prescriptionDAO.getAll();
    }
}
