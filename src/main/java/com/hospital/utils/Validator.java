package com.hospital.utils;

import java.time.LocalDate;
import java.util.regex.Pattern;

public class Validator {
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"
    );
    
    private static final Pattern PHONE_PATTERN = Pattern.compile(
        "^\\+?[0-9\\s\\-()]{7,15}$"
    );

    /**
     * Checks if email is valid.
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email).matches();
    }

    /**
     * Checks if contact phone number is valid.
     */
    public static boolean isValidPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return false;
        }
        return PHONE_PATTERN.matcher(phone).matches();
    }

    /**
     * Validates if date of birth is in the past.
     */
    public static boolean isValidDOB(LocalDate dob) {
        if (dob == null) {
            return false;
        }
        return dob.isBefore(LocalDate.now());
    }

    /**
     * General string null/empty validation.
     */
    public static boolean isNotEmpty(String str) {
        return str != null && !str.trim().isEmpty();
    }

    /**
     * Double validation (e.g. fees, prices) - must be positive.
     */
    public static boolean isPositiveDouble(String doubleStr) {
        try {
            double val = Double.parseDouble(doubleStr);
            return val >= 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Integer validation (e.g. stock count) - must be positive.
     */
    public static boolean isPositiveInt(String intStr) {
        try {
            int val = Integer.parseInt(intStr);
            return val >= 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
