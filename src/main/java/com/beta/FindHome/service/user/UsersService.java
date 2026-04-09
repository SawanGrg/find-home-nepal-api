package com.beta.FindHome.service.user;

import com.beta.FindHome.dto.auth.ForgotPasswordOTPRequestDTO;
import com.beta.FindHome.dto.auth.LoginRequestDTO;
import com.beta.FindHome.dto.auth.RegisterUserRequestDTO;
import com.beta.FindHome.dto.auth.TokenVerificationResponseDTO;
import com.beta.FindHome.dto.user.*;
import com.beta.FindHome.dto.user.admin.OwnerVerificationDTO;
import com.beta.FindHome.dto.user.owner.OwnerRejectedRegistrationResponseDTO;
import com.beta.FindHome.dto.user.owner.RegisterOwnerRequestDTO;
import com.beta.FindHome.model.Users;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;
import com.beta.FindHome.model.OwnerRegistrationStatus;


public interface UsersService {

    // --- Auth ---
    String authenticateUser(LoginRequestDTO loginRequestDTO);

    // --- Registration ---
    String registerUser(RegisterUserRequestDTO userDTO);
    String registerOwner(RegisterOwnerRequestDTO userDTO,
                         MultipartFile citizenshipFront,
                         MultipartFile citizenshipBack);

    // --- OTP & Token ---
    Boolean verifyToken(String token);
    Boolean verifyOTP(String otp, String token);
    String sendOTP(String phoneNumber);
    TokenVerificationResponseDTO verifyForgotPasswordOTPToken(String token,
                                                              ForgotPasswordOTPRequestDTO otpRequest);
    Boolean resendOTP(String token);

    // --- User Profile ---
    String getUserByUsername(String username);
    List<String> getAllUsernames();
    ProfileResponseDTO getUserDetails(String userName);
    ProfileResponseDTO getOwnerUserDetails(String userName);
    Users getUserDetailsById(UUID id);

    ProfileResponseDTO updateUserDetails(String userName,
                                         UserDetailsRequestDTO dto,
                                         MultipartFile citizenshipFront,
                                         MultipartFile citizenshipBack);

    ProfileResponseDTO updateOwnerDetails(String userName,
                                          UserDetailsRequestDTO dto,
                                          MultipartFile citizenshipFront,
                                          MultipartFile citizenshipBack);

    Page<UserDetailsListResponseDTO> getAllOwnerDetails(int page, int size);
    Page<UserDetailsListResponseDTO> getAllUserDetails(int page, int size);

    // --- Password ---
    void updatePassword(ChangePasswordRequestDTO dto);
    void updateForgotPassword(String token, ChangeForgotPasswordRequestDTO dto);

    // --- Owner Verification ---
    boolean approveOwnerRegistration(OwnerVerificationDTO dto);
    OwnerRejectedRegistrationResponseDTO ownerTokenVerification(String token);
    OwnerRejectedRegistrationResponseDTO getRejectionDetails(String username);
    OwnerRegistrationStatus saveOrUpdateOwnerStatus(OwnerRegistrationStatus status);
}


//public interface UsersService {
//
//
//
//
//    /**
//     * Authenticates a user based on the provided login data.
//     *
//     * @param loginRequestDTO the login data for the user.
//     * @return a JWT token for the authenticated user.
//     */
//    String authenticateUser(LoginRequestDTO loginRequestDTO);
//
//    public String getUserByUsername(String username);
//
//
//    /**
//     * Fetches a list of all usernames.
//     *
//     * @return a list of usernames.
//     */
//    List<String> getAllUsernames();
//
//    /**
//     * Registers a new user based on the provided registration data.
//     *
//     * @param userDTO the registration data for the user.
//     * @return a JWT token for the registered user.
//     */
//    String registerUser(RegisterUserRequestDTO userDTO);
//
//    /**
//     * Registers a new owner based on the provided registration data.
//     *
//     * @param userDTO the registration data for the user.
//     */
//    String registerOwner(RegisterOwnerRequestDTO userDTO, MultipartFile citizenship_front, MultipartFile citizenship_back);
//
//
//    /**
//     * Verifies the validity of a given token.
//     *
//     * @param token the token to verify.
//     * @return true if the token is valid, false otherwise.
//     */
//    Boolean verifyToken(String token);
//
//    /**
//     * Verifies the OTP for a given token
//     * where extract phone number from the token.
//     * and checking in redis
//     *
//     */
//    TokenVerificationResponseDTO verifyForgotPasswordOTPToken(String token, ForgotPasswordOTPRequestDTO otp);
//
//    /**
//     * Verifies the OTP for a given phone number.
//     *
//     * @param otp the OTP to verify.
//     * @param token the phone number associated with the OTP.
//     * @return true if the OTP is valid, false otherwise.
//     */
//    Boolean verifyOTP(String otp, String token);
//
//    /**
//     * Sends the OTP to a given phone number.
//     *
//     * @param phoneNumber the phone number to resend the OTP to.
//     * @return true if the OTP was sent successfully, false otherwise.
//     */
//    String sendOTP(String phoneNumber);
//
//    /**
//     * Resends the OTP to a given phone number.
//     *
//     * @param phoneNumber the phone number to resend the OTP to.
//     * @return true if the OTP was sent successfully, false otherwise.
//     */
//    Boolean resendOTP(String phoneNumber);
//
//    /**
//     * Loads a user by username.
//     *
//     * @param username the username of the user to load.
//     * @return the user details of the loaded user.
//     */
//    UserDetails loadUserByUsername(String username);
//
//    /**
//     * get user details by username
//     *
//     * @return profile details of the user
//     */
//    ProfileResponseDTO getUserDetails(String userName);
//
//    public ProfileResponseDTO getOwnerUserDetails(
//            String userName
//    );
//    /**
//     * get user details by username
//     *
//     * @return profile details of the user
//     */
//    Users getUserDetailsById(UUID id);
//
//    /**
//     * update user details
//     *
//     * @return updated profile details of the user
//     */
//    ProfileResponseDTO updateUserDetails(String userName, UserDetailsRequestDTO userDetailsRequestDTO, MultipartFile citizenship_front,
//                                         MultipartFile citizenship_back);
//
//    ProfileResponseDTO updateOwnerDetails(String userName, UserDetailsRequestDTO userDetailsRequestDTO, MultipartFile citizenship_front,
//                                         MultipartFile citizenship_back);
//
//
//    /**
//     * get all owner details
//     *
//     * @return list of all owner details
//     */
//    Page<UserDetailsListResponseDTO> getAllOwnerDetails(int page, int size);
//
//    Page<UserDetailsListResponseDTO> getAllUserDetails(int page, int size);
//
//
//    /**
//     * approve owner registration
//     *
//     * @return true if owner registration is approved
//     */
//    boolean approveOwnerRegistration(OwnerVerificationDTO ownerVerificationDTO);
//
//    /**
//     * verify owner token
//     */
//    OwnerRejectedRegistrationResponseDTO ownerTokenVerification(String token);
//
//    /**
//     * update user password
//     *
//     * @return void
//     */
//    void updatePassword(ChangePasswordRequestDTO changePasswordRequestDTO);
//
//    void updateForgotPassword(String token, ChangeForgotPasswordRequestDTO changeForgotPasswordRequestDTO);
//}
