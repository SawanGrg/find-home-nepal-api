package com.beta.FindHome.dto.property;

import com.beta.FindHome.dto.common.amenities.AmenitiesDTO;
import com.beta.FindHome.dto.common.area.AreaDTO;
import com.beta.FindHome.dto.common.assets.AssetDTO;
import com.beta.FindHome.enums.model.Furnish;
import com.beta.FindHome.model.Amenities;
import com.beta.FindHome.model.Area;
import com.beta.FindHome.model.Assets;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Setter
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL) // Ignore null fields in the response
public class GetAllPropertyResponseDTO {
    // Common attributes for all entities
    private UUID id;
    private String propertyType;
    private BigDecimal price;
    private String description;
    private String district;
    private String city;
    private String ward;
    private String tole;
    private String rules;
    private Boolean isAvailable;
    private Boolean isVerified;
    private Boolean isDeleted;

    // Attributes specific to House
    private Integer houseFloors;
    private Integer bedRooms;
    private Integer bathRooms;
    private Integer kitchen;
    private Integer livingRoom;

    // Attributes specific to Flat
    private Integer flatBedRooms;
    private Integer flatBathRooms;
    private Integer flatKitchen;
    private Integer flatLivingRoom;

    // Amenities (common for all entities)
    private AmenitiesDTO amenities;

    // Area (common for all entities)
    private AreaDTO area;

    // Assets (common for all entities)
    private List<AssetDTO> assets;

    private LocalDateTime createdAt;
}
