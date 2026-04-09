package com.beta.FindHome.dto.exception;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

@Setter
@Getter
public class ErrorResponseDTO {

    private LocalDateTime timeStamp;
    private String message;
    private String details;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Map<String, String> errors;


    public ErrorResponseDTO(LocalDateTime timeStamp, String message, String details ) {
        this.timeStamp = timeStamp;
        this.message = message;
        this.details = details;
    }

    public ErrorResponseDTO(LocalDateTime timeStamp, String message, String details,Map<String, String> errors) {
        this.timeStamp = timeStamp;
        this.message = message;
        this.details = details;
        this.errors = errors;
    }

}

