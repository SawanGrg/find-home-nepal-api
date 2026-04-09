package com.beta.FindHome.dto.user.admin;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class OwnerVerificationDTO {

    @NotNull(message = "Owner ID must not be null")
    private UUID ownerId;

    @Nullable()
    private String message;

    @NotNull(message = "Status must not be null")
    private Boolean status;

    public OwnerVerificationDTO(UUID ownerId, String message, Boolean status) {
        this.ownerId = ownerId;
        this.message = message;
        this.status = status;
    }


}

