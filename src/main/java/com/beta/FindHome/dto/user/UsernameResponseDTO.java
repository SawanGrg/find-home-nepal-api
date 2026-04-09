package com.beta.FindHome.dto.user;

import java.time.LocalDateTime;
import java.util.List;

public class UsernameResponseDTO {
    private List<String> username;
    private String message;
    private LocalDateTime instant;

    // Constructor to initialize all fields
    public UsernameResponseDTO(List<String> username, String message) {
        this.username = username;
        this.message = message;
        this.instant = LocalDateTime.now(); // Assign the current time directly
    }

    // Default constructor
    public UsernameResponseDTO() {
        this.instant = LocalDateTime.now(); // Ensure instant is initialized
    }

    // Getters and Setters
    public List<String> getUsername() {
        return username;
    }

    public void setUsername(List<String> username) {
        this.username = username;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getInstant() {
        return instant;
    }

    public void setInstant(LocalDateTime instant) {
        this.instant = instant;
    }
}
