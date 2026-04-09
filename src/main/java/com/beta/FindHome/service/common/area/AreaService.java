// AreaService.java
package com.beta.FindHome.service.common.area;

import com.beta.FindHome.dto.property.flat.AddFlatRequestDTO;
import com.beta.FindHome.dto.property.house.AddHouseRequestDTO;
import com.beta.FindHome.dto.property.room.AddRoomRequestDTO;
import com.beta.FindHome.model.Area;
import com.beta.FindHome.model.Property;

public interface AreaService {

    void addHouseArea(AddHouseRequestDTO dto, Property house);

    void addFlatArea(AddFlatRequestDTO dto, Property flat);

    void addRoomArea(AddRoomRequestDTO dto, Property room);

    void updateArea(Area area, Property property);
}