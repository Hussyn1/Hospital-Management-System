package com.hospital.models;

public class PrescriptionItem {
    private int id;
    private int prescriptionId;
    private int medicineId;
    private String dosage;
    private String frequency;
    private int quantity;

    // Helper fields
    private String medicineName;
    private String genericName;
    private double price;

    public PrescriptionItem() {}

    public PrescriptionItem(int id, int prescriptionId, int medicineId, String dosage, String frequency, int quantity) {
        this.id = id;
        this.prescriptionId = prescriptionId;
        this.medicineId = medicineId;
        this.dosage = dosage;
        this.frequency = frequency;
        this.quantity = quantity;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getPrescriptionId() { return prescriptionId; }
    public void setPrescriptionId(int prescriptionId) { this.prescriptionId = prescriptionId; }

    public int getMedicineId() { return medicineId; }
    public void setMedicineId(int medicineId) { this.medicineId = medicineId; }

    public String getDosage() { return dosage; }
    public void setDosage(String dosage) { this.dosage = dosage; }

    public String getFrequency() { return frequency; }
    public void setFrequency(String frequency) { this.frequency = frequency; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public String getMedicineName() { return medicineName; }
    public void setMedicineName(String medicineName) { this.medicineName = medicineName; }

    public String getGenericName() { return genericName; }
    public void setGenericName(String genericName) { this.genericName = genericName; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
}
