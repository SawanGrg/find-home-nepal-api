package com.beta.FindHome.dto.property;

import com.beta.FindHome.model.Amenities;
import com.beta.FindHome.model.Area;
import com.beta.FindHome.model.Assets;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)  // Ignore null fields in the response
public class GetSpecificPropertyResponseDTO {
    // Common attributes for all entities
    private String userName;

    private UUID id;
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

    // Amenities (common for all entities) - Single object, not a list
    private Amenities amenities;

    // Area (common for all entities) - Single object, not a list
    private Area area;

    // Assets (common for all entities)
    private List<Assets> assets;
}

