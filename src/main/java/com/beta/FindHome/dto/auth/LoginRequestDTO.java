package com.beta.FindHome.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter
public class LoginRequestDTO {

    @NotBlank(message = "Username is required")
    @Size(min = 2, max = 100, message = "Username must be less than 100 characters")
    @Pattern(regexp = "^\\S+$", message = "Username must not contain whitespace")
    private String username;


    @NotBlank(message = "Password is required")
    @Length(min = 6, message = "Password must be at least 6 characters")
    private String password;
}
