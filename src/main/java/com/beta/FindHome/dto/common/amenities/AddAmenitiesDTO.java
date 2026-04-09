package com.beta.FindHome.dto.common.amenities;

import com.beta.FindHome.dto.property.house.AddHouseRequestDTO;
import com.beta.FindHome.enums.model.Furnish;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddAmenitiesDTO {

    private boolean hasParking;

    private boolean hasWifi;

    private boolean hasSecurityStaff;

    private boolean hasUnderGroundWaterTank;

    private boolean hasTV;

    private boolean hasCCTV;

    private boolean hasAC;

    private boolean hasFridge;

    private boolean hasBalcony;

    private boolean hasWater;

    private boolean hasSolarWaterHeater;

    private boolean hasFan;

    private Furnish furnishingStatus;

    public AddAmenitiesDTO() {
    }

//    public AddAmenitiesDTO(
//            AddHouseRequestDTO amenities
//    ) {
//        this.hasParking = amenities.getAmenities().isHasParking();
//        this.hasWifi = amenities.getAmenities().isHasWifi();
//        this.hasSecurityStaff = amenities.getAmenities().isHasSecurityStaff();
//        this.hasUnderGroundWaterTank = amenities.getAmenities().isHasUnderGroundWaterTank();
//        this.hasTV = amenities.getAmenities().isHasTV();
//        this.hasCCTV = amenities.getAmenities().isHasCCTV();
//        this.hasAC = amenities.getAmenities().isHasAC();
//        this.hasFridge = amenities.getAmenities().isHasFridge();
//        this.hasBalcony = amenities.getAmenities().isHasBalcony();
//        this.hasWater = amenities.getAmenities().isHasWater();
//        this.hasSolarWaterHeater = amenities.getAmenities().isHasSolarWaterHeater();
//        this.hasFan = amenities.getAmenities().isHasFan();
//        this.furnishingStatus = amenities.getAmenities().getFurnishingStatus();
//    }

}
