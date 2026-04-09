package com.beta.FindHome.dto.auth;

import java.time.LocalDateTime;

public class OTPVerificationResponseDTO {
    private Boolean isValid;
    private String message;
    private LocalDateTime instant;

    public OTPVerificationResponseDTO(Boolean isValid, String message) {
        this.isValid = isValid;
        this.message = message;
        this.instant = getInstant();
    }

    public OTPVerificationResponseDTO() {
    }

    // Getters and Setters
    public boolean isValid() {
        return isValid;
    }

    public void setValid(boolean isValid) {
        this.isValid = isValid;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getInstant() {
        return LocalDateTime.now();
    }
}
