package com.beta.FindHome.utils;

import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class OTPGenerator {
    public String generateOTP() {
        Random random = new Random();

        // Generate a random 4-digit number
        int otp = 1000 + random.nextInt(9000); // Generates a number between 1000 and 9999

        return String.valueOf(otp); // Convert the integer to a string
    }
}
