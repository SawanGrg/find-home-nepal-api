package com.beta.FindHome.dto.auth;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OTPSendResponseDTO {

    private String message;
    private String token;

    public OTPSendResponseDTO(String message, String token) {
        this.message = message;
        this.token = token;
    }

}
