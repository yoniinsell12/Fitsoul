package com.fitsoul.app.core.validation;

public class ValidationResult {
    private final boolean isValid;
    private final String errorMessage;
    
    public ValidationResult(boolean isValid, String errorMessage) {
        this.isValid = isValid;
        this.errorMessage = errorMessage;
    }
    
    public boolean isValid() {
        return isValid;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public static ValidationResult valid() {
        return new ValidationResult(true, null);
    }
    
    public static ValidationResult invalid(String errorMessage) {
        return new ValidationResult(false, errorMessage);
    }
    
    @Override
    public String toString() {
        return "ValidationResult{" +
                "isValid=" + isValid +
                ", errorMessage='" + errorMessage + '\'' +
                '}';
    }
}
