package com.beta.FindHome.dto.user.owner;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterOwnerResponseDTO {

    private String message;
    private LocalDateTime instant;

    public RegisterOwnerResponseDTO(String message) {
        this.message = message;
    }

    public LocalDateTime getInstant() {
        return LocalDateTime.now();
    }
}
