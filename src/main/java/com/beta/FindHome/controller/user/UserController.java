package com.beta.FindHome.controller.user;

import com.beta.FindHome.dto.SuccessResponseDTO;
import com.beta.FindHome.dto.exception.ErrorResponseDTO;
import com.beta.FindHome.dto.user.*;
import com.beta.FindHome.exception.ResourceNotFoundException;
import com.beta.FindHome.service.user.UsersService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/user")
@Validated
@Slf4j
@Tag(name = "User Management", description = "APIs for managing user information")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UsersService userService;
    private final ObjectMapper objectMapper;

    @Autowired
    public UserController(UsersService userService, ObjectMapper objectMapper) {
        this.userService = userService;
        this.objectMapper = objectMapper;
    }

    @Operation(
            summary = "Get all usernames",
            description = "Retrieve a list of all usernames in the system",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Usernames retrieved successfully",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = UsernameResponseDTO.class))
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "No users found",
                            content = @Content(mediaType = "application/json")
                    )
            }
    )
    @GetMapping("/users")
    public ResponseEntity<UsernameResponseDTO> getAllUsers() {
        log.info("Received request to fetch all usernames");
        List<String> usernames = userService.getAllUsernames();
        log.debug("Returning {} usernames", usernames.size());
        return ResponseEntity
                .ok(new UsernameResponseDTO(usernames, "Users retrieved successfully"));
    }

    @GetMapping(
            value = "/{username}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(
            summary = "Get user profile",
            description = "Fetch user details for the authenticated user",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved user profile",
                    content = @Content(schema = @Schema(implementation = ProfileResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - invalid credentials"),
            @ApiResponse(responseCode = "403", description = "Forbidden - accessing other user's data"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("#username == authentication.principal.username")
    public ResponseEntity<ProfileResponseDTO> getUserProfile(
            @Parameter(description = "Username of the user to retrieve", required = true, example = "john_doe")
            @PathVariable String username
    ) {
            log.info("Fetching profile for user: {}", username);
            ProfileResponseDTO profile = userService.getUserDetails(username);
            return ResponseEntity
                    .ok(profile);
    }

    @PutMapping(
            value = "/{username}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(
            summary = "Update user profile",
            description = "Update profile information for the authenticated user",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully updated user profile",
                    content = @Content(schema = @Schema(implementation = ProfileResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - invalid credentials"),
            @ApiResponse(responseCode = "403", description = "Forbidden - updating other user's data",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "422", description = "Unprocessable Entity - validation errors",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    @PreAuthorize("#username == authentication.principal.username")
    public ResponseEntity<ProfileResponseDTO> updateUserProfile(
            @Parameter(description = "Username of the user to update", required = true, example = "john_doe")
            @PathVariable String username,
            @Valid
            @RequestPart(value = "user")
            String userDetailsRequestDTOString,
            @RequestPart(value = "citizenship_front", required = false)
            MultipartFile citizenship_front,
            @RequestPart(value = "citizenship_back", required = false)
            MultipartFile citizenship_back
    )
            throws JsonProcessingException
    {
        log.info("Updating profile for user: {}", username);
        // This will be caught by MethodArgumentNotValidException handler if validation fails
        UserDetailsRequestDTO userDetailsRequestDTO = objectMapper.readValue(userDetailsRequestDTOString, UserDetailsRequestDTO.class);
        ProfileResponseDTO updatedProfile = userService.updateUserDetails(
                username,
                userDetailsRequestDTO,
                citizenship_front,
                citizenship_back
        );
        log.debug("Successfully updated profile for user: {}", username);
        return ResponseEntity
                .ok(updatedProfile);
    }

    @PatchMapping(
            value = "/update/password",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<SuccessResponseDTO> updateForgotPassword(
            @RequestBody @Validated ChangePasswordRequestDTO changePasswordRequestDTO
    ) {
        userService.updatePassword(changePasswordRequestDTO);
        return ResponseEntity
                .ok(new SuccessResponseDTO("Password updated successfully"));
    }
}
