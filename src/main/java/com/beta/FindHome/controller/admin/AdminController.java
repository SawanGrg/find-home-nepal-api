package com.beta.FindHome.controller.admin;

import com.beta.FindHome.controller.user.UserController;
import com.beta.FindHome.dto.user.ProfileResponseDTO;
import com.beta.FindHome.dto.user.UserDetailsListResponseDTO;
import com.beta.FindHome.dto.user.admin.OwnerVerificationDTO;
import com.beta.FindHome.service.user.UsersService;
import com.beta.FindHome.utils.MapperUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Page;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin")
@Tag(name = "Admin Controller API", description = "APIs for managing admin")
public class AdminController {

    private final UsersService userService;
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    public AdminController(
            UsersService userService
    ) {
        this.userService = userService;
    }

    @GetMapping("/owners")
    @Operation(
            summary = "Get all owners",
            description = "Retrieve a paginated list of all owners in the system",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Owners retrieved successfully",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = UserDetailsListResponseDTO.class))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid pagination parameters",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Internal server error",
                            content = @Content(mediaType = "application/json")
                    )
            }
    )
    public ResponseEntity<?> getAllOwners(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size
    ) {
        // Validate pagination parameters
        if (page < 0) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("error", "Page number cannot be negative"));
        }

        if (size <= 0 || size > 100) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("error", "Page size must be between 1 and 100"));
        }
        Page<UserDetailsListResponseDTO> ownersPage = userService.getAllOwnerDetails(page, size);
        return ResponseEntity
                .ok(ownersPage);
    }

    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size
    ) {
        // Validate pagination parameters
        if (page < 0) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("error", "Page number cannot be negative"));
        }
        if (size <= 0 || size > 100) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("error", "Page size must be between 1 and 100"));
        }
        Page<UserDetailsListResponseDTO> usersPage = userService.getAllUserDetails(page, size);
        return ResponseEntity
                .ok(usersPage);
    }

    //verify the owner registration and approve it
    @PutMapping("/owner/verification")
    @Operation(
            summary = "Approve owner registration",
            description = "Approve the registration of a new owner",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Owner registration approved successfully",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid owner ID provided",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Internal server error",
                            content = @Content(mediaType = "application/json")
                    )
            }
    )
    public ResponseEntity<?> approveOwnerRegistration(
            @Validated
            @RequestBody OwnerVerificationDTO ownerVerificationDTO
    ) {
        userService.approveOwnerRegistration(ownerVerificationDTO);
        return ResponseEntity
                .ok()
                .build();
    }

    @GetMapping("/{username}")
    @Operation(
            summary = "Get specific user details by username",
            description = "Retrieve details of a specific user by their username",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "User details retrieved successfully",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ProfileResponseDTO.class))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid username provided",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "User not found",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Internal server error",
                            content = @Content(mediaType = "application/json")
                    )
            }
    )
    public ResponseEntity<ProfileResponseDTO> getUserByUsername(
            @PathVariable(value = "username", required = true) String username
    ) {
        ProfileResponseDTO userDetails = userService.getUserDetails(username);
        return ResponseEntity
                .ok(userDetails);
    }
}