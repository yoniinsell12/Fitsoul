package com.fitsoul.app.core.validation;

import java.util.regex.Pattern;

public class ValidationHelper {
    
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    );
    
    private static final Pattern PHONE_PATTERN = Pattern.compile(
        "^[+]?[1-9]\\d{1,14}$"
    );
    
    public static ValidationResult validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return new ValidationResult(false, "Email is required");
        }
        
        if (!EMAIL_PATTERN.matcher(email.trim()).matches()) {
            return new ValidationResult(false, "Please enter a valid email address");
        }
        
        return new ValidationResult(true, null);
    }
    
    public static ValidationResult validatePassword(String password) {
        if (password == null || password.isEmpty()) {
            return new ValidationResult(false, "Password is required");
        }
        
        if (password.length() < 6) {
            return new ValidationResult(false, "Password must be at least 6 characters");
        }
        
        return new ValidationResult(true, null);
    }
    
    public static ValidationResult validatePasswordConfirmation(String password, String confirmPassword) {
        if (confirmPassword == null || confirmPassword.isEmpty()) {
            return new ValidationResult(false, "Please confirm your password");
        }
        
        if (!password.equals(confirmPassword)) {
            return new ValidationResult(false, "Passwords do not match");
        }
        
        return new ValidationResult(true, null);
    }
    
    public static ValidationResult validateFullName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            return new ValidationResult(false, "Full name is required");
        }
        
        if (fullName.trim().length() < 2) {
            return new ValidationResult(false, "Full name must be at least 2 characters");
        }
        
        return new ValidationResult(true, null);
    }
    
    public static ValidationResult validatePhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return new ValidationResult(false, "Phone number is required");
        }
        
        String cleanPhone = phoneNumber.replaceAll("\\s+", "").replaceAll("-", "");
        
        if (!PHONE_PATTERN.matcher(cleanPhone).matches()) {
            return new ValidationResult(false, "Please enter a valid phone number");
        }
        
        return new ValidationResult(true, null);
    }
    
    public static ValidationResult validateVerificationCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return new ValidationResult(false, "Verification code is required");
        }
        
        if (code.trim().length() != 6) {
            return new ValidationResult(false, "Verification code must be 6 digits");
        }
        
        if (!code.trim().matches("\\d{6}")) {
            return new ValidationResult(false, "Verification code must contain only numbers");
        }
        
        return new ValidationResult(true, null);
    }
    
    public static PasswordStrength getPasswordStrength(String password) {
        if (password == null || password.isEmpty()) {
            return PasswordStrength.NONE;
        }
        
        int score = 0;
        
        // Length check
        if (password.length() >= 8) score++;
        if (password.length() >= 12) score++;
        
        // Character variety checks
        if (password.matches(".*[a-z].*")) score++; // lowercase
        if (password.matches(".*[A-Z].*")) score++; // uppercase
        if (password.matches(".*\\d.*")) score++;   // digits
        if (password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*")) score++; // special chars
        
        if (score <= 2) return PasswordStrength.WEAK;
        if (score <= 4) return PasswordStrength.MEDIUM;
        return PasswordStrength.STRONG;
    }
    
    public enum PasswordStrength {
        NONE, WEAK, MEDIUM, STRONG
    }
}
