package com.beta.FindHome.controller.owner;

import com.beta.FindHome.dto.user.ProfileResponseDTO;
import com.beta.FindHome.service.user.UsersService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Parameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/owner")
public class OwnerController {

    private final UsersService userService;
    private static final Logger logger = LoggerFactory.getLogger(OwnerController.class);
    private final ObjectMapper objectMapper;

    public OwnerController(
            UsersService userService,
            ObjectMapper objectMapper
    ) {
        this.userService = userService;
        this.objectMapper = objectMapper;
    }

    @GetMapping("/{username}")
    @PreAuthorize("#username == authentication.principal.username")
    public ResponseEntity<ProfileResponseDTO> getOwnerProfile(
            @Parameter(description = "Username of the user to retrieve", required = true, example = "john_doe")
            @PathVariable String username
    ) {
        logger.info("Fetching profile for user: {}", username);
        ProfileResponseDTO profile = userService.getOwnerUserDetails(username);
        return ResponseEntity.ok(profile);
    }
}
