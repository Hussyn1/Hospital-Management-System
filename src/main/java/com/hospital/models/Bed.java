package com.hospital.models;

import com.hospital.enums.BedStatus;

public class Bed {
    private int id;
    private int wardId;
    private String bedNumber;
    private BedStatus status;

    // Helper fields
    private String wardName;
    private String wardType;

    public Bed() {}

    public Bed(int id, int wardId, String bedNumber, BedStatus status) {
        this.id = id;
        this.wardId = wardId;
        this.bedNumber = bedNumber;
        this.status = status;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getWardId() { return wardId; }
    public void setWardId(int wardId) { this.wardId = wardId; }

    public String getBedNumber() { return bedNumber; }
    public void setBedNumber(String bedNumber) { this.bedNumber = bedNumber; }

    public BedStatus getStatus() { return status; }
    public void setStatus(BedStatus status) { this.status = status; }

    public String getWardName() { return wardName; }
    public void setWardName(String wardName) { this.wardName = wardName; }

    public String getWardType() { return wardType; }
    public void setWardType(String wardType) { this.wardType = wardType; }
}
