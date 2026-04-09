package com.beta.FindHome.dto.auth;

public class OTPResendResponseDTO {

    private String message;

    public OTPResendResponseDTO(String message) {
        this.message = message;
    }

    public OTPResendResponseDTO() {
    }

    // Getters and Setters
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
