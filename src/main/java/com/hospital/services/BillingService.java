package com.hospital.services;

import com.hospital.dao.AppointmentDAO;
import com.hospital.dao.BillingDAO;
import com.hospital.dao.DBConnection;
import com.hospital.dao.AdmissionDAO;
import com.hospital.dao.EMRDAO;
import com.hospital.dao.LabDAO;
import com.hospital.dao.PrescriptionDAO;
import com.hospital.enums.PaymentStatus;
import com.hospital.models.Appointment;
import com.hospital.models.Admission;
import com.hospital.models.Bill;
import com.hospital.models.LabRequest;
import com.hospital.models.MedicalRecord;
import com.hospital.models.Prescription;
import com.hospital.models.PrescriptionItem;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

public class BillingService {

    private final BillingDAO     billingDAO     = new BillingDAO();
    private final AppointmentDAO appointmentDAO = new AppointmentDAO();
    private final AdmissionDAO   admissionDAO   = new AdmissionDAO();
    private final EMRDAO         emrDAO         = new EMRDAO();
    private final LabDAO         labDAO         = new LabDAO();
    private final PrescriptionDAO prescriptionDAO = new PrescriptionDAO();

    // ──────────────────────────────────────────────────────────────────────────
    // GENERATE / ACCUMULATE BILL
    //
    // Design: one open bill per patient.
    //   • First activity  → creates the bill row.
    //   • Subsequent ones → appends the new charge to the same row.
    //   • Once PAID       → the next activity opens a fresh bill (new visit cycle).
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Called after every billable event (appointment close, ward discharge).
     * Calculates the charge for this specific event and either creates or
     * appends to the patient's single open invoice.
     *
     * @param patientId     always required
     * @param appointmentId pass if this event is an appointment closure  (else null)
     * @param admissionId   pass if this event is a ward discharge         (else null)
     * @return the affected Bill (never null)
     */
    public Bill generateBill(String patientId, Integer appointmentId, Integer admissionId)
            throws SQLException {

        double chargeForThisEvent = 0.0;

        // ── 1. Consultation fee + linked lab & prescription costs ──────────────
        if (appointmentId != null) {
            Appointment appt = appointmentDAO.getById(appointmentId);
            if (appt != null) {
                chargeForThisEvent += getDoctorFee(appt.getDoctorId());

                MedicalRecord record = getRecordByApptId(appointmentId);
                if (record != null) {
                    chargeForThisEvent += getLabRequestsCost(record.getId());
                    chargeForThisEvent += getPrescriptionsCost(record.getId());
                }
            }
        }

        // ── 2. Ward stay daily rate ────────────────────────────────────────────
        if (admissionId != null) {
            Admission adm = admissionDAO.getById(admissionId);
            if (adm != null) {
                LocalDateTime start = adm.getAdmissionDate();
                LocalDateTime end   = adm.getDischargeDate() != null
                                        ? adm.getDischargeDate()
                                        : LocalDateTime.now();
                long days = ChronoUnit.DAYS.between(start.toLocalDate(), end.toLocalDate());
                if (days <= 0) days = 1; // minimum 1-day charge
                chargeForThisEvent += days * adm.getDailyRate();
            }
        }

        // ── 3. Find existing open bill or create a new one ────────────────────
        Bill openBill = billingDAO.getOpenBillForPatient(patientId);

        if (openBill != null) {
            // Append charge to the existing open invoice
            billingDAO.addToTotal(openBill.getId(), chargeForThisEvent);
            // Re-fetch so the returned object has the updated total
            openBill = billingDAO.getById(openBill.getId());
            return openBill;
        } else {
            // No open bill — create a fresh one for this patient
            Bill newBill = new Bill();
            newBill.setPatientId(patientId);
            newBill.setAppointmentId(appointmentId);
            newBill.setAdmissionId(admissionId);
            newBill.setTotalAmount(chargeForThisEvent);
            newBill.setPaidAmount(0.0);
            newBill.setPaymentStatus(PaymentStatus.UNPAID);
            newBill.setPaymentMethod("CASH"); // default; updated on first payment

            billingDAO.save(newBill);

            // Re-fetch via getById so patientName (from JOIN) is populated
            return billingDAO.getById(newBill.getId());
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // PROCESS PAYMENT
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Applies a payment to an invoice.
     *
     * Enforces:
     *   • Bill must exist.
     *   • Bill must NOT already be PAID — throws IllegalStateException.
     *   • Payment must not exceed the outstanding balance — throws IllegalArgumentException.
     *   • Automatically sets status to PARTIAL or PAID.
     */
    public boolean processPayment(int billId, double amount, String method)
            throws SQLException {

        if (amount <= 0) {
            throw new IllegalArgumentException("Payment amount must be greater than zero.");
        }

        // getById includes the PAID guard inside updatePayment, but we also
        // need outstanding here to compute the new status.
        Bill b = billingDAO.getById(billId);
        if (b == null) {
            throw new IllegalArgumentException("Invoice #" + billId + " not found.");
        }
        if (b.getPaymentStatus() == PaymentStatus.PAID) {
            throw new IllegalStateException(
                "Invoice #" + billId + " is already fully paid. No payment accepted.");
        }

        double outstanding = b.getTotalAmount() - b.getPaidAmount();
        if (amount > outstanding) {
            throw new IllegalArgumentException(String.format(
                "Payment $%.2f exceeds outstanding balance $%.2f. " +
                "Enter the exact outstanding amount or less.", amount, outstanding));
        }

        double newPaidTotal = b.getPaidAmount() + amount;
        PaymentStatus newStatus = (newPaidTotal >= b.getTotalAmount())
                                  ? PaymentStatus.PAID
                                  : PaymentStatus.PARTIAL;

        return billingDAO.updatePayment(billId, amount, newStatus, method);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // READ HELPERS
    // ──────────────────────────────────────────────────────────────────────────

    public Bill getBill(int id) throws SQLException {
        return billingDAO.getById(id);
    }

    public List<Bill> getPatientBills(String patientId) throws SQLException {
        return billingDAO.getByPatientId(patientId);
    }

    public List<Bill> getAllBills() throws SQLException {
        return billingDAO.getAll();
    }

    public Map<String, Double> getRevenueBreakdown() throws SQLException {
        return billingDAO.getRevenueSummary();
    }

    // ──────────────────────────────────────────────────────────────────────────
    // PRIVATE COST AGGREGATORS
    // ──────────────────────────────────────────────────────────────────────────

    private double getDoctorFee(int doctorId) {
        try {
            String query = "SELECT consultation_fee FROM doctors WHERE id = ?;";
            try (Connection conn = DBConnection.getConnection();
                 java.sql.PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setInt(1, doctorId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) return rs.getDouble(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    private MedicalRecord getRecordByApptId(int apptId) throws SQLException {
        List<MedicalRecord> list = emrDAO.getAll();
        for (MedicalRecord record : list) {
            if (record.getAppointmentId() != null
                    && record.getAppointmentId().intValue() == apptId) {
                return record;
            }
        }
        return null;
    }

    private double getLabRequestsCost(int recordId) throws SQLException {
        double cost = 0.0;
        List<LabRequest> requests = labDAO.getAll();
        for (LabRequest req : requests) {
            if (req.getRecordId() != null
                    && req.getRecordId().intValue() == recordId
                    && "COMPLETED".equals(req.getStatus())) {
                cost += 50.0; // flat charge per diagnostic test
            }
        }
        return cost;
    }

    private double getPrescriptionsCost(int recordId) throws SQLException {
        double cost = 0.0;
        List<Prescription> prescriptions = prescriptionDAO.getAll();
        for (Prescription p : prescriptions) {
            if (p.getRecordId() == recordId && "FILLED".equals(p.getStatus())) {
                for (PrescriptionItem item : p.getItems()) {
                    cost += item.getQuantity() * item.getPrice();
                }
            }
        }
        return cost;
    }
}