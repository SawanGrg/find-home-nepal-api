package com.beta.FindHome.controller.house;

import com.beta.FindHome.annotations.MaxListSize;
import com.beta.FindHome.dto.property.house.AddHouseRequestDTO;
import com.beta.FindHome.dto.SuccessResponseDTO;
import com.beta.FindHome.dto.property.house.UpdateHouseRequestDTO;
import com.beta.FindHome.exception.ResourceNotFoundException;
import com.beta.FindHome.service.house.HouseService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

@RestController
@RequestMapping("/api/v1/house")
@Validated
@Tag(name = "House Management", description = "APIs for managing property houses")
@SecurityRequirement(name = "bearerAuth")
@Slf4j
public class HouseController {

    private final HouseService houseService;
    private final ObjectMapper objectMapper;

    @Autowired
    public HouseController(
            HouseService houseService,
            ObjectMapper objectMapper
    ) {
        this.houseService = houseService;
        this.objectMapper = objectMapper;
    }

    @PostMapping(
            value = "/add-house",
            consumes = {MediaType.MULTIPART_FORM_DATA_VALUE},
            produces = {MediaType.APPLICATION_JSON_VALUE}
    )
    @PreAuthorize("hasAuthority('OWNER')")
    @Operation(
            summary = "Add a new house",
            description = "Add a new house with images and optional video. Requires PROPERTY_OWNER role.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "House created successfully",
                    content = @Content(schema = @Schema(implementation = SuccessResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input or file type"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions"),
            @ApiResponse(responseCode = "413", description = "Payload too large"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<SuccessResponseDTO> addHouse(
            @Valid
            @RequestPart(value = "house", required = true) String houseRequestDTOString,
            @RequestPart(value = "imageUrls", required = true)
            @MaxListSize(max = 5, message = "Image per house is 5.") List<MultipartFile> imageUrls,
            @RequestPart(value = "videoUrl", required = false) MultipartFile videoUrl
    )
            throws JsonProcessingException
    {
        AddHouseRequestDTO houseRequestDTO = objectMapper.readValue(houseRequestDTOString, AddHouseRequestDTO.class);
        houseService.addHome(houseRequestDTO, imageUrls, videoUrl);
        return new ResponseEntity<>(
                new SuccessResponseDTO("House added successfully"),
                HttpStatus.OK
        );
    }

    @PutMapping(
            value = "/update-house/{houseId}",
            produces = {MediaType.APPLICATION_JSON_VALUE}
    )
    @PreAuthorize("@securityService.isHouseOwner(authentication, #houseId)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "House updated successfully",
                    content = @Content(schema = @Schema(implementation = SuccessResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input or file type"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions"),
            @ApiResponse(responseCode = "404", description = "House not found"),
            @ApiResponse(responseCode = "413", description = "Payload too large"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<SuccessResponseDTO> updateHouse(
            @Valid
            @RequestPart(value = "house", required = false) String updateHouseRequestDTOString,
            @RequestPart(value = "imageUrls", required = false)
            @MaxListSize(max = 5, message = "Image per house is 5.") List<MultipartFile> imageUrls,
            @RequestPart(value = "videoUrl", required = false) MultipartFile videoUrl,
            @PathVariable(value = "houseId") String houseId
    )
            throws JsonProcessingException
    {
        UpdateHouseRequestDTO houseRequestDTO = objectMapper.readValue(updateHouseRequestDTOString, UpdateHouseRequestDTO.class);
        log.info("Received request to update house: {}", houseId);
        houseService.updateHome(houseRequestDTO, houseId, imageUrls, videoUrl);
        log.debug("House {} updated successfully", houseId);
        return ResponseEntity
                .ok(new SuccessResponseDTO("House updated successfully"));
    }

    @DeleteMapping(
            value = "/delete-house/{houseId}",
            produces = {MediaType.APPLICATION_JSON_VALUE}
    )
    @PreAuthorize("@securityService.isHouseOwner(authentication, #houseId)")
    @Operation(
            summary = "Delete a house",
            description = "Delete a house. Requires ownership of the property.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "House deleted successfully",
                    content = @Content(schema = @Schema(implementation = SuccessResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions"),
            @ApiResponse(responseCode = "404", description = "House not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<SuccessResponseDTO> deleteHouse(
            @PathVariable("houseId") UUID houseId
    ) {
        log.info("Received request to delete house: {}", houseId);
        houseService.deleteHouse(houseId);
        log.debug("House {} deleted successfully", houseId);
        return ResponseEntity
                .ok(new SuccessResponseDTO("House deleted successfully"));
    }

}
