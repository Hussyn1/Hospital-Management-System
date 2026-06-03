package com.hospital.models;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Patient {
    private String id;
    private String name;
    private LocalDate dob;
    private String bloodGroup;
    private String contact;
    private String emergencyContact;
    private String medicalHistory;
    private String status; // ACTIVE, ADMITTED, DISCHARGED
    private boolean isDeleted;
    private LocalDateTime createdAt;

    public Patient() {}

    public Patient(String id, String name, LocalDate dob, String bloodGroup, String contact, String emergencyContact, String medicalHistory, String status, boolean isDeleted, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.dob = dob;
        this.bloodGroup = bloodGroup;
        this.contact = contact;
        this.emergencyContact = emergencyContact;
        this.medicalHistory = medicalHistory;
        this.status = status;
        this.isDeleted = isDeleted;
        this.createdAt = createdAt;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public LocalDate getDob() { return dob; }
    public void setDob(LocalDate dob) { this.dob = dob; }

    public String getBloodGroup() { return bloodGroup; }
    public void setBloodGroup(String bloodGroup) { this.bloodGroup = bloodGroup; }

    public String getContact() { return contact; }
    public void setContact(String contact) { this.contact = contact; }

    public String getEmergencyContact() { return emergencyContact; }
    public void setEmergencyContact(String emergencyContact) { this.emergencyContact = emergencyContact; }

    public String getMedicalHistory() { return medicalHistory; }
    public void setMedicalHistory(String medicalHistory) { this.medicalHistory = medicalHistory; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public boolean isDeleted() { return isDeleted; }
    public void setDeleted(boolean deleted) { isDeleted = deleted; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
