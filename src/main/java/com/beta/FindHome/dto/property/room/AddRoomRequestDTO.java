package com.beta.FindHome.dto.property.room;

import com.beta.FindHome.dto.common.amenities.AddAmenitiesDTO;
import com.beta.FindHome.enums.model.Furnish;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class AddRoomRequestDTO {

    @NotNull(message = "room price is required.")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than zero.")
    private BigDecimal roomPrice;

    @NotBlank(message = "Description is required.")
    @Size(max = 2000, message = "Description must not exceed 2000 characters.")
    private String roomDescription;

    @NotBlank(message = "District is required.")
    @Size(max = 255, message = "District must not exceed 255 characters.")
    private String district;

    @NotBlank(message = "City is required.")
    @Size(max = 100, message = "City must not exceed 100 characters.")
    private String city;

    @NotBlank(message = "Ward is required.")
    @Size(max = 100, message = "Ward must not exceed 100 characters.")
    private String ward;

    @NotBlank(message = "Tole is required.")
    @Size(max = 100, message = "Tole must not exceed 100 characters.")
    private String tole;

    @NotBlank(message = "room rules are required.")
    @Size(max = 2000, message = "room rules must not exceed 2000 characters.")
    private String roomRules;

    @NotNull(message = "Length is required.")
    @Positive(message = "Length must be greater than zero.")
    private Float length;

    @NotNull(message = "Breadth is required.")
    @Positive(message = "Breadth must be greater than zero.")
    private Float breadth;

    @NotNull(message = "At least one image is required.")
    private List<MultipartFile> imageUrls;

    @Nullable
    private MultipartFile videoUrl;

    private AddAmenitiesDTO amenities;

    public AddRoomRequestDTO() {
    }
}
