package com.hospital.models;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Prescription {
    private int id;
    private int recordId;
    private int doctorId;
    private String patientId;
    private String status; // PENDING, FILLED
    private LocalDateTime createdAt;
    
    // Joint helper fields
    private String patientName;
    private String doctorName;
    private List<PrescriptionItem> items = new ArrayList<>();

    public Prescription() {}

    public Prescription(int id, int recordId, int doctorId, String patientId, String status, LocalDateTime createdAt) {
        this.id = id;
        this.recordId = recordId;
        this.doctorId = doctorId;
        this.patientId = patientId;
        this.status = status;
        this.createdAt = createdAt;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getRecordId() { return recordId; }
    public void setRecordId(int recordId) { this.recordId = recordId; }

    public int getDoctorId() { return doctorId; }
    public void setDoctorId(int doctorId) { this.doctorId = doctorId; }

    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }

    public String getDoctorName() { return doctorName; }
    public void setDoctorName(String doctorName) { this.doctorName = doctorName; }

    public List<PrescriptionItem> getItems() { return items; }
    public void setItems(List<PrescriptionItem> items) { this.items = items; }
}
