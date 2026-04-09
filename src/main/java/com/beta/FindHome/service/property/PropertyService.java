package com.beta.FindHome.service.property;

import com.beta.FindHome.dto.property.GetAllPropertyResponseDTO;
import com.beta.FindHome.dto.property.GetSpecificPropertyResponseDTO;
import com.beta.FindHome.dto.property.PropertyRequestDTO;
import com.beta.FindHome.model.House;
import com.beta.FindHome.model.Flat;
import com.beta.FindHome.model.Room;
import com.beta.FindHome.model.Users;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface PropertyService {
    Users findLandLordIdByPropertyId(UUID propertyId);
    Page<GetAllPropertyResponseDTO> getAllPropertyList(PropertyRequestDTO propertyRequestDTO, int page, int size);
    GetSpecificPropertyResponseDTO getProperty(UUID id);
}