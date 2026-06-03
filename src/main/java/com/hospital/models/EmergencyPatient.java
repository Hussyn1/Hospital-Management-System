package com.hospital.models;

import com.hospital.enums.TriageLevel;
import java.time.LocalDateTime;

public class EmergencyPatient {
    private int id;
    private String patientId;
    private TriageLevel triageLevel;
    private int queuePriority;
    private Integer bedId;
    private LocalDateTime registeredAt;
    private String status; // WAITING, ADMITTED, DISCHARGED

    // Helper fields
    private String patientName;
    private String bedNumber;

    public EmergencyPatient() {}

    public EmergencyPatient(int id, String patientId, TriageLevel triageLevel, int queuePriority, Integer bedId, LocalDateTime registeredAt, String status) {
        this.id = id;
        this.patientId = patientId;
        this.triageLevel = triageLevel;
        this.queuePriority = queuePriority;
        this.bedId = bedId;
        this.registeredAt = registeredAt;
        this.status = status;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }

    public TriageLevel getTriageLevel() { return triageLevel; }
    public void setTriageLevel(TriageLevel triageLevel) { this.triageLevel = triageLevel; }

    public int getQueuePriority() { return queuePriority; }
    public void setQueuePriority(int queuePriority) { this.queuePriority = queuePriority; }

    public Integer getBedId() { return bedId; }
    public void setBedId(Integer bedId) { this.bedId = bedId; }

    public LocalDateTime getRegisteredAt() { return registeredAt; }
    public void setRegisteredAt(LocalDateTime registeredAt) { this.registeredAt = registeredAt; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }

    public String getBedNumber() { return bedNumber; }
    public void setBedNumber(String bedNumber) { this.bedNumber = bedNumber; }
}
