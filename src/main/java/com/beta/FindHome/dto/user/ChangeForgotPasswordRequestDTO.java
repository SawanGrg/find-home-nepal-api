package com.beta.FindHome.dto.user;


import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
@Builder
public class ChangeForgotPasswordRequestDTO {
    private String newPassword;
    private String confirmPassword;
}
