package com.beta.FindHome.dto.user;

import com.beta.FindHome.enums.model.Gender;
import com.beta.FindHome.enums.model.MaritalStatus;
import com.beta.FindHome.enums.model.RoleStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor  // Add this
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserDetailsListResponseDTO {

    private UUID id;
    private String firstName;
    private String middleName;
    private String lastName;
    private String userName;
    private String email;
    private String phoneNumber;
    private String profilePicture;
    private String citizenshipFront;
    private String citizenshipBack;
    private LocalDate dob;
    private Gender userGender;
    private MaritalStatus maritalStatus;
    private RoleStatus roleStatus;
    private Boolean isVerified;
    private LocalDateTime createdAt;
}
