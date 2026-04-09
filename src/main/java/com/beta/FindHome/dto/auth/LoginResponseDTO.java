package com.beta.FindHome.dto.auth;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginResponseDTO {

    private String message;
    private String token;
    private String username;

    public LoginResponseDTO() {
    }

    public LoginResponseDTO(String message,String username, String token) {
        this.message = message;
        this.username = username;
        this.token = token;
    }
}