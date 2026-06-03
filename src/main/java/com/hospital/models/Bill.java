package com.hospital.models;

import com.hospital.enums.PaymentStatus;
import java.time.LocalDateTime;

public class Bill {
    private int id;
    private String patientId;
    private Integer appointmentId;
    private Integer admissionId;
    private double totalAmount;
    private double paidAmount;
    private PaymentStatus paymentStatus;
    private LocalDateTime billingDate;
    private String paymentMethod; // CASH, CARD, INSURANCE, UPI

    // Helper fields
    private String patientName;

    public Bill() {}

    public Bill(int id, String patientId, Integer appointmentId, Integer admissionId, double totalAmount, double paidAmount, PaymentStatus paymentStatus, LocalDateTime billingDate, String paymentMethod) {
        this.id = id;
        this.patientId = patientId;
        this.appointmentId = appointmentId;
        this.admissionId = admissionId;
        this.totalAmount = totalAmount;
        this.paidAmount = paidAmount;
        this.paymentStatus = paymentStatus;
        this.billingDate = billingDate;
        this.paymentMethod = paymentMethod;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }

    public Integer getAppointmentId() { return appointmentId; }
    public void setAppointmentId(Integer appointmentId) { this.appointmentId = appointmentId; }

    public Integer getAdmissionId() { return admissionId; }
    public void setAdmissionId(Integer admissionId) { this.admissionId = admissionId; }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

    public double getPaidAmount() { return paidAmount; }
    public void setPaidAmount(double paidAmount) { this.paidAmount = paidAmount; }

    public PaymentStatus getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(PaymentStatus paymentStatus) { this.paymentStatus = paymentStatus; }

    public LocalDateTime getBillingDate() { return billingDate; }
    public void setBillingDate(LocalDateTime billingDate) { this.billingDate = billingDate; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }
}
