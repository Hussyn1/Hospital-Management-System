package com.hospital.services;

import com.hospital.dao.AppointmentDAO;
import com.hospital.enums.AppointmentStatus;
import com.hospital.models.Appointment;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class AppointmentService {
    private final AppointmentDAO appointmentDAO = new AppointmentDAO();

    /**
     * Books a new appointment with slot validations and double-booking conflict checks.
     */
    public boolean bookAppointment(Appointment appt) throws SQLException, IllegalArgumentException {
        // Basic validations
        if (appt.getPatientId() == null || appt.getPatientId().trim().isEmpty()) {
            throw new IllegalArgumentException("Patient ID is required.");
        }
        if (appt.getDoctorId() <= 0) {
            throw new IllegalArgumentException("Valid Doctor selection is required.");
        }
        if (appt.getAppointmentDate() == null || appt.getAppointmentDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Appointment date must be today or in the future.");
        }
        if (appt.getAppointmentTime() == null) {
            throw new IllegalArgumentException("Appointment time slot is required.");
        }

        // Pre-flight Doctor Overlap Slot Validation (No double-booking)
        List<Appointment> existingAppts = appointmentDAO.getByDoctorIdAndDate(appt.getDoctorId(), appt.getAppointmentDate());
        LocalTime requestedTime = appt.getAppointmentTime();

        for (Appointment existing : existingAppts) {
            LocalTime existingTime = existing.getAppointmentTime();
            long minutesOverlap = Math.abs(ChronoUnit.MINUTES.between(existingTime, requestedTime));
            
            // Check for ±30 minutes overlap block
            if (minutesOverlap < 30) {
                throw new IllegalArgumentException(
                    String.format("Double-Booking Overlap Alert: Doctor has a confirmed appointment at %s. Please select a slot at least 30 minutes apart.", 
                                  existingTime.toString())
                );
            }
        }

        appt.setStatus(AppointmentStatus.PENDING);
        return appointmentDAO.save(appt);
    }

    public boolean updateAppointment(Appointment appt) throws SQLException, IllegalArgumentException {
        if (appt.getAppointmentDate() == null || appt.getAppointmentDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Appointment date must be today or in the future.");
        }
        
        // Slot overlap check for modifications as well
        List<Appointment> existingAppts = appointmentDAO.getByDoctorIdAndDate(appt.getDoctorId(), appt.getAppointmentDate());
        LocalTime requestedTime = appt.getAppointmentTime();

        for (Appointment existing : existingAppts) {
            if (existing.getId() == appt.getId()) continue; // Skip self

            LocalTime existingTime = existing.getAppointmentTime();
            long minutesOverlap = Math.abs(ChronoUnit.MINUTES.between(existingTime, requestedTime));
            if (minutesOverlap < 30) {
                throw new IllegalArgumentException(
                    String.format("Double-Booking Overlap Alert: Doctor has a confirmed appointment at %s.", existingTime.toString())
                );
            }
        }

        return appointmentDAO.update(appt);
    }

    public boolean confirmAppointment(int id) throws SQLException {
        return appointmentDAO.updateStatus(id, AppointmentStatus.CONFIRMED);
    }

    public boolean completeAppointment(int id) throws SQLException {
        return appointmentDAO.updateStatus(id, AppointmentStatus.COMPLETED);
    }

    public boolean cancelAppointment(int id) throws SQLException {
        return appointmentDAO.updateStatus(id, AppointmentStatus.CANCELLED);
    }

    public Appointment getAppointment(int id) throws SQLException {
        return appointmentDAO.getById(id);
    }

    public List<Appointment> getAllAppointments() throws SQLException {
        return appointmentDAO.getAll();
    }

    public List<Appointment> getPatientAppointments(String patientId) throws SQLException {
        return appointmentDAO.getByPatientId(patientId);
    }
}
