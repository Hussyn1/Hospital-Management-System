package com.hospital.services;

import com.hospital.dao.*;
import com.hospital.models.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Analytics and dashboard metrics aggregation service.
 * Provides data for dashboard summary cards and charts.
 */
public class ReportsService {

    /**
     * Returns key dashboard statistics for the admin overview panel.
     */
    public Map<String, Object> getDashboardStats() throws SQLException {
        Map<String, Object> stats = new LinkedHashMap<>();

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            // Total Active Patients
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM patients WHERE is_deleted = 0;")) {
                stats.put("totalPatients", rs.next() ? rs.getInt(1) : 0);
            }

            // Total Doctors
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM doctors;")) {
                stats.put("totalDoctors", rs.next() ? rs.getInt(1) : 0);
            }

            // Today's Appointments
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM appointments WHERE appointment_date = date('now');")) {
                stats.put("todayAppointments", rs.next() ? rs.getInt(1) : 0);
            }

            // Active Admissions (not yet discharged)
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM admissions WHERE discharge_date IS NULL;")) {
                stats.put("activeAdmissions", rs.next() ? rs.getInt(1) : 0);
            }

            // Available Beds
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM beds WHERE status = 'AVAILABLE';")) {
                stats.put("availableBeds", rs.next() ? rs.getInt(1) : 0);
            }

            // Occupied Beds
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM beds WHERE status = 'OCCUPIED';")) {
                stats.put("occupiedBeds", rs.next() ? rs.getInt(1) : 0);
            }

            // Pending Prescriptions
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM prescriptions WHERE status = 'PENDING';")) {
                stats.put("pendingPrescriptions", rs.next() ? rs.getInt(1) : 0);
            }

            // Pending Lab Requests
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM lab_requests WHERE status = 'PENDING';")) {
                stats.put("pendingLabRequests", rs.next() ? rs.getInt(1) : 0);
            }

            // Emergency Queue Size
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM emergency_queue WHERE status = 'WAITING';")) {
                stats.put("emergencyQueueSize", rs.next() ? rs.getInt(1) : 0);
            }

            // Low Stock Medicine Count
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM medicines WHERE stock_qty <= low_stock_threshold;")) {
                stats.put("lowStockMedicines", rs.next() ? rs.getInt(1) : 0);
            }

            // Low Stock Inventory Count
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM inventory WHERE stock_qty <= low_stock_threshold;")) {
                stats.put("lowStockInventory", rs.next() ? rs.getInt(1) : 0);
            }

            // Total Revenue (all paid)
            try (ResultSet rs = stmt.executeQuery("SELECT COALESCE(SUM(paid_amount), 0) FROM billing;")) {
                stats.put("totalRevenue", rs.next() ? rs.getDouble(1) : 0.0);
            }

            // Unpaid Bills Total
            try (ResultSet rs = stmt.executeQuery("SELECT COALESCE(SUM(total_amount - paid_amount), 0) FROM billing WHERE payment_status != 'PAID';")) {
                stats.put("unpaidBillsTotal", rs.next() ? rs.getDouble(1) : 0.0);
            }

            // Unread Notifications Count (admin user_id = 1)
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM notifications WHERE is_read = 0;")) {
                stats.put("unreadNotifications", rs.next() ? rs.getInt(1) : 0);
            }
        }

        return stats;
    }

    /**
     * Returns appointment status distribution for pie/bar charts.
     */
    public Map<String, Integer> getAppointmentStatusBreakdown() throws SQLException {
        Map<String, Integer> breakdown = new LinkedHashMap<>();
        String query = "SELECT status, COUNT(*) FROM appointments GROUP BY status;";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                breakdown.put(rs.getString(1), rs.getInt(2));
            }
        }
        return breakdown;
    }

    /**
     * Returns bed occupancy breakdown by ward for dashboard charts.
     */
    public Map<String, int[]> getBedOccupancyByWard() throws SQLException {
        Map<String, int[]> occupancy = new LinkedHashMap<>();
        String query = "SELECT w.name, " +
                       "SUM(CASE WHEN b.status = 'AVAILABLE' THEN 1 ELSE 0 END), " +
                       "SUM(CASE WHEN b.status = 'OCCUPIED' THEN 1 ELSE 0 END), " +
                       "SUM(CASE WHEN b.status = 'UNDER_MAINTENANCE' THEN 1 ELSE 0 END) " +
                       "FROM beds b JOIN wards w ON b.ward_id = w.id GROUP BY w.name;";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                occupancy.put(rs.getString(1), new int[]{rs.getInt(2), rs.getInt(3), rs.getInt(4)});
            }
        }
        return occupancy;
    }

    /**
     * Returns revenue breakdown by payment method.
     */
    public Map<String, Double> getRevenueByPaymentMethod() throws SQLException {
        Map<String, Double> revenue = new LinkedHashMap<>();
        String query = "SELECT payment_method, COALESCE(SUM(paid_amount), 0) FROM billing WHERE payment_method IS NOT NULL GROUP BY payment_method;";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                revenue.put(rs.getString(1), rs.getDouble(2));
            }
        }
        return revenue;
    }
}
