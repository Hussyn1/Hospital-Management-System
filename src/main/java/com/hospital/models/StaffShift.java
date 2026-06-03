package com.hospital.models;

import java.time.LocalDateTime;

public class StaffShift {
    private int id;
    private int userId;
    private LocalDateTime shiftStart;
    private LocalDateTime shiftEnd;
    private String roleAssigned;

    // Helper fields
    private String staffName;

    public StaffShift() {}

    public StaffShift(int id, int userId, LocalDateTime shiftStart, LocalDateTime shiftEnd, String roleAssigned) {
        this.id = id;
        this.userId = userId;
        this.shiftStart = shiftStart;
        this.shiftEnd = shiftEnd;
        this.roleAssigned = roleAssigned;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public LocalDateTime getShiftStart() { return shiftStart; }
    public void setShiftStart(LocalDateTime shiftStart) { this.shiftStart = shiftStart; }

    public LocalDateTime getShiftEnd() { return shiftEnd; }
    public void setShiftEnd(LocalDateTime shiftEnd) { this.shiftEnd = shiftEnd; }

    public String getRoleAssigned() { return roleAssigned; }
    public void setRoleAssigned(String roleAssigned) { this.roleAssigned = roleAssigned; }

    public String getStaffName() { return staffName; }
    public void setStaffName(String staffName) { this.staffName = staffName; }
}
