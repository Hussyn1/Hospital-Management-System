package com.hospital.models;

public class Doctor {
    private int id;
    private int userId;
    private String specialization;
    private String licenseNumber;
    private double consultationFee;
    private String availabilitySchedule; // JSON string
    private String department;
    
    // Helper fields from joined query
    private String doctorName; 
    private String contact;
    private String email;

    public Doctor() {}

    public Doctor(int id, int userId, String specialization, String licenseNumber, double consultationFee, String availabilitySchedule, String department) {
        this.id = id;
        this.userId = userId;
        this.specialization = specialization;
        this.licenseNumber = licenseNumber;
        this.consultationFee = consultationFee;
        this.availabilitySchedule = availabilitySchedule;
        this.department = department;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }

    public String getLicenseNumber() { return licenseNumber; }
    public void setLicenseNumber(String licenseNumber) { this.licenseNumber = licenseNumber; }

    public double getConsultationFee() { return consultationFee; }
    public void setConsultationFee(double consultationFee) { this.consultationFee = consultationFee; }

    public String getAvailabilitySchedule() { return availabilitySchedule; }
    public void setAvailabilitySchedule(String availabilitySchedule) { this.availabilitySchedule = availabilitySchedule; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getDoctorName() { return doctorName; }
    public void setDoctorName(String doctorName) { this.doctorName = doctorName; }

    public String getContact() { return contact; }
    public void setContact(String contact) { this.contact = contact; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
