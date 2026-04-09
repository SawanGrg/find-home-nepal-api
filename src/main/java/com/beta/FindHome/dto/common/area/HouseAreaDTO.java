package com.beta.FindHome.dto.common.area;
import com.beta.FindHome.model.House;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HouseAreaDTO {

    @NotNull(message = "House cannot be null")
    private House house;

    @NotNull(message = "Length cannot be null")
    private Float length;

    @NotNull(message = "Breadth cannot be null")
    private Float breadth;
}
