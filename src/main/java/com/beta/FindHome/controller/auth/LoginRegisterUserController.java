package com.beta.FindHome.controller.auth;

import com.beta.FindHome.dto.SuccessResponseDTO;
import com.beta.FindHome.dto.auth.*;
import com.beta.FindHome.dto.user.ChangeForgotPasswordRequestDTO;
import com.beta.FindHome.service.user.UsersService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication API", description = "Endpoints for user registration, login, and authentication")
public class LoginRegisterUserController {

    private final UsersService userService;
    private final AuthenticationManager authenticationManager;

    @Autowired
    public LoginRegisterUserController(
            UsersService userService,
            AuthenticationManager authenticationManager
    ) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
    }

    @Operation(
            summary = "Register a new user",
            description = "Allows a user to register by providing their details. Returns a registration token.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "User registration successful",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = RegisterUserResponseDTO.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid input provided")
            })
    @PostMapping("/register")
    public ResponseEntity<RegisterUserResponseDTO> registerUser(
            @Valid @RequestBody RegisterUserRequestDTO userDTO
    ) {
        String token = userService.registerUser(userDTO);
        RegisterUserResponseDTO response = new RegisterUserResponseDTO("User registration successful. Please login.", token);
        return ResponseEntity.ok(response);
    }

    // functionality: --> initial the forgot password
    // user initiate the process with sends with phone number
    // response : --> endpoint will respond the token and sms otp
    @PostMapping("/otp/send/{phoneNumber}")
    public ResponseEntity<OTPSendResponseDTO> sendOTP(
            @PathVariable(required = true) @Validated String phoneNumber
    ) {
        String token = userService.sendOTP(phoneNumber);
        return ResponseEntity.ok(new OTPSendResponseDTO("OTP sent successfully.", token));
    }

    // functionality: --> verify the token for forgot password
    // user will send the token
    // and
    // otp from forgot password sms
    // response: --> endpoint will respond if the token is valid or not
    @Operation(
            summary = "Verify a token",
            description = "Checks if a given token is valid.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Token is valid",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = TokenVerificationResponseDTO.class))),
                    @ApiResponse(responseCode = "401", description = "Token is not valid")
            })
    @PostMapping("/token/verification/{token}")
    public ResponseEntity<TokenVerificationResponseDTO> verifyToken(
            @PathVariable @Validated String token,
            @RequestBody ForgotPasswordOTPRequestDTO forgotPasswordOTPRequestDTO
    ) {
        log.info("Verifying token: {}", token);
        TokenVerificationResponseDTO tokenVerificationResponseDTO = userService.verifyForgotPasswordOTPToken(token, forgotPasswordOTPRequestDTO);
        return ResponseEntity.ok(tokenVerificationResponseDTO);
    }

    // functionality: --> update the password after verifying the token
    // user will send the token
    // and
    // new password, confirm password
    // response: --> endpoint will respond if the password is updated or not
    @PatchMapping(
            value = "/update/forgot-password/{token}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<SuccessResponseDTO> updatePassword(
            @PathVariable(required = true, name = "token") @Validated String token,
            @RequestBody @Validated ChangeForgotPasswordRequestDTO changePasswordRequestDTO
    ) {
        userService.updateForgotPassword(token, changePasswordRequestDTO);
        return ResponseEntity
                .ok(new SuccessResponseDTO("Password updated successfully"));
    }

    //functionality: --> validate rsa256 token only
    @GetMapping("/token/validate/{token}")
    public ResponseEntity<SuccessResponseDTO> validateToken(
            @PathVariable(required = true, name = "token") @Validated String token
    ) {
        userService.verifyToken(token); // throws if invalid
        return ResponseEntity.ok(new SuccessResponseDTO("Token is valid"));
    }

    // functionality: --> normal user send otp for changing isVerified status to true
    @Operation(
            summary = "Verify OTP and Update User Status ( Normal User) ",
            description = "Verifies the provided OTP (One-Time Password) for a given phone number.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OTP is valid",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = OTPVerificationResponseDTO.class))),
                    @ApiResponse(responseCode = "401", description = "OTP is not valid")
            })
    @PostMapping("/otp/verification/{token}")
    public ResponseEntity<OTPVerificationResponseDTO> verifyOTP(
            @PathVariable @Validated String token,
            @RequestBody @Validated OTPVerificationRequestDTO otpRequest
    ) {
        boolean isValid = userService.verifyOTP(otpRequest.getOtp(), token);
        if (!isValid) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new OTPVerificationResponseDTO(false, "OTP is not valid"));
        }

        return ResponseEntity.ok(new OTPVerificationResponseDTO(true, "User verified successfully"));
    }

    @Operation(
            summary = "Resend OTP",
            description = "Resends the OTP for a given phone number if it's invalid or expired.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OTP sent successfully",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = OTPResendResponseDTO.class))),
                    @ApiResponse(responseCode = "401", description = "OTP sending failed")
            })
    @PostMapping("/otp/resend/{token}")
    public ResponseEntity<OTPResendResponseDTO> resendOTP(
            @PathVariable @Validated String token
    ) {
        boolean hasSent = userService.resendOTP(token);
        if (!hasSent) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new OTPResendResponseDTO("OTP Error."));
        }
        return ResponseEntity.ok(new OTPResendResponseDTO("OTP sent successfully."));
    }

    @Operation(
            summary = "User login",
            description = "Allows a user to log in with their credentials and returns a JWT token.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Login successful",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = LoginResponseDTO.class))),
                    @ApiResponse(responseCode = "401", description = "Invalid credentials")
            })
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> authenticate(
            @Valid @RequestBody LoginRequestDTO request
    ) {
        String token = userService.authenticateUser(request);
        String username = userService.getUserByUsername(request.getUsername());
        LoginResponseDTO response = new LoginResponseDTO(
                "Login successful",
                username,
                token
        );
        return ResponseEntity.ok(response);
    }

    private Authentication doAuthenticate(String username, String password) {
        try {
            //this authentication manager . authenticate will call the provider manager defined in the security config
            // as we are using using UsernamePasswordAuthenticationToken, it will calls or uses DaoAuthenticationProvider
            // provider manager will use dao authentication provider to authenticate the user
            //so the authenticate method will call the loadUserByUsername method of the UserDetailsService
           return authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("Invalid credentials, please check your username and password");
        }
    }
}
