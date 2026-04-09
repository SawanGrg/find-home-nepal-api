package com.beta.FindHome.dto.property.house;

import com.beta.FindHome.annotations.MaxListSize;
import com.beta.FindHome.dto.common.amenities.AddAmenitiesDTO;
import com.beta.FindHome.dto.common.amenities.UpdateAmenitiesDTO;
import com.beta.FindHome.enums.model.Furnish;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Setter
@Getter
public class UpdateHouseRequestDTO {

    @Nullable
    @Size(max = 2000, message = "Description must not exceed 2000 characters.")
    private String houseDescription;

    @Nullable
    @Size(max = 255, message = "District must not exceed 255 characters.")
    private String district;

    @Nullable
    @Size(max = 100, message = "City must not exceed 100 characters.")
    private String city;

    @Nullable
    @Size(max = 100, message = "Ward must not exceed 100 characters.")
    private String ward;

    @Nullable
    @Size(max = 100, message = "Tole must not exceed 100 characters.")
    private String tole;

    @Nullable
    @Size(max = 2000, message = "House rules must not exceed 2000 characters.")
    private String houseRules;

    @Nullable
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than zero.")
    private BigDecimal housePrice;

    @Nullable
    @PositiveOrZero(message = "Number of floors cannot be negative.")
    private Integer houseFloors;

    @Nullable
    @Positive(message = "Number of bedrooms must be at least 1.")
    private Integer bedRooms;

    @Nullable
    @Positive(message = "Number of bathrooms must be at least 1.")
    private Integer bathRooms;

    @Nullable
    @PositiveOrZero(message = "Number of kitchens cannot be negative.")
    private Integer kitchen;

    @Nullable
    @PositiveOrZero(message = "Number of living rooms cannot be negative.")
    private Integer livingRoom;

    private Boolean isAvailable;

    private Boolean isVerified;

    @Nullable
    private List<MultipartFile> imageUrls;

    @Nullable
    private MultipartFile videoUrl;

    @Nullable
    private List<UUID> imageUrlsToDelete;

    @Nullable
    private UUID videoUrlToDelete;

    @Nullable
    @Positive(message = "Length must be greater than zero.")
    private Float length;

    @Nullable
    @Positive(message = "Breadth must be greater than zero.")
    private Float breadth;

    @Nullable
    private UpdateAmenitiesDTO amenities;
}
