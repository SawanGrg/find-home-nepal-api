package com.beta.FindHome.dto.user.admin;

import com.beta.FindHome.enums.model.OwnerRegistrationStatusType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OwnerRegistrationStatusDTO {

    private String username;
    private OwnerRegistrationStatusType status;
    private String message;

    public OwnerRegistrationStatusDTO(String username, OwnerRegistrationStatusType status, String message) {
        this.username = username;
        this.status = status;
        this.message = message;
    }

}
