package com.beta.FindHome.dto.user.owner;

import com.beta.FindHome.enums.model.Gender;
import com.beta.FindHome.enums.model.MaritalStatus;
import com.beta.FindHome.enums.model.OwnerRegistrationStatusType;
import com.beta.FindHome.enums.model.RoleStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OwnerRejectedRegistrationResponseDTO {
    private String firstName;
    private String middleName;
    private String lastName;
    private String userName;
    private String email;
    private String password;
    private String confirmPassword;
    private String phoneNumber;
    private LocalDate dob;
    private Gender userGender;
    private MaritalStatus maritalStatus;
    private Boolean isVerified;
    private RoleStatus roleStatus;
    private String citizenshipFront;
    private String citizenshipBack;
    private String message;
    private OwnerRegistrationStatusType status;
}