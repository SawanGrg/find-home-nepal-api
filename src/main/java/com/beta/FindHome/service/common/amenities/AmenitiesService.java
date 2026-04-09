package com.beta.FindHome.service.common.amenities;

import com.beta.FindHome.dto.property.flat.AddFlatRequestDTO;
import com.beta.FindHome.dto.property.house.AddHouseRequestDTO;
import com.beta.FindHome.dto.property.room.AddRoomRequestDTO;
import com.beta.FindHome.model.Amenities;
import com.beta.FindHome.model.Property;

public interface AmenitiesService {

    void addHouseAmenities(AddHouseRequestDTO dto, Property house);

    void addFlatAmenities(AddFlatRequestDTO dto, Property flat);

    void addRoomAmenities(AddRoomRequestDTO dto, Property room);

    void updateAmenities(Amenities amenities, Property property);
}