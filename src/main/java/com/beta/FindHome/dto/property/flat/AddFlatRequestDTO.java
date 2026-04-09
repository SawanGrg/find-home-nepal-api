package com.beta.FindHome.dto.property.flat;

import com.beta.FindHome.dto.common.amenities.AddAmenitiesDTO;
import com.beta.FindHome.enums.model.Furnish;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class AddFlatRequestDTO {

    @NotNull(message = "Flat price is required.")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than zero.")
    private BigDecimal flatPrice;

    @NotBlank(message = "Description is required.")
    @Size(max = 2000, message = "Description must not exceed 2000 characters.")
    private String flatDescription;

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

    @NotBlank(message = "Flat rules are required.")
    @Size(max = 2000, message = "Flat rules must not exceed 2000 characters.")
    private String flatRules;

    @Positive(message = "Number of bedrooms must be at least 1.")
    private Integer bedRooms;

    @Positive(message = "Number of bathrooms must be at least 1.")
    private Integer bathRooms;

    @PositiveOrZero(message = "Number of kitchens cannot be negative.")
    private Integer kitchen;

    @PositiveOrZero(message = "Number of living rooms cannot be negative.")
    private Integer livingRoom;

    @NotNull(message = "At least one image is required.")
    private List<MultipartFile> imageUrls;

    @Nullable
    private MultipartFile videoUrl;

    @NotNull(message = "Length is required.")
    @Positive(message = "Length must be greater than zero.")
    private Float length;

    @NotNull(message = "Breadth is required.")
    @Positive(message = "Breadth must be greater than zero.")
    private Float breadth;

    private AddAmenitiesDTO amenities;

    public AddFlatRequestDTO() {
    }
}
