package com.beta.FindHome.dto.property.flat;

import com.beta.FindHome.dto.common.amenities.UpdateAmenitiesDTO;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.NumberFormat;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Setter
@Getter
public class UpdateFlatRequestDTO {
    @Nullable
    @Size(max = 2000, message = "Description must not exceed 2000 characters.")
    private String flatDescription;

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
    private String flatRules;

    @Nullable
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than zero.")
    private BigDecimal flatPrice;

    @Nullable
    @NumberFormat(style = NumberFormat.Style.NUMBER) // This is used to format the number as a number
    private Integer bedRooms;

    @Nullable
    @NumberFormat(style = NumberFormat.Style.NUMBER) // This is used to format the number as a number
    private Integer bathRooms;

    @Nullable
    @NumberFormat(style = NumberFormat.Style.NUMBER) // This is used to format the number as a number
    private Integer kitchen;

    @Nullable
    @NumberFormat(style = NumberFormat.Style.NUMBER) // This is used to format the number as a number
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
