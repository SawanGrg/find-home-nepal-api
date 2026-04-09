package com.beta.FindHome.controller.room;

import com.beta.FindHome.dto.SuccessResponseDTO;
import com.beta.FindHome.dto.property.room.AddRoomRequestDTO;
import com.beta.FindHome.dto.property.room.UpdateRoomRequestDTO;
import com.beta.FindHome.service.room.RoomService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
@RequestMapping("/api/v1/room")
@Validated
@Tag(name = "Room Management", description = "APIs for managing property rooms")
@SecurityRequirement(name = "bearerAuth")
public class RoomController {
    private static final Logger log = LoggerFactory.getLogger(RoomController.class);
    private final RoomService roomService;
    private final ObjectMapper objectMapper;

    @Autowired
    public RoomController(
            ObjectMapper objectMapper,
            RoomService roomService
    ) {
        this.roomService = roomService;
        this.objectMapper = objectMapper;
    }

    @PostMapping(
            value = "/add-room",
            consumes = {MediaType.MULTIPART_FORM_DATA_VALUE},
            produces = {MediaType.APPLICATION_JSON_VALUE}
    )
    @PreAuthorize("hasAuthority('OWNER')")
    @Operation(
            summary = "Add a new room",
            description = "Add a new room with images and optional video. Requires PROPERTY_OWNER role.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Room created successfully",
                    content = @Content(schema = @Schema(implementation = SuccessResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input or file type"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions"),
            @ApiResponse(responseCode = "413", description = "Payload too large"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<SuccessResponseDTO> addRoom(
            @Valid
            @RequestPart(value = "room", required = true) String roomRequestDTOString,
            @RequestPart(value = "imageUrls", required = true) List<MultipartFile> imageUrls,
            @RequestPart(value = "videoUrl", required = false) MultipartFile videoUrl
    )
            throws JsonProcessingException
    {
        AddRoomRequestDTO roomRequestDTO = objectMapper.readValue(roomRequestDTOString, AddRoomRequestDTO.class);
        log.debug("Room request DTO: {}", roomRequestDTO);
        roomService.addRoom(roomRequestDTO, imageUrls, videoUrl);
        log.debug("Room added successfully for property: {}", roomRequestDTO);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                    .body(new SuccessResponseDTO("Room added successfully"));

    }

    @PutMapping(
            value = "/update-room/{roomId}",
            produces = {MediaType.APPLICATION_JSON_VALUE}
    )
    @PreAuthorize("@securityService.isRoomOwner(authentication, #roomId)")
    @Operation(
            summary = "Update room details",
            description = "Update room information including images and video. Requires ownership of the property.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Room updated successfully",
                    content = @Content(schema = @Schema(implementation = SuccessResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input or file type"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions"),
            @ApiResponse(responseCode = "404", description = "Room not found"),
            @ApiResponse(responseCode = "413", description = "Payload too large"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<SuccessResponseDTO> updateRoom(
            @Valid
            @RequestPart(value = "room", required = false) String updateroomRequestDTOString,
            @RequestPart(value = "imageUrls", required = false) List<MultipartFile> imageUrls,
            @RequestPart(value = "videoUrl", required = false) MultipartFile videoUrl,
            @PathVariable(value = "roomId") String roomId
    )
            throws JsonProcessingException
    {
        log.info("Received request to update room: {}", roomId);
        UpdateRoomRequestDTO roomRequestDTO = objectMapper.readValue(updateroomRequestDTOString, UpdateRoomRequestDTO.class);
        roomService.updateRoom(roomRequestDTO, roomId, imageUrls, videoUrl);
        log.debug("Room {} updated successfully", roomId);
        return new ResponseEntity<>(
                    new SuccessResponseDTO("Room updated successfully"), HttpStatus.OK
        );
    }

    @DeleteMapping(
            value = "/delete-room/{roomId}",
            produces = {MediaType.APPLICATION_JSON_VALUE}
    )
    @PreAuthorize("@securityService.isRoomOwner(authentication, #roomId)")
    @Operation(
            summary = "Delete a room",
            description = "Delete a room. Requires ownership of the property.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Room deleted successfully",
                    content = @Content(schema = @Schema(implementation = SuccessResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions"),
            @ApiResponse(responseCode = "404", description = "Room not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<SuccessResponseDTO> deleteRoom(
            @Parameter(description = "ID of the room to delete", required = true)
            @PathVariable("roomId") UUID roomId
    ) {
        log.info("Received request to delete room: {}", roomId);
        roomService.deleteRoom(roomId);
        log.debug("Room {} deleted successfully", roomId);
        return ResponseEntity
                .ok(new SuccessResponseDTO("Room deleted successfully"));
    }
}
