package com.beta.FindHome.exception;

import com.beta.FindHome.dto.exception.ErrorResponseDTO;
import com.beta.FindHome.utils.ErrorLogUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {


    private void logError(HttpServletRequest request, Exception ex) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        String ipAddress = request.getRemoteAddr();
        String apiPath = request.getRequestURI();
        String errorMessage = ex.getMessage();

        // Log the error in Excel
        try {
            ErrorLogUtils.printLog(username, ipAddress, apiPath, errorMessage);
        } catch (IOException e) {
            e.printStackTrace();  // Handle logging error
        }
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleGlobalException(HttpServletRequest request, Exception ex) {
         logError(request, ex);

        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
                LocalDateTime.now(),
                ex.getMessage(),
                "An error occurred while processing the request",
                null
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // Handle validation errors for input validation
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> handleValidationExceptions(HttpServletRequest request, MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        logError(request, ex);  // Log validation exception

        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
                LocalDateTime.now(),
                "Input errors",
                "Validation failed",
                errors
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    //handle for 429 exception
    @ExceptionHandler(RequestNotPermitted.class)
    public  ResponseEntity<ErrorResponseDTO> handleDatabaseException(
            HttpServletRequest request,
            RequestNotPermitted ex
    ){
        logError(request, ex);  // Log database exception

        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
                LocalDateTime.now(),
                ex.getMessage(),
                "Rate limit exceeded. Please try again later.",
                null
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // Handle custom DatabaseException
    @ExceptionHandler(DatabaseException.class)
    public ResponseEntity<ErrorResponseDTO> handleDatabaseException(HttpServletRequest request, DatabaseException ex) {
        logError(request, ex);  // Log database exception

        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
                LocalDateTime.now(),
                ex.getMessage(),
                "Database error",
                null
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // Handle custom UserException
    @ExceptionHandler(UserException.class)
    public ResponseEntity<ErrorResponseDTO> handleUserException(HttpServletRequest request, UserException ex) {
        logError(request, ex);  // Log user exception

        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
                LocalDateTime.now(),
                ex.getMessage(),
                "User-related error",
                null
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);  // Changed from INTERNAL_SERVER_ERROR
    }

    // Handle no users found (custom exception)
    @ExceptionHandler(NoUsersFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleNoUsersFoundException(HttpServletRequest request, NoUsersFoundException ex) {
        logError(request, ex);  // Log no users found exception

        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
                LocalDateTime.now(),
                ex.getMessage(),
                "No users found",
                null
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    // Handle house not found (custom exception)
    @ExceptionHandler(HouseException.class)
    public ResponseEntity<ErrorResponseDTO> handleHouseException(HttpServletRequest request, HouseException ex) {
        logError(request, ex);  // Log house not found exception

        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
                LocalDateTime.now(),
                ex.getMessage(),
                "House not found",
                null
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(FlatException.class)
    public ResponseEntity<ErrorResponseDTO> handleFlatException(HttpServletRequest request, FlatException ex) {
        logError(request, ex);  // Log house not found exception

        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
                LocalDateTime.now(),
                ex.getMessage(),
                "Flat not found",
                null
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(RoomException.class)
    public ResponseEntity<ErrorResponseDTO> handleRoomException(HttpServletRequest request, RoomException ex) {
        logError(request, ex);  // Log house not found exception

        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
                LocalDateTime.now(),
                ex.getMessage(),
                "Room not found",
                null
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(AssetException.class)
    public ResponseEntity<ErrorResponseDTO> handleAssetException(HttpServletRequest request, AssetException ex) {
        logError(request, ex);  // Log asset exception

        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
                LocalDateTime.now(),
                ex.getMessage(),
                "Asset error",
                null
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(AreaException.class)
    public ResponseEntity<ErrorResponseDTO> handleHouseException(HttpServletRequest request, AreaException ex) {
        logError(request, ex);  // Log house exception

        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
                LocalDateTime.now(),
                ex.getMessage(),
                "House error",
                null
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(AmenitiesException.class)
    public ResponseEntity<ErrorResponseDTO> handleAmenitiesException(HttpServletRequest request, AmenitiesException ex) {
        logError(request, ex);  // Log amenities exception

        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
                LocalDateTime.now(),
                ex.getMessage(),
                "Amenities error",
                null
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponseDTO> handleConstraintViolationException(HttpServletRequest request, ConstraintViolationException ex) {
        logError(request, ex);  // Log constraint violation exception

        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
                LocalDateTime.now(),
                ex.getMessage(),
                "Constraint violation error",
                null
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConversationNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleConversationNotFoundException(HttpServletRequest request, ConversationNotFoundException ex) {
        logError(request, ex);  // Log conversation not found exception

        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
                LocalDateTime.now(),
                ex.getMessage(),
                "Conversation not found",
                null
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ParticipantAccessException.class)
    public ResponseEntity<ErrorResponseDTO> handleParticipantAccessException(HttpServletRequest request, ParticipantAccessException ex) {
        logError(request, ex);  // Log participant access exception

        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
                LocalDateTime.now(),
                ex.getMessage(),
                "Participant access error",
                null
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(DataNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleDataNotFoundException(HttpServletRequest request, DataNotFoundException ex) {
        logError(request, ex);  // Log the actual DataNotFoundException

        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
                LocalDateTime.now(),
                ex.getMessage(),
                "Data not found",
               null
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleEntityNotFoundException(HttpServletRequest request, EntityNotFoundException ex) {
        logError(request, ex);  // Log the actual EntityNotFoundException

        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
                LocalDateTime.now(),
                ex.getMessage(),
                "Entity not found",
                null
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(JsonProcessingException.class)
    public ResponseEntity<ErrorResponseDTO> handleJsonProcessingException(
            HttpServletRequest request,
            JsonProcessingException ex) {
        logError(request, ex);

        ErrorResponseDTO errorResponse;

        if (ex instanceof InvalidFormatException) {
            InvalidFormatException ife = (InvalidFormatException) ex;
            errorResponse = handleInvalidFormatException(ife);
        } else {
            errorResponse = new ErrorResponseDTO(
                    LocalDateTime.now(),
                    "Invalid JSON format",
                    "Failed to parse request body",
                    null
            );
        }
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    private ErrorResponseDTO handleInvalidFormatException(InvalidFormatException ex) {
        String fieldName = ex.getPath().stream()
                .map(JsonMappingException.Reference::getFieldName)
                .collect(Collectors.joining("."));

        String errorMessage;
        Object value = ex.getValue();

        if (ex.getTargetType().isEnum()) {
            Class<? extends Enum> enumClass = (Class<? extends Enum>) ex.getTargetType();
            errorMessage = String.format("Invalid value '%s' for %s. Accepted values are: %s",
                    value,
                    fieldName,
                    Arrays.toString(enumClass.getEnumConstants()));
        } else {
            errorMessage = String.format("Invalid format for field '%s'. Expected %s but got '%s'",
                    fieldName,
                    ex.getTargetType().getSimpleName(),
                    value);
        }

        Map<String, String> errors = new HashMap<>();
        errors.put(fieldName, errorMessage);

        return new ErrorResponseDTO(
                LocalDateTime.now(),
                "Validation error",
                "Invalid input format",
                errors
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponseDTO> handleIllegalArgumentException(
            HttpServletRequest request,
            IllegalArgumentException ex) {
        logError(request, ex);

        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
                LocalDateTime.now(),
                ex.getMessage(),
                "Invalid input provided",
                null
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponseDTO> handleAccessDeniedException(
            HttpServletRequest request,
            AccessDeniedException ex) {
        logError(request, ex);

        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
                LocalDateTime.now(),
                ex.getMessage(),
                "Access denied",
                null
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleResourceNotFoundException(
            HttpServletRequest request,
            ResourceNotFoundException ex) {
        logError(request, ex);

        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
                LocalDateTime.now(),
                ex.getMessage(),
                "Requested resource not found",
                null
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ErrorResponseDTO> handleDataAccessException(
            HttpServletRequest request,
            DataAccessException ex) {
        logError(request, ex);

        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
                LocalDateTime.now(),
                "Database operation failed",
                "Please try again later",
                null
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(FileProcessingException.class)
    public ResponseEntity<ErrorResponseDTO> handleFileProcessingException(
            HttpServletRequest request,
            FileProcessingException ex) {
        logError(request, ex);

        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
                LocalDateTime.now(),
                ex.getMessage(),
                "File processing error",
                null
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(DataPersistenceException.class)
    public ResponseEntity<ErrorResponseDTO> handleDataPersistenceException(
            HttpServletRequest request,
            DataPersistenceException ex) {
        logError(request, ex);

        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
                LocalDateTime.now(),
                ex.getMessage(),
                "Data persistence error",
                null
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(IOException.class)
    public ResponseEntity<ErrorResponseDTO> handleIOException(
            HttpServletRequest request,
            IOException ex) {
        logError(request, ex);

        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
                LocalDateTime.now(),
                ex.getMessage(),
                "I/O error",
                null
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(FileStorageException.class)
    public ResponseEntity<ErrorResponseDTO> handleFileStorageException(
            HttpServletRequest request,
            FileStorageException ex) {
        logError(request, ex);

        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
                LocalDateTime.now(),
                ex.getMessage(),
                "File storage error",
                null
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(RoleNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleRoleNotFoundException(
            HttpServletRequest request,
            RoleNotFoundException ex) {
        logError(request, ex);

        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
                LocalDateTime.now(),
                ex.getMessage(),
                "File storage error",
                null
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(TokenVerificationException.class)
    public ResponseEntity<ErrorResponseDTO> handleTokenVerificationException(
            HttpServletRequest request,
            TokenVerificationException ex) {
        logError(request, ex);

        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
                LocalDateTime.now(),
                ex.getMessage(),
                "Token verification error",
                null
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(BlogException.class)
    public ResponseEntity<ErrorResponseDTO> handleBlogException(
            HttpServletRequest request,
            BlogException ex) {
        logError(request, ex);

        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
                LocalDateTime.now(),
                ex.getMessage(),
                "Blog error",
                null
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}