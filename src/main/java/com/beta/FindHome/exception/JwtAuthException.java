package com.beta.FindHome.exception;

import com.beta.FindHome.dto.exception.ErrorResponseDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;
import java.time.LocalDateTime;

public class JwtAuthException implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest httpServletRequest,
                         HttpServletResponse httpServletResponse,
                         AuthenticationException e
    ) throws IOException {

        // Create an instance of ErrorResponseDTO
        ErrorResponseDTO errorResponseDTO = new ErrorResponseDTO(
                LocalDateTime.now(),   // Use LocalDateTime.now() for the timestamp
                "UNAUTHORIZED",        // Set the error message as UNAUTHORIZED
                e.getMessage()          // Set the details as the exception message
        );

        // Set the response properties
        httpServletResponse.setContentType("application/json");
        httpServletResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        // Create an ObjectMapper for converting the ErrorResponseDTO to JSON
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        // Write the ErrorResponseDTO as JSON to the response output stream
        httpServletResponse.getOutputStream().println(
                objectMapper.writeValueAsString(errorResponseDTO)
        );
    }
}
