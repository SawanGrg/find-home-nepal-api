package com.beta.FindHome.controller.auth;

import com.beta.FindHome.dto.user.ProfileResponseDTO;
import com.beta.FindHome.dto.user.UserDetailsRequestDTO;
import com.beta.FindHome.dto.user.owner.OwnerRejectedRegistrationResponseDTO;
import com.beta.FindHome.dto.user.owner.RegisterOwnerRequestDTO;
import com.beta.FindHome.dto.user.owner.RegisterOwnerResponseDTO;
import com.beta.FindHome.service.user.UsersService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication API", description = "Endpoints for owner registration, login, and authentication")
public class LoginRegisterOwnerController {

    private final UsersService userService;
    private static final Logger logger = LoggerFactory.getLogger(LoginRegisterOwnerController.class);
    private final ObjectMapper objectMapper;

    @Autowired
    public LoginRegisterOwnerController(
            UsersService userService,
            ObjectMapper objectMapper
    ) {
        this.userService = userService;
        this.objectMapper = objectMapper;
    }

    @Operation(
            summary = "Register a new owner",
            description = "Allows a user to register by providing their details.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Owner registration successful",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = RegisterOwnerResponseDTO.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid input provided")
            })
    @PostMapping(value = "/owner/register", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<RegisterOwnerResponseDTO> registerUser(
            @Valid @RequestPart(value = "owner", required = true)
            String userDTO,
            @RequestPart(value = "citizenship_front", required = false)
            MultipartFile citizenshipFront,
            @RequestPart(value = "citizenship_back", required = false)
            MultipartFile citizenshipBack
    )
            throws JsonProcessingException
    {
        RegisterOwnerRequestDTO ownerRequestDTO = objectMapper.readValue(userDTO, RegisterOwnerRequestDTO.class);
        String message = userService.registerOwner(ownerRequestDTO, citizenshipFront,citizenshipBack);
        if (!message.equals("Owner registration successful.")) {
                return ResponseEntity
                        .badRequest()
                        .body(new RegisterOwnerResponseDTO(message));
        }
        return ResponseEntity
                .ok()
                .body(new RegisterOwnerResponseDTO(message));
    }

    //check owner token
    @GetMapping("/owner/token/verification")
    public ResponseEntity<OwnerRejectedRegistrationResponseDTO> checkOwnerToken(
            @RequestParam(value = "token", required = true)
            String token
    ) {
        logger.info("Received token verification request for token: {}", token);

        if (StringUtils.isEmpty(token)) {
                return ResponseEntity
                        .badRequest()
                        .build();
        }
        OwnerRejectedRegistrationResponseDTO response = userService.ownerTokenVerification(token);
        if (response == null) {
                logger.warn("Token verification failed - no matching record found");
                return ResponseEntity
                        .notFound()
                        .build();
        }
        return ResponseEntity
                .ok(response);
    }

    @PutMapping(
            value = "/owner/update",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ProfileResponseDTO> updateOwnerProfile(
            @Parameter(description = "Token for verification", required = true, example = "token123")
            @RequestParam(value = "token", required = true)
            String token,
            @Valid @RequestPart(value = "owner")
            String ownerDetailsRequestDTOString,
            @RequestPart(value = "citizenship_front", required = false)
            MultipartFile citizenship_front,
            @RequestPart(value = "citizenship_back", required = false)
            MultipartFile citizenship_back
    )
            throws JsonProcessingException
    {
        if (token == null || token.isEmpty()) {
                return ResponseEntity.badRequest().build();
        }
        OwnerRejectedRegistrationResponseDTO response = userService.ownerTokenVerification(token);
        if (response == null) {
                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .build();
        }
        UserDetailsRequestDTO ownerDetailsRequestDTO = objectMapper.readValue(ownerDetailsRequestDTOString, UserDetailsRequestDTO.class);
        ProfileResponseDTO updatedProfile = userService.updateOwnerDetails(
                    ownerDetailsRequestDTO.getUserName(),
                    ownerDetailsRequestDTO,
                    citizenship_front,
                    citizenship_back
        );
        return ResponseEntity
                .ok(updatedProfile);
    }
}
