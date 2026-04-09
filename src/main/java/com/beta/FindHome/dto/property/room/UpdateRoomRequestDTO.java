package com.beta.FindHome.dto.property.room;

import com.beta.FindHome.dto.common.amenities.UpdateAmenitiesDTO;
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
public class UpdateRoomRequestDTO {

    @Nullable
    @Size(max = 2000, message = "Description must not exceed 2000 characters.")
    private String roomDescription;

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
    private String roomRules;

    @Nullable
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than zero.")
    private BigDecimal roomPrice;

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
