package com.beta.FindHome.utils;

import com.beta.FindHome.config.MessageServiceConfig;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;

@Component
public class MessageServiceUtils {

    private final RedisTemplate<String, Object> redisTemplate;
    private final OTPGenerator otpGenerator;
    private final WebClient webClient;
    private final String sociairAPIToken;
    private final String sociairAPIURL;
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(MessageServiceUtils.class);
    private final RedisUtils redisUtils;

    @Autowired
    public MessageServiceUtils(
            MessageServiceConfig messageServiceConfig,
            WebClient.Builder webClientBuilder,
            OTPGenerator otpGenerator,
            RedisTemplate<String, Object> redisTemplate,
            RedisUtils redisUtils) {
        this.sociairAPIToken = messageServiceConfig.getMessageAPIToken();
        this.sociairAPIURL = messageServiceConfig.getMessageAPIURL();
        this.webClient = webClientBuilder.baseUrl(sociairAPIURL).build();
        this.otpGenerator = otpGenerator;
        this.redisTemplate = redisTemplate;
        this.redisUtils = redisUtils;
    }

//    public String developmentOTPSMS(
//            String phoneNumber
//    ) {
//        OTPMessage otpMessage = new OTPMessage("8888", phoneNumber);
//        redisTemplate.opsForValue().set(phoneNumber, otpMessage, 30, TimeUnit.MINUTES);
//        return otpMessage.getOtp();
//    }

    @Async
    @Retryable(
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000))
    public CompletableFuture<String> ownerVerificationSMS(
            String phoneNumber,
            String message
    ){
            try {
            // Create the request body
            var requestBody = new SMSRequest(message, phoneNumber);

            // Send the SMS and handle the success response
            SMSResponse response = webClient.post()
                    .uri("/api/sms")
                    .header("Authorization", "Bearer " + sociairAPIToken)
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .body(Mono.just(requestBody), SMSRequest.class)
                    .retrieve()
                    .bodyToMono(SMSResponse.class)
                    .toFuture() //toFuture() is used to convert the Mono to a CompletableFuture as the return type of the method is CompletableFuture
                    .get(); // get() is used for blocking and waiting for the result

            if (response != null && "Success! SMS has been sent".equalsIgnoreCase(response.getMessage())) {
                return CompletableFuture.completedFuture("SMS sent successfully!");
            } else {
                return CompletableFuture.completedFuture("Unexpected success response received: " + response);
            }
        } catch (WebClientResponseException e) {
            // Handle and log exception
            return CompletableFuture.completedFuture("Error: Unable to send SMS. " + e.getResponseBodyAsString());
        } catch (Exception e) {
            throw new CompletionException("Unexpected error during SMS operation", e);
        }
    }
    @Async
    @Retryable(
            maxAttempts = 3,
            backoff = @Backoff(delay = 100))
    public CompletableFuture<String> loginOTPSMS(
            String phoneNumber
    ) {
        try {
            String message = generateAndSaveOTPMessage(phoneNumber);
            var requestBody = new SMSRequest(message, phoneNumber);

            // Send the SMS and handle the success response
            SMSResponse response = webClient.post()
                    .uri("/api/sms")
                    .header("Authorization", "Bearer " + sociairAPIToken)
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .body(Mono.just(requestBody), SMSRequest.class)
                    .retrieve()
                    .bodyToMono(SMSResponse.class)
                    .toFuture() //toFuture() is used to convert the Mono to a CompletableFuture as the return type of the method is CompletableFuture
                    .get(); // get() is used for blocking and waiting for the result

            if (response != null && "Success! SMS has been sent".equalsIgnoreCase(response.getMessage())) {
                return CompletableFuture.completedFuture("SMS sent successfully!");
            } else {
                return CompletableFuture.completedFuture("Unexpected success response received: " + response);
            }
        } catch (WebClientResponseException e) {
            // Handle and log exception
            return CompletableFuture.completedFuture("Error: Unable to send SMS. " + e.getResponseBodyAsString());
        } catch (Exception e) {
            throw new CompletionException("Unexpected error during SMS operation", e);
        }

    }
    @Async
    @Retryable(
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000))
    public CompletableFuture<String> forgotPasswordOTPSMS(
            String phoneNumber
    ) {
        try {
            String message = generateAndForgotPasswordOTPMessage(phoneNumber);
            var requestBody = new SMSRequest(message, phoneNumber);

            SMSResponse response = webClient.post()
                    .uri("/api/sms")
                    .header("Authorization", "Bearer " + sociairAPIToken)
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .body(Mono.just(requestBody), SMSRequest.class)
                    .retrieve()
                    .bodyToMono(SMSResponse.class)
                    .toFuture()
                    .get(); // Or non-blocking handling

            if (response != null && "Success! SMS has been sent".equalsIgnoreCase(response.getMessage())) {
                return CompletableFuture.completedFuture("SMS sent successfully!");
            } else {
                return CompletableFuture.completedFuture("Unexpected success response received: " + response);
            }
        } catch (WebClientResponseException e) {
            // Handle and log exception
            return CompletableFuture.completedFuture("Error: Unable to send SMS. " + e.getResponseBodyAsString());
        } catch (Exception e) {
            throw new CompletionException("Unexpected error during SMS operation", e);
        }
    }

    public boolean verifyOTP(
            String otp,
            String phoneNumber
    ) {
        try {
            Object otpMessageObj = redisTemplate.opsForValue().get(phoneNumber);
            if (otpMessageObj != null) {
                // Convert the Object into the correct OTPMessage type using ObjectMapper
                ObjectMapper objectMapper = new ObjectMapper();
                OTPMessage otpMessage = objectMapper.convertValue(otpMessageObj, OTPMessage.class);
                if (otpMessage.getOtp().equals(otp)) {
                    redisUtils.delete(phoneNumber); // Delete the OTP from Redis after successful verification
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            logger.error("Error verifying OTP for phone number {}: {}", phoneNumber, e.getMessage());
            return false;
        }
    }

    private String generateAndSaveOTPMessage(
            String phoneNumber
    ) {
        String otpNumber = otpGenerator.generateOTP();
        OTPMessage otpMessage = new OTPMessage(otpNumber, phoneNumber); // Create the OTPMessage object

        redisTemplate.opsForValue().set( phoneNumber, otpMessage, 5, TimeUnit.MINUTES);
        return "Find Home Nepal Registration\n" +
                "Your OTP is: " + otpNumber + "\n" +
                "Please do not share this OTP with anyone.\n" +
                "Thank you for choosing Find Home Nepal!";
    }
    private String generateAndForgotPasswordOTPMessage(
            String phoneNumber)
    {
        String otpNumber = otpGenerator.generateOTP();
        OTPMessage otpMessage = new OTPMessage(otpNumber, phoneNumber); // Create the OTPMessage object

        redisTemplate.opsForValue().set( phoneNumber, otpMessage, 5, TimeUnit.MINUTES);
        return "Find Home Nepal Password Reset\n" +
                "Your OTP is: " + otpNumber + "\n" +
                "This OTP is valid for a limited time only. Please use it to reset your password.\n" +
                "If you did not request a password reset, please ignore this message.";
    }

    // Inner class for the request body
    private static class SMSRequest {
        private final String message;
        private final String mobile;

        public SMSRequest(String message, String mobile) {
            this.message = message;
            this.mobile = mobile;
        }

        public String getMessage() {
            return message;
        }

        public String getMobile() {
            return mobile;
        }
    }

    // For success responses
    @JsonIgnoreProperties(ignoreUnknown = true) //means that any properties not bound in this type should be ignored
    private static class SMSResponse {
        private final String message;
        private final int ntc;
        private final int ncell;
        private final int smartcell;

        @JsonCreator
        public SMSResponse(
                @JsonProperty("message") String message,
                @JsonProperty("ntc") int ntc,
                @JsonProperty("ncell") int ncell,
                @JsonProperty("smartcell") int smartcell) {
            this.message = message;
            this.ntc = ntc;
            this.ncell = ncell;
            this.smartcell = smartcell;
        }

        public String getMessage() {
            return message;
        }

        @Override
        public String toString() {
            return "SMSResponse{" +
                    "message='" + message + '\'' +
                    ", ntc=" + ntc +
                    ", ncell=" + ncell +
                    ", smartcell=" + smartcell +
                    '}';
        }
    }

    // For error responses
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class ErrorResponse {
        private final String message;
        private final int ntc;
        private final int ncell;
        private final int smartcell;
        private final int other;
        private final List<String> invalidNumber;

        @JsonCreator
        public ErrorResponse(
                @JsonProperty("message") String message,
                @JsonProperty("ntc") int ntc,
                @JsonProperty("ncell") int ncell,
                @JsonProperty("smartcell") int smartcell,
                @JsonProperty("other") int other,
                @JsonProperty("invalid_number") List<String> invalidNumber) {
            this.message = message;
            this.ntc = ntc;
            this.ncell = ncell;
            this.smartcell = smartcell;
            this.other = other;
            this.invalidNumber = invalidNumber;
        }

        public String getMessage() {
            return message;
        }

        @Override
        public String toString() {
            return "ErrorResponse{" +
                    "message='" + message + '\'' +
                    ", ntc=" + ntc +
                    ", ncell=" + ncell +
                    ", smartcell=" + smartcell +
                    ", other=" + other +
                    ", invalidNumber=" + invalidNumber +
                    '}';
        }
    }
    private static class OTPMessage {
        private String otp;
        private String phoneNumber;
        private long timestamp;

        public OTPMessage(String otp, String phoneNumber) {
            this.otp = otp;
            this.phoneNumber = phoneNumber;
            this.timestamp = System.currentTimeMillis(); // Store the current timestamp
        }

        // Parameterized constructor (optional)
        @JsonCreator
        public OTPMessage(@JsonProperty("otp") String otp,
                          @JsonProperty("phoneNumber") String phoneNumber,
                          @JsonProperty("timestamp") long timestamp) {
            this.otp = otp;
            this.phoneNumber = phoneNumber;
            this.timestamp = timestamp;
        }

        // Getters and Setters
        public String getOtp() {
            return otp;
        }

        public void setOtp(String otp) {
            this.otp = otp;
        }

        public String getPhoneNumber() {
            return phoneNumber;
        }

        public void setPhoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }
    }
}
