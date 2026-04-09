package com.beta.FindHome.utils;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class OTPGeneratorTest {

    private final OTPGenerator otpGenerator = new OTPGenerator();


    @Test
    public void testGenerateOTP_Length() {
        String otp = otpGenerator.generateOTP();
        assertEquals(4, otp.length(), "OTP should be 4 digits long");
    }

    @Test
    public void testGenerateOTP_IsNumeric() {
        String otp = otpGenerator.generateOTP();
        assertTrue(otp.matches("\\d{4}"), "OTP should contain only digits");
    }

    @Test
    public void testGenerateOTP_Range() {
        String otp = otpGenerator.generateOTP();
        int otpValue = Integer.parseInt(otp);
        assertTrue(otpValue >= 1000 && otpValue <= 9999, "OTP should be between 1000 and 9999");
    }
}