package com.beta.FindHome.dto.property;

import com.beta.FindHome.enums.filter.FilterType;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.RequiredArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.UUID;

@Setter
@Getter
@ToString
@EqualsAndHashCode
public class PropertyRequestDTO {
    @NotNull(message = "Filter type must not be null")
    @JsonProperty("filterType") // Explicitly specify JSON property name
    private FilterType filterType;

    private UUID landlordId; // Landlord ID for filtering properties by landlord
    // Common filters for all entities
    @Nullable
    private BigDecimal minPrice; // Minimum price
    @Nullable
    private BigDecimal maxPrice; // Maximum price
    @Nullable
    private String district; // District filter
    @Nullable
    private String city; // City filter
    @Nullable
    private String ward; // Ward filter
    @Nullable
    private String tole; // Tole filter
    @Nullable
    private Boolean isAvailable; // Availability filter
    @Nullable
    private Boolean isDeleted; // Deletion status filter
    @Nullable
    private Boolean isVerified; // Verification status filter

    @Nullable
    private String sortBy;

    // Amenities filters (common for all entities)
    @Nullable
    private Boolean hasParking; // Has parking
    @Nullable
    private Boolean hasWifi; // Has Wi-Fi
    @Nullable
    private Boolean hasSecurityStaff; // Has security staff
    @Nullable
    private Boolean hasUnderGroundWaterTank; // Has underground water tank
    @Nullable
    private Boolean hasTV; // Has TV
    @Nullable
    private Boolean hasCCTV; // Has CCTV
    @Nullable
    private Boolean hasAC; // Has AC
    @Nullable
    private Boolean hasFridge; // Has fridge
    @Nullable
    private Boolean hasBalcony; // Has balcony
    @Nullable
    private Boolean hasWater; // Has water
    @Nullable
    private Boolean hasSolarWaterHeater; // Has solar water heater
    @Nullable
    private Boolean hasFan; // Has fan

}
