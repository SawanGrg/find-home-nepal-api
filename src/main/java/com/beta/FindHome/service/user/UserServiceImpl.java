package com.beta.FindHome.service.user;

import com.beta.FindHome.dto.auth.ForgotPasswordOTPRequestDTO;
import com.beta.FindHome.dto.auth.LoginRequestDTO;
import com.beta.FindHome.dto.auth.RegisterUserRequestDTO;
import com.beta.FindHome.dto.auth.TokenVerificationResponseDTO;
import com.beta.FindHome.dto.user.*;
import com.beta.FindHome.dto.user.admin.OwnerVerificationDTO;
import com.beta.FindHome.dto.user.owner.OwnerRejectedRegistrationResponseDTO;
import com.beta.FindHome.dto.user.owner.RegisterOwnerRequestDTO;
import com.beta.FindHome.enums.model.OwnerRegistrationStatusType;
import com.beta.FindHome.events.publisher.OwnerVerificationEventPublisher;
import com.beta.FindHome.events.publisher.SMSEventPublisher;
import com.beta.FindHome.exception.*;
import com.beta.FindHome.factory.interfaces.ImageFactoryInterface;
import com.beta.FindHome.model.OwnerRegistrationStatus;
import com.beta.FindHome.model.Role;
import com.beta.FindHome.model.Users;
import com.beta.FindHome.model.security.UserPrincipal;
import com.beta.FindHome.repository.OwnerRegistrationStatusRepository;
import com.beta.FindHome.repository.RoleRepository;
import com.beta.FindHome.repository.UserRepository;
import com.beta.FindHome.utils.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@Service
public class UserServiceImpl implements UsersService, UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final OwnerRegistrationStatusRepository ownerRegistrationStatusRepository;
    private final PasswordEncoder passwordEncoder;
    private final RS256SignerService rs256SignerService;
    private final MessageServiceUtils messageServiceUtils;
    private final MapperUtil mapperUtil;
    private final EncryptionUtils encryptionUtils;
    private final RedisUtils redisUtils;
    private final JwtUtils jwtUtils;
    private final SMSEventPublisher smsEventPublisher;
    private final ImageFactoryInterface imageFactory;
    private final OwnerVerificationEventPublisher ownerVerificationEventPublisher;

    @Autowired
    public UserServiceImpl(
            UserRepository userRepository,
            RoleRepository roleRepository,
            OwnerRegistrationStatusRepository ownerRegistrationStatusRepository,
            PasswordEncoder passwordEncoder,
            RS256SignerService rs256SignerService,
            MessageServiceUtils messageServiceUtils,
            MapperUtil mapperUtil,
            EncryptionUtils encryptionUtils,
            RedisUtils redisUtils,
            JwtUtils jwtUtils,
            SMSEventPublisher smsEventPublisher,
            ImageFactoryInterface imageFactory,
            OwnerVerificationEventPublisher ownerVerificationEventPublisher
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.ownerRegistrationStatusRepository = ownerRegistrationStatusRepository;
        this.passwordEncoder = passwordEncoder;
        this.rs256SignerService = rs256SignerService;
        this.messageServiceUtils = messageServiceUtils;
        this.mapperUtil = mapperUtil;
        this.encryptionUtils = encryptionUtils;
        this.redisUtils = redisUtils;
        this.jwtUtils = jwtUtils;
        this.smsEventPublisher = smsEventPublisher;
        this.imageFactory = imageFactory;
        this.ownerVerificationEventPublisher = ownerVerificationEventPublisher;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Users user = userRepository.findByUserNameWithRoles(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        return UserPrincipal.from(user, user.getRoles());
    }

    @Override
    @Transactional(readOnly = true)
    public String authenticateUser(LoginRequestDTO loginRequestDTO) {
        Users user = userRepository.findByUserName(loginRequestDTO.getUsername());
        if (user == null) {
            user = userRepository.findByPhoneNumber(loginRequestDTO.getUsername());
        }
        if (user == null) {
            user = userRepository.findByEmail(loginRequestDTO.getUsername())
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        }

        if (!passwordEncoder.matches(loginRequestDTO.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Invalid credentials");
        }

        Set<Role> roles = roleRepository.findRolesByUsers(user);
        if (roles.isEmpty()) {
            throw new UserException("User has no assigned roles.");
        }

        if (!user.isVerified()) {
            String roleName = roles.iterator().next().getRoleName();
            throw new UserException(roleName + " is not verified.");
        }

        // Build UserPrincipal — correct UserDetails implementation
        UserPrincipal principal = UserPrincipal.from(user, roles);
        return jwtUtils.generateToken(principal);
    }

    // =====================================================================
    // AUTH

    // =====================================================================

    // =====================================================================
    // REGISTRATION
    // =====================================================================

    @Override
    @Transactional
    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 2000))
    public String registerUser(RegisterUserRequestDTO userDTO) {
        if (!userDTO.getPassword().equals(userDTO.getConfirmPassword())) {
            throw new UserException("Password and confirm password do not match.");
        }

        if (userRepository.findByEmail(userDTO.getEmail()).isPresent()) {
            throw new UserException("Email is already in use.");
        }
        if (userRepository.findByPhoneNumber(userDTO.getPhoneNumber()) != null) {
            throw new UserException("Phone number is already in use.");
        }
        if (userRepository.findByUserName(userDTO.getUserName()) != null) {
            throw new UserException("Username is already in use.");
        }

        Users user = mapperUtil.createUserMapper(userDTO);

        Role userRole = roleRepository.findByRoleName("USER")
                .orElseThrow(() -> new RoleNotFoundException("Role not found: USER"));
        user.setRoles(Set.of(userRole));

        userRepository.save(user);

        try {
            smsEventPublisher.publishLoginOTPMessageEvent(user.getPhoneNumber());
        } catch (Exception e) {
            logger.error("Failed to send OTP SMS for phone: {}", user.getPhoneNumber(), e);
            throw new UserException("Registration succeeded but failed to send OTP. Please retry.");
        }

        String token = rs256SignerService.generateJWT(user.getPhoneNumber());
        logger.info("User registered successfully: {}", user.getUserName());
        return token;
    }

    @Override
    @Transactional
    @Retryable(maxAttempts = 2, backoff = @Backoff(delay = 2000))
    public String registerOwner(
            RegisterOwnerRequestDTO userDTO,
            MultipartFile citizenshipFront,
            MultipartFile citizenshipBack
    ) {
        if (!userDTO.getPassword().equals(userDTO.getConfirmPassword())) {
            throw new UserException("Password and confirm password do not match.");
        }
        if (userRepository.findByEmail(userDTO.getEmail()).isPresent()) {
            throw new UserException("Email is already in use.");
        }
        if (userRepository.findByPhoneNumber(userDTO.getPhoneNumber()) != null) {
            throw new UserException("Phone number is already in use.");
        }
        if (userRepository.findByUserName(userDTO.getUserName()) != null) {
            throw new UserException("Username is already in use.");
        }

        Users user = mapperUtil.createOwnerMapper(userDTO);

        Role ownerRole = roleRepository.findByRoleName("OWNER")
                .orElseThrow(() -> new RoleNotFoundException("Role not found: OWNER"));
        user.setRoles(Set.of(ownerRole));

        user.setCitizenshipFront(imageFactory.save(citizenshipFront));
        user.setCitizenshipBack(imageFactory.save(citizenshipBack));

        userRepository.save(user);
        logger.info("Owner registered successfully: {}", user.getUserName());
        return "Owner registration successful.";
    }

    // =====================================================================
    // OTP & TOKEN
    // =====================================================================

    @Override
    @Transactional
    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 2000))
    public Boolean verifyToken(String token) {
        try {
            rs256SignerService.verifyJWT(token);
            return true;
        } catch (IllegalArgumentException e) {
            throw new TokenVerificationException("Token verification failed: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 2000))
    public Boolean verifyOTP(String otp, String token) {
        try {
            String phoneNumber = rs256SignerService.extractUserPhoneNumber(token);
            if (phoneNumber == null) {
                throw new UserException("Invalid token.");
            }

            if (!messageServiceUtils.verifyOTP(otp, phoneNumber)) {
                return false;
            }

            Users user = userRepository.findByPhoneNumber(phoneNumber);
            if (user == null) {
                throw new UserException("User not found for phone: " + phoneNumber);
            }

            user.setVerified(true);
            userRepository.save(user);
            return true;
        } catch (UserException e) {
            throw e;
        } catch (Exception e) {
            logger.error("OTP verification failed", e);
            return false;
        }
    }

    @Override
    @Transactional
    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 2000))
    public String sendOTP(String phoneNumber) {
        Users user = userRepository.findByPhoneNumber(phoneNumber);
        if (user == null) {
            throw new UserException("User not found for phone: " + phoneNumber);
        }

        smsEventPublisher.publishForgotPasswordOTPMessageEvent(phoneNumber);
        String token = rs256SignerService.generateJWT(phoneNumber);
        logger.info("OTP sent and token generated for phone: {}", phoneNumber);
        return token;
    }

    @Override
    @Transactional
    public TokenVerificationResponseDTO verifyForgotPasswordOTPToken(
            String token,
            ForgotPasswordOTPRequestDTO otpRequest
    ) {
        String phoneNumber = rs256SignerService.extractUserPhoneNumber(token);
        if (phoneNumber == null) {
            throw new UserException("Invalid token.");
        }

        if (!redisUtils.keyExists(phoneNumber)) {
            throw new UserException("OTP has expired.");
        }

        if (!messageServiceUtils.verifyOTP(otpRequest.getOtp(), phoneNumber)) {
            return new TokenVerificationResponseDTO(null, false, "OTP is not valid.");
        }

        String newToken = rs256SignerService.generateJWT(phoneNumber);
        logger.info("Forgot password OTP verified for phone: {}", phoneNumber);
        return new TokenVerificationResponseDTO(newToken, true, "OTP is valid.");
    }

    @Override
    @Transactional
    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 2000))
    public Boolean resendOTP(String token) {
        String phoneNumber = rs256SignerService.extractUserPhoneNumber(token);
        if (phoneNumber == null) {
            throw new UserException("Invalid token.");
        }
        try {
            smsEventPublisher.publishLoginOTPMessageEvent(phoneNumber);
            return true;
        } catch (Exception e) {
            logger.error("Failed to resend OTP for phone: {}", phoneNumber, e);
            return false;
        }
    }

    // =====================================================================
    // USER PROFILE
    // =====================================================================

    @Override
    @Transactional(readOnly = true)
    public String getUserByUsername(String username) {
        Users user = userRepository.findByUserName(username);
        if (user == null) {
            user = userRepository.findByPhoneNumber(username);
        }
        if (user == null) {
            user = userRepository.findByEmail(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        }
        return user.getUserName();
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getAllUsernames() {
        List<String> usernames = userRepository.getAllVerifiedUsernames();
        if (usernames.isEmpty()) {
            throw new NoUsersFoundException("No verified users found.");
        }
        return usernames;
    }

    @Override
    @Transactional(readOnly = true)
    public ProfileResponseDTO getUserDetails(String userName) {
        Users user = userRepository.findByUserName(userName);
        if (user == null) {
            throw new UserException("User not found: " + userName);
        }
        return mapperUtil.createUserProfileDTO(user);
    }

    @Override
    @Transactional(readOnly = true)
    public ProfileResponseDTO getOwnerUserDetails(String userName) {
        Users user = userRepository.findByUserName(userName);
        if (user == null) {
            throw new UserException("User not found: " + userName);
        }
        return mapperUtil.createOwnerUserProfileDTO(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Users getUserDetailsById(UUID id) {
        Users user = userRepository.findById(id)
                .orElseThrow(() -> new UserException("User not found."));

        String principal = SecurityContextHolder.getContext()
                .getAuthentication().getName();

        if (!principal.equals(user.getUserName())) {
            throw new AccessDeniedException("Cannot access another user's details.");
        }
        return user;
    }

    @Override
    @Transactional
    public ProfileResponseDTO updateUserDetails(
            String userName,
            UserDetailsRequestDTO userDetailsRequestDTO,
            MultipartFile citizenshipFront,
            MultipartFile citizenshipBack
    ) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Authentication required");
        }

        if (!authentication.getName().equals(userName)) {
            throw new AccessDeniedException("Cannot update another user's details.");
        }

        Users user = userRepository.findByUserName(userName);
        if (user == null) {
            throw new ResourceNotFoundException("User not found: " + userName);
        }

        // Username uniqueness check
        if (userDetailsRequestDTO.getUserName() != null) {
            Users existing = userRepository.findByUserName(userDetailsRequestDTO.getUserName());
            if (existing != null && !existing.getId().equals(user.getId())) {
                throw new UserException("Username already in use.");
            }
        }

        // Phone uniqueness check
        if (userDetailsRequestDTO.getPhoneNumber() != null) {
            Users existing = userRepository.findByPhoneNumber(userDetailsRequestDTO.getPhoneNumber());
            if (existing != null && !existing.getId().equals(user.getId())) {
                throw new UserException("Phone number already in use.");
            }
        }

        // Email uniqueness check
        if (userDetailsRequestDTO.getEmail() != null) {
            userRepository.findByEmail(userDetailsRequestDTO.getEmail()).ifPresent(existing -> {
                if (!existing.getId().equals(user.getId())) {
                    throw new UserException("Email already in use.");
                }
            });
        }

        if (citizenshipFront != null) {
            user.setCitizenshipFront(imageFactory.save(citizenshipFront));
        }
        if (citizenshipBack != null) {
            user.setCitizenshipBack(imageFactory.save(citizenshipBack));
        }

        Users updatedUser = mapperUtil.updateUserMapper(user, userDetailsRequestDTO);
        userRepository.save(updatedUser);
        return mapperUtil.createUserProfileDTO(updatedUser);
    }

    @Override
    @Transactional
    public ProfileResponseDTO updateOwnerDetails(
            String userName,
            UserDetailsRequestDTO userDetailsRequestDTO,
            MultipartFile citizenshipFront,
            MultipartFile citizenshipBack
    ) {
        Users user = userRepository.findByUserName(userName);
        if (user == null) {
            throw new UserException("User not found: " + userName);
        }

        if (citizenshipFront != null) {
            user.setCitizenshipFront(imageFactory.save(citizenshipFront));
        }
        if (citizenshipBack != null) {
            user.setCitizenshipBack(imageFactory.save(citizenshipBack));
        }

        Users updatedUser = mapperUtil.updateUserMapper(user, userDetailsRequestDTO);
        userRepository.save(updatedUser);
        return mapperUtil.createUserProfileDTO(updatedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserDetailsListResponseDTO> getAllOwnerDetails(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return userRepository.findAllByRoles_RoleName("OWNER", pageable)
                .map(mapperUtil::convertToUserDetailsListResponseDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserDetailsListResponseDTO> getAllUserDetails(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return userRepository.findAllByRoles_RoleName("USER", pageable)
                .map(mapperUtil::convertToUserDetailsListResponseDTO);
    }

    // =====================================================================
    // PASSWORD
    // =====================================================================

    @Override
    @Transactional
    public void updatePassword(ChangePasswordRequestDTO dto) {
        String userName = SecurityContextHolder.getContext().getAuthentication().getName();

        Users user = userRepository.findByUserName(userName);
        if (user == null) {
            throw new UserException("User not found.");
        }

        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
            throw new UserException("Old password is incorrect.");
        }
        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            throw new UserException("New password and confirmation do not match.");
        }
        if (passwordEncoder.matches(dto.getNewPassword(), user.getPassword())) {
            throw new UserException("New password must be different from old password.");
        }

        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userRepository.save(user);
        logger.info("Password updated for user: {}", userName);
    }

    @Override
    @Transactional
    public void updateForgotPassword(String token, ChangeForgotPasswordRequestDTO dto) {
        String phoneNumber = rs256SignerService.extractUserPhoneNumber(token);
        if (phoneNumber == null) {
            throw new UserException("Invalid token.");
        }

        Users user = userRepository.findByPhoneNumber(phoneNumber);
        if (user == null) {
            throw new UserException("User not found.");
        }

        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            throw new UserException("New password and confirmation do not match.");
        }

        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userRepository.save(user);
        logger.info("Forgot password updated for phone: {}", phoneNumber);
    }

    // =====================================================================
    // OWNER VERIFICATION
    // =====================================================================

    @Override
    @Transactional
    public boolean approveOwnerRegistration(OwnerVerificationDTO dto) {
        if (dto.getOwnerId() == null) {
            throw new IllegalArgumentException("Owner ID cannot be null");
        }

        Users user = userRepository.findById(dto.getOwnerId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with ID: " + dto.getOwnerId()));

        boolean isOwner = user.getRoles().stream()
                .anyMatch(role -> "OWNER".equals(role.getRoleName()));
        if (!isOwner) {
            throw new IllegalArgumentException("User is not an owner.");
        }

        if (user.isVerified()) {
            throw new IllegalArgumentException("Owner is already verified.");
        }

        if (Boolean.TRUE.equals(dto.getStatus())) {
            user.setVerified(true);
            userRepository.save(user);

            String message = buildApprovalMessage(user, dto.getMessage());
            ownerVerificationEventPublisher.publishOwnerVerificationMessageEvent(
                    message, user.getPhoneNumber());
            logger.info("Owner approved: {}", user.getUserName());
            return true;

        } else {
            String reApplyUrl = "https://www.findhomenepal.com/become-a-host/owner-register?token="
                    + encryptionUtils.encrypt(user.getUserName());

            String message = buildRejectionMessage(user, dto.getMessage(), reApplyUrl);

            OwnerRegistrationStatus status = new OwnerRegistrationStatus(
                    OwnerRegistrationStatusType.REJECTED,
                    dto.getMessage(),
                    user
            );
            ownerRegistrationStatusRepository.save(status);
            ownerVerificationEventPublisher.publishOwnerVerificationMessageEvent(
                    message, user.getPhoneNumber());
            logger.info("Owner rejected: {}", user.getUserName());
            return false;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public OwnerRejectedRegistrationResponseDTO ownerTokenVerification(String token) {
        String username = encryptionUtils.decrypt(token);

        Users user = userRepository.findByUserName(username);
        if (user == null) {
            throw new UserException("User not found.");
        }

        boolean isOwner = user.getRoles().stream()
                .anyMatch(role -> "OWNER".equals(role.getRoleName()));
        if (!isOwner) {
            throw new UserException("User is not an owner.");
        }

        if (user.isVerified()) {
            throw new UserException("User is already verified.");
        }

        return getRejectionDetails(username);
    }

    @Transactional(readOnly = true)
    public OwnerRejectedRegistrationResponseDTO getRejectionDetails(String username) {
        OwnerRegistrationStatus status = ownerRegistrationStatusRepository
                .findByUserUserName(username)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No registration status found for: " + username));

        Users user = status.getUser();
        return OwnerRejectedRegistrationResponseDTO.builder()
                .firstName(user.getFirstName())
                .middleName(user.getMiddleName())
                .lastName(user.getLastName())
                .userName(user.getUserName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .dob(user.getDob())
                .userGender(user.getUserGender())
                .maritalStatus(user.getMaritalStatus())
                .isVerified(user.isVerified())
                .citizenshipFront(user.getCitizenshipFront())
                .citizenshipBack(user.getCitizenshipBack())
                .message(status.getMessage())
                .roleStatus(user.getRoleStatus())
                .status(status.getStatus())
                .build();
    }

    @Transactional
    public OwnerRegistrationStatus saveOrUpdateOwnerStatus(OwnerRegistrationStatus status) {
        ownerRegistrationStatusRepository
                .findByUserUserName(status.getUser().getUserName())
                .ifPresent(existing -> status.setId(existing.getId()));
        return ownerRegistrationStatusRepository.save(status);
    }

    // =====================================================================
    // PRIVATE HELPERS
    // =====================================================================

    private String buildApprovalMessage(Users user, String note) {
        return "Find Home Nepal Registration Approved\n"
                + "Owner: " + user.getFirstName().toUpperCase()
                + " " + user.getLastName().toUpperCase() + "\n"
                + (note != null && !note.isBlank() ? "Note: " + note.trim() + "\n" : "")
                + "You can now list your property on our platform.\n"
                + "Thank you for choosing Find Home Nepal!";
    }

    private String buildRejectionMessage(Users user, String reason, String url) {
        return "Find Home Nepal Registration Rejected\n"
                + "Owner: " + user.getFirstName().toUpperCase()
                + " " + user.getLastName().toUpperCase() + "\n"
                + "Reason: " + (reason != null && !reason.isBlank()
                ? reason.trim()
                : "Submitted details were incomplete or invalid.") + "\n"
                + "Please review and re-submit.\n"
                + "Re-apply here: " + url;
    }
}