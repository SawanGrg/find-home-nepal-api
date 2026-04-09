package com.beta.FindHome.dto.common.assets;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import com.beta.FindHome.model.House;

@Getter
@Setter
public class AddHouseAssetRequestDTO {

    @NotNull(message = "House is required.")
    private House house;  

    @NotBlank(message = "Asset type is required.")
    @Size(max = 50, message = "Asset type must not exceed 50 characters.")
    private String assetType;

    @NotBlank(message = "URL is required.")
    @Size(max = 255, message = "URL must not exceed 255 characters.")
    private String url;
}
