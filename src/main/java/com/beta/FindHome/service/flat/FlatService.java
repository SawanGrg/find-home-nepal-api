package com.beta.FindHome.service.flat;

import com.beta.FindHome.dto.property.flat.AddFlatRequestDTO;
import com.beta.FindHome.dto.property.flat.UpdateFlatRequestDTO;
import com.beta.FindHome.dto.property.house.AddHouseRequestDTO;
import com.beta.FindHome.dto.property.house.UpdateHouseRequestDTO;
import com.beta.FindHome.model.Flat;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface FlatService {

    /**
     * Adds a new house to the system along with assets like images and videos.
     * @param flatRequestDTO Contains house details to be added.
     * @param images List of image files for the house.
     * @param video Video file for the house.
     */
    void addFlat(
            AddFlatRequestDTO flatRequestDTO,
            List<MultipartFile> images,
            MultipartFile video);

    // Add a method to update house details
    void updateFlat(
            UpdateFlatRequestDTO newHouseDetailsDTO,
            String houseId,
            List<MultipartFile> images,
            MultipartFile video
    );

    void deleteFlat(UUID houseId);
    public Flat getFlatDetailsById(UUID flatId);
}
