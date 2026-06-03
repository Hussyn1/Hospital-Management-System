package com.hospital.models;

import java.time.LocalDate;

public class LabRequest {
    private int id;
    private Integer recordId; // Nullable if direct order
    private String patientId;
    private String testName;
    private LocalDate requestedDate;
    private String resultText;
    private boolean flagAbnormal;
    private String status; // PENDING, COMPLETED
    private String reportFilePath;

    // Helper fields
    private String patientName;
    private String doctorName;

    public LabRequest() {}

    public LabRequest(int id, Integer recordId, String patientId, String testName, LocalDate requestedDate, String resultText, boolean flagAbnormal, String status, String reportFilePath) {
        this.id = id;
        this.recordId = recordId;
        this.patientId = patientId;
        this.testName = testName;
        this.requestedDate = requestedDate;
        this.resultText = resultText;
        this.flagAbnormal = flagAbnormal;
        this.status = status;
        this.reportFilePath = reportFilePath;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Integer getRecordId() { return recordId; }
    public void setRecordId(Integer recordId) { this.recordId = recordId; }

    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }

    public String getTestName() { return testName; }
    public void setTestName(String testName) { this.testName = testName; }

    public LocalDate getRequestedDate() { return requestedDate; }
    public void setRequestedDate(LocalDate requestedDate) { this.requestedDate = requestedDate; }

    public String getResultText() { return resultText; }
    public void setResultText(String resultText) { this.resultText = resultText; }

    public boolean isFlagAbnormal() { return flagAbnormal; }
    public void setFlagAbnormal(boolean flagAbnormal) { this.flagAbnormal = flagAbnormal; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getReportFilePath() { return reportFilePath; }
    public void setReportFilePath(String reportFilePath) { this.reportFilePath = reportFilePath; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }

    public String getDoctorName() { return doctorName; }
    public void setDoctorName(String doctorName) { this.doctorName = doctorName; }
}
