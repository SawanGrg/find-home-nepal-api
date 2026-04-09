package com.beta.FindHome.controller.auth;

import com.beta.FindHome.dto.SuccessResponseDTO;
import com.beta.FindHome.dto.exception.ErrorResponseDTO;
import com.beta.FindHome.utils.LogoutServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication API", description = "Endpoints for authentication and user session management")
public class LogoutController {

    private final static org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(LogoutController.class);

    private final LogoutServiceImpl logoutService;

    @Autowired
    public LogoutController(LogoutServiceImpl logoutService) {
        this.logoutService = logoutService;
    }

    @Operation(
            summary = "Logout user",
            description = "Logs out the currently authenticated user by invalidating the session token.",
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "User logged out successfully",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = SuccessResponseDTO.class))),
                    @ApiResponse(responseCode = "500",
                            description = "Internal server error")
            }
    )
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        try {
            String result = logoutService.logoutUser(request);
            if (result.contains("Logout successful. Token blacklisted.")) {
                return ResponseEntity.ok(new SuccessResponseDTO(result));
            }
            else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(new ErrorResponseDTO(
                                LocalDateTime.now(),
                                "Internal server error",
                                "Something went wrong during logout"
                        ));
            }
        } catch (Exception ex) {
            logger.error("Logout failed", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponseDTO(
                                    LocalDateTime.now(),
                                    "Internal server error",
                                    "Something went wrong during logout"
                            )
                    );


        }
    }
}
