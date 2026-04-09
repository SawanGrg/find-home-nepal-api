package com.beta.FindHome.dto.user;

import com.beta.FindHome.enums.model.Gender;
import com.beta.FindHome.enums.model.MaritalStatus;
import com.beta.FindHome.enums.model.RoleStatus;
import jakarta.annotation.Nullable;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@Setter
@Getter
public class UserDetailsRequestDTO {
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
}
