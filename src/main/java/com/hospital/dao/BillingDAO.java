package com.hospital.dao;

import com.hospital.enums.PaymentStatus;
import com.hospital.models.Bill;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BillingDAO {

    // ──────────────────────────────────────────────────────────────────────────
    // CORE CRUD
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Creates a brand-new bill row.
     * Only called when no open bill exists yet for this patient.
     */
    public boolean save(Bill b) throws SQLException {
        String query =
            "INSERT INTO billing " +
            "(patient_id, appointment_id, admission_id, total_amount, paid_amount, payment_status, payment_method) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?);";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, b.getPatientId());
            if (b.getAppointmentId() != null) pstmt.setInt(2, b.getAppointmentId());
            else                              pstmt.setNull(2, java.sql.Types.INTEGER);
            if (b.getAdmissionId() != null)   pstmt.setInt(3, b.getAdmissionId());
            else                              pstmt.setNull(3, java.sql.Types.INTEGER);
            pstmt.setDouble(4, b.getTotalAmount());
            pstmt.setDouble(5, b.getPaidAmount());
            pstmt.setString(6, b.getPaymentStatus().name());
            pstmt.setString(7, b.getPaymentMethod());

            int affected = pstmt.executeUpdate();
            if (affected > 0) {
                b.setId(DBConnection.getLastInsertId(conn));
                return true;
            }
        }
        return false;
    }

    /**
     * Appends an extra charge to an existing open bill's total.
     * Only updates rows that are NOT already fully PAID — prevents silently
     * inflating a closed invoice.
     */
    public boolean addToTotal(int billId, double extraAmount) throws SQLException {
        String query =
            "UPDATE billing SET total_amount = total_amount + ? " +
            "WHERE id = ? AND payment_status != 'PAID';";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setDouble(1, extraAmount);
            pstmt.setInt(2, billId);
            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * Records a payment against a bill.
     *
     * Guards:
     *   - Throws IllegalArgumentException if the bill does not exist.
     *   - Throws IllegalStateException  if the bill is already fully PAID.
     *   - Caps the payment so paid_amount never exceeds total_amount.
     */
    public boolean updatePayment(int id, double amountPaid, PaymentStatus status, String method)
            throws SQLException {

        // Re-fetch latest state to enforce guards
        Bill current = getById(id);
        if (current == null) {
            throw new IllegalArgumentException("Invoice #" + id + " does not exist.");
        }
        if (current.getPaymentStatus() == PaymentStatus.PAID) {
            throw new IllegalStateException(
                "Invoice #" + id + " is already fully paid. No further payment is accepted.");
        }

        // Cap: don't allow overpayment beyond total
        double outstanding = current.getTotalAmount() - current.getPaidAmount();
        if (amountPaid > outstanding) {
            throw new IllegalArgumentException(String.format(
                "Payment of $%.2f exceeds the outstanding balance of $%.2f.", amountPaid, outstanding));
        }

        String query =
            "UPDATE billing " +
            "SET paid_amount    = paid_amount + ?, " +
            "    payment_status = ?, " +
            "    payment_method = ?, " +
            "    billing_date   = CURRENT_TIMESTAMP " +
            "WHERE id = ?;";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setDouble(1, amountPaid);
            pstmt.setString(2, status.name());
            pstmt.setString(3, method);
            pstmt.setInt(4, id);
            return pstmt.executeUpdate() > 0;
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // FIND-OR-CREATE  (one open bill per patient)
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Returns the single open (UNPAID or PARTIAL) bill for a patient,
     * or null if none exists yet.
     * This is what enforces the "one active bill per patient" rule.
     */
    public Bill getOpenBillForPatient(String patientId) throws SQLException {
        String query =
            "SELECT b.*, p.name AS patient_name FROM billing b " +
            "JOIN patients p ON b.patient_id = p.id " +
            "WHERE b.patient_id = ? " +
            "  AND b.payment_status IN ('UNPAID', 'PARTIAL') " +
            "ORDER BY b.billing_date DESC LIMIT 1;";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, patientId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    // ──────────────────────────────────────────────────────────────────────────
    // LOOKUPS
    // ──────────────────────────────────────────────────────────────────────────

    public Bill getById(int id) throws SQLException {
        String query =
            "SELECT b.*, p.name AS patient_name FROM billing b " +
            "JOIN patients p ON b.patient_id = p.id WHERE b.id = ?;";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    public List<Bill> getByPatientId(String patientId) throws SQLException {
        List<Bill> list = new ArrayList<>();
        String query =
            "SELECT b.*, p.name AS patient_name FROM billing b " +
            "JOIN patients p ON b.patient_id = p.id " +
            "WHERE b.patient_id = ? ORDER BY b.billing_date DESC;";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, patientId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    public List<Bill> getAll() throws SQLException {
        List<Bill> list = new ArrayList<>();
        String query =
            "SELECT b.*, p.name AS patient_name FROM billing b " +
            "JOIN patients p ON b.patient_id = p.id " +
            "ORDER BY b.billing_date DESC;";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    /** Revenue by payment METHOD — for the Reports bar chart. */
    public Map<String, Double> getRevenueByPaymentMethod() throws SQLException {
        Map<String, Double> map = new HashMap<>();
        String query =
            "SELECT COALESCE(payment_method, 'UNKNOWN'), SUM(paid_amount) " +
            "FROM billing GROUP BY payment_method;";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) map.put(rs.getString(1), rs.getDouble(2));
        }
        return map;
    }

    /** Revenue summary by payment STATUS — kept for backward compat. */
    public Map<String, Double> getRevenueSummary() throws SQLException {
        Map<String, Double> summary = new HashMap<>();
        String query =
            "SELECT payment_status, SUM(paid_amount) FROM billing GROUP BY payment_status;";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) summary.put(rs.getString(1), rs.getDouble(2));
        }
        return summary;
    }

    // ──────────────────────────────────────────────────────────────────────────
    // ROW MAPPER
    // ──────────────────────────────────────────────────────────────────────────

    private Bill mapRow(ResultSet rs) throws SQLException {
        Bill b = new Bill();
        b.setId(rs.getInt("id"));
        b.setPatientId(rs.getString("patient_id"));

        int apptId = rs.getInt("appointment_id");
        b.setAppointmentId(rs.wasNull() ? null : apptId);

        int admId = rs.getInt("admission_id");
        b.setAdmissionId(rs.wasNull() ? null : admId);

        b.setTotalAmount(rs.getDouble("total_amount"));
        b.setPaidAmount(rs.getDouble("paid_amount"));
        b.setPaymentStatus(PaymentStatus.valueOf(rs.getString("payment_status")));
        b.setPaymentMethod(rs.getString("payment_method"));

        // Always populated by the JOIN in every query above
        b.setPatientName(rs.getString("patient_name"));

        String dateStr = rs.getString("billing_date");
        if (dateStr != null) {
            try {
                b.setBillingDate(LocalDateTime.parse(dateStr.replace(" ", "T")));
            } catch (Exception e) {
                b.setBillingDate(LocalDateTime.now());
            }
        }
        return b;
    }
}