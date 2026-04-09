package com.beta.FindHome.dto.common.assets;

import com.beta.FindHome.model.House;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class UpdateHouseAssetDTO {

    @Nullable
    private House house;

    @Nullable
    @Size(max = 50, message = "Asset type must not exceed 50 characters.")
    private String assetType;

    @Nullable
    @Size(max = 255, message = "URL must not exceed 255 characters.")
    private String url;
}
