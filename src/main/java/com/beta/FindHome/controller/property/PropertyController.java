package com.beta.FindHome.controller.property;

import com.beta.FindHome.dto.property.GetAllPropertyResponseDTO;
import com.beta.FindHome.dto.property.GetSpecificPropertyResponseDTO;
import com.beta.FindHome.dto.property.PropertyRequestDTO;
import com.beta.FindHome.exception.ResourceNotFoundException;
import com.beta.FindHome.service.property.PropertyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/public")
@Validated
@Tag(name = "Property Management", description = "APIs for managing and retrieving property information")
public class PropertyController {

    private static final Logger logger = LoggerFactory.getLogger(PropertyController.class);
    private static final String DEFAULT_PAGE_SIZE = "10";
    private static final String DEFAULT_PAGE_NUMBER = "0";

    private final PropertyService propertyService;

    @Autowired
    public PropertyController(PropertyService propertyService) {
        this.propertyService = propertyService;
    }

    @PostMapping("/all-properties")
    @Operation(
            summary = "Get paginated list of properties",
            description = "Retrieves a paginated list of properties based on search criteria"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved properties"),
            @ApiResponse(responseCode = "400", description = "Invalid input parameters"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Page<GetAllPropertyResponseDTO>> getProperties(
            @Valid @RequestBody PropertyRequestDTO propertyRequestDTO,
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(value = "page", defaultValue = DEFAULT_PAGE_NUMBER, required = false) int page,
            @Parameter(description = "Number of items per page", example = "10")
            @RequestParam(value = "size", defaultValue = DEFAULT_PAGE_SIZE, required = false) int size
    ) {
        logger.info("Received request for properties with criteria: {}", propertyRequestDTO);
        Page<GetAllPropertyResponseDTO> result = propertyService.getAllPropertyList(propertyRequestDTO, page, size);
        logger.debug("Returning {} properties on page {}", result.getNumberOfElements(), page);
        return ResponseEntity.ok(result);
    }

    @GetMapping(
            value = "/property/{id}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(
            summary = "Get property by ID",
            description = "Retrieves detailed information about a specific property"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved property"),
            @ApiResponse(responseCode = "400", description = "Invalid ID format"),
            @ApiResponse(responseCode = "404", description = "Property not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<GetSpecificPropertyResponseDTO> getProperty(
            @Parameter(description = "ID of the property to be retrieved", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable(value = "id") UUID id
    ) {
        logger.info("Received request for property with ID: {}", id);
        GetSpecificPropertyResponseDTO result = propertyService.getProperty(id);
        return ResponseEntity.ok(result);
    }
}
