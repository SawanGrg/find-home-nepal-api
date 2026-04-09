package com.beta.FindHome.dto.common.amenities;

import com.beta.FindHome.enums.model.Furnish;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class UpdateAmenitiesDTO {

    private Boolean hasParking;
    private Boolean hasWifi;
    private Boolean hasSecurityStaff;
    private Boolean hasUnderGroundWaterTank;
    private Boolean hasTV;
    private Boolean hasCCTV;
    private Boolean hasAC;
    private Boolean hasFridge;
    private Boolean hasBalcony;
    private Boolean hasWater;
    private Boolean hasSolarWaterHeater;
    private Boolean hasFan;
    private Furnish furnishingStatus;

    public UpdateAmenitiesDTO() {
    }
}
