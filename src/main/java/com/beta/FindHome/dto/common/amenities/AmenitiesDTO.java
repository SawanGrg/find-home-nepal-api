package com.beta.FindHome.dto.common.amenities;

import com.beta.FindHome.enums.model.Furnish;

import java.util.UUID;

public record AmenitiesDTO(
        UUID id,
        boolean hasParking,
        boolean hasWifi,
        boolean hasSecurityStaff,
        boolean hasUnderGroundWaterTank,
        boolean hasTV,
        boolean hasCCTV,
        boolean hasAC,
        boolean hasFridge,
        boolean hasBalcony,
        boolean hasWater,
        boolean hasSolarWaterHeater,
        boolean hasFan,
        Furnish furnishingStatus
) {}