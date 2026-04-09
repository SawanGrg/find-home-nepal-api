package com.beta.FindHome.dto.auth;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
public class TokenVerificationResponseDTO {

    private Boolean isValid;
    private String message;
    private String token;
    private LocalDateTime instant;

    public TokenVerificationResponseDTO(String token, Boolean isValid, String message) {
        this.token = token;
        this.isValid = isValid;
        this.message = message;
        this.instant = getInstant();
    }

    public TokenVerificationResponseDTO() {
    }
    public LocalDateTime getInstant() {
        return LocalDateTime.now();
    }
}
