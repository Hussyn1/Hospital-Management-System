package com.hospital.models;

import java.time.LocalDateTime;

public class Admission {
    private int id;
    private String patientId;
    private int bedId;
    private LocalDateTime admissionDate;
    private LocalDateTime dischargeDate;
    private double dailyRate;

    // Helper fields
    private String patientName;
    private String bedNumber;
    private String wardName;

    public Admission() {}

    public Admission(int id, String patientId, int bedId, LocalDateTime admissionDate, LocalDateTime dischargeDate, double dailyRate) {
        this.id = id;
        this.patientId = patientId;
        this.bedId = bedId;
        this.admissionDate = admissionDate;
        this.dischargeDate = dischargeDate;
        this.dailyRate = dailyRate;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }

    public int getBedId() { return bedId; }
    public void setBedId(int bedId) { this.bedId = bedId; }

    public LocalDateTime getAdmissionDate() { return admissionDate; }
    public void setAdmissionDate(LocalDateTime admissionDate) { this.admissionDate = admissionDate; }

    public LocalDateTime getDischargeDate() { return dischargeDate; }
    public void setDischargeDate(LocalDateTime dischargeDate) { this.dischargeDate = dischargeDate; }

    public double getDailyRate() { return dailyRate; }
    public void setDailyRate(double dailyRate) { this.dailyRate = dailyRate; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }

    public String getBedNumber() { return bedNumber; }
    public void setBedNumber(String bedNumber) { this.bedNumber = bedNumber; }

    public String getWardName() { return wardName; }
    public void setWardName(String wardName) { this.wardName = wardName; }
}
