package com.beta.FindHome.dto.auth;

import jakarta.annotation.Nullable;

public class OTPVerificationRequestDTO {

    @Nullable
    private String otp;

    public OTPVerificationRequestDTO(String phoneNumber, String otp) {
        this.otp = otp;
    }

    public OTPVerificationRequestDTO() {
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }
}
