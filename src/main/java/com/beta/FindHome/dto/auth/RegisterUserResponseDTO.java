package com.beta.FindHome.dto.auth;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class RegisterUserResponseDTO {
    
    private String message;
    private String token;
    private LocalDateTime instant;

    public RegisterUserResponseDTO(String message, String token) {
        this.message = message;
        this.token = token;
        this.instant = getInstant();
    }   

    public RegisterUserResponseDTO() {
    }

    public LocalDateTime getInstant() {
        return LocalDateTime.now();
    }

}
