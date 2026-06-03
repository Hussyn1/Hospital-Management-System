package com.hospital.services;

import com.hospital.dao.HRDAO;
import com.hospital.models.StaffShift;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class HRService {
    private final HRDAO hrDAO = new HRDAO();

    public boolean assignShift(int userId, LocalDateTime start, LocalDateTime end, String role) throws SQLException, IllegalArgumentException {
        if (userId <= 0) {
            throw new IllegalArgumentException("Valid staff user ID is required.");
        }
        if (start == null || end == null) {
            throw new IllegalArgumentException("Shift start and end times are required.");
        }
        if (!start.isBefore(end)) {
            throw new IllegalArgumentException("Shift start must be before shift end.");
        }
        if (role == null || role.trim().isEmpty()) {
            throw new IllegalArgumentException("Role assignment is required.");
        }

        StaffShift shift = new StaffShift();
        shift.setUserId(userId);
        shift.setShiftStart(start);
        shift.setShiftEnd(end);
        shift.setRoleAssigned(role);

        return hrDAO.saveShift(shift);
    }

    public boolean removeShift(int shiftId) throws SQLException {
        return hrDAO.deleteShift(shiftId);
    }

    public List<StaffShift> getStaffShifts(int userId) throws SQLException {
        return hrDAO.getShiftsForUser(userId);
    }

    public List<StaffShift> getAllShifts() throws SQLException {
        return hrDAO.getAllShifts();
    }
}
