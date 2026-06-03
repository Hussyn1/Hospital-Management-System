package com.hospital.models;

import java.time.LocalDate;

public class MedicalRecord {
    private int id;
    private Integer appointmentId; // Nullable if direct log
    private String patientId;
    private int doctorId;
    private String diagnosis;
    private LocalDate visitDate;
    private String treatmentPlan;

    // Join helper fields
    private String patientName;
    private String doctorName;

    public MedicalRecord() {}

    public MedicalRecord(int id, Integer appointmentId, String patientId, int doctorId, String diagnosis, LocalDate visitDate, String treatmentPlan) {
        this.id = id;
        this.appointmentId = appointmentId;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.diagnosis = diagnosis;
        this.visitDate = visitDate;
        this.treatmentPlan = treatmentPlan;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Integer getAppointmentId() { return appointmentId; }
    public void setAppointmentId(Integer appointmentId) { this.appointmentId = appointmentId; }

    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }

    public int getDoctorId() { return doctorId; }
    public void setDoctorId(int doctorId) { this.doctorId = doctorId; }

    public String getDiagnosis() { return diagnosis; }
    public void setDiagnosis(String diagnosis) { this.diagnosis = diagnosis; }

    public LocalDate getVisitDate() { return visitDate; }
    public void setVisitDate(LocalDate visitDate) { this.visitDate = visitDate; }

    public String getTreatmentPlan() { return treatmentPlan; }
    public void setTreatmentPlan(String treatmentPlan) { this.treatmentPlan = treatmentPlan; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }

    public String getDoctorName() { return doctorName; }
    public void setDoctorName(String doctorName) { this.doctorName = doctorName; }
}
