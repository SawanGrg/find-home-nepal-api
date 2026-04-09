package com.beta.FindHome.controller.flat;

import com.beta.FindHome.annotations.MaxListSize;
import com.beta.FindHome.dto.property.flat.AddFlatRequestDTO;
import com.beta.FindHome.dto.SuccessResponseDTO;
import com.beta.FindHome.dto.property.flat.UpdateFlatRequestDTO;
import com.beta.FindHome.exception.ResourceNotFoundException;
import com.beta.FindHome.service.flat.FlatService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/flat")
@Validated
@Tag(name = "Flat Management", description = "APIs for managing property flats")
//@SecurityRequirement(name = "bearerAuth")
public class FlatController {

    private final FlatService flatService;
    private final ObjectMapper objectMapper;

    @Autowired
    public FlatController(
            ObjectMapper objectMapper,
            FlatService flatService
    ) {
        this.flatService = flatService;
        this.objectMapper = objectMapper;
    }

    @PostMapping(
            value = "/add-flat",
            consumes = {MediaType.MULTIPART_FORM_DATA_VALUE},
            produces = {MediaType.APPLICATION_JSON_VALUE}
    )
    @PreAuthorize("hasAuthority('OWNER')")
    @Operation(
            summary = "Add a new flat",
            description = "Add a new flat with images and optional video. Requires PROPERTY_OWNER role.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Flat created successfully",
                    content = @Content(schema = @Schema(implementation = SuccessResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input or file type"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions"),
            @ApiResponse(responseCode = "413", description = "Payload too large"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<SuccessResponseDTO> addFlat(
            @Valid
            @RequestPart(value = "flat", required = true)
            String flatRequestDTOString,
            @RequestPart(value = "imageUrls", required = true)
            @MaxListSize(max = 5, message = "Max 5 images allowed per flat.")
            List<MultipartFile> imageUrls,
            @RequestPart(value = "videoUrl", required = false)
            MultipartFile videoUrl
    )
            throws JsonProcessingException
    {
        AddFlatRequestDTO flatRequestDTO = objectMapper.readValue(flatRequestDTOString, AddFlatRequestDTO.class);
        log.info("Received request to add flat for property: {}", flatRequestDTO.getFlatDescription());
        flatService.addFlat(flatRequestDTO, imageUrls, videoUrl);
        log.debug("Flat added successfully for property: {}", flatRequestDTO.getFlatPrice());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                    .body(new SuccessResponseDTO("Flat added successfully"));

    }

    @PutMapping(
            value = "/update-flat/{flatId}",
            produces = {MediaType.APPLICATION_JSON_VALUE}
    )
    @PreAuthorize("@securityService.isFlatOwner(authentication, #flatId)")
    @Operation(
            summary = "Update flat details",
            description = "Update flat information including images and video. Requires ownership of the property.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Flat updated successfully",
                    content = @Content(schema = @Schema(implementation = SuccessResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input or file type"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions"),
            @ApiResponse(responseCode = "404", description = "Flat not found"),
            @ApiResponse(responseCode = "413", description = "Payload too large"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<SuccessResponseDTO> updateFlat(
            @Valid
            @RequestPart(value = "flat", required = false)
            String updateFlatRequestDTOString,
            @RequestPart(value = "imageUrls", required = false)
            @MaxListSize(max = 5, message = "Max 5 images allowed per flat.")
            List<MultipartFile> imageUrls,
            @RequestPart(value = "videoUrl", required = false)
            MultipartFile videoUrl,
            @PathVariable(value = "flatId") UUID flatId
    )
            throws JsonProcessingException
    {
        log.info("Received request to update flat: {}", updateFlatRequestDTOString);
        UpdateFlatRequestDTO flatRequestDTO = objectMapper.readValue(updateFlatRequestDTOString, UpdateFlatRequestDTO.class);
        flatService.updateFlat(flatRequestDTO, flatId.toString(), imageUrls, videoUrl);
        return ResponseEntity
                .ok(new SuccessResponseDTO("Flat updated successfully"));
    }

    @DeleteMapping(
            value = "/delete-flat/{flatId}",
            produces = {MediaType.APPLICATION_JSON_VALUE}
    )
    @PreAuthorize("@securityService.isFlatOwner(authentication, #flatId)")
    @Operation(
            summary = "Delete a flat",
            description = "Delete a flat. Requires ownership of the property.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Flat deleted successfully",
                    content = @Content(schema = @Schema(implementation = SuccessResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions"),
            @ApiResponse(responseCode = "404", description = "Flat not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<SuccessResponseDTO> deleteFlat(
            @PathVariable("flatId") UUID flatId
    ) {
        log.info("Received request to delete flat: {}", flatId);
        flatService.deleteFlat(flatId);
        log.debug("Flat {} deleted successfully", flatId);
        return ResponseEntity
                .ok(new SuccessResponseDTO("Flat deleted successfully"));
    }
}
