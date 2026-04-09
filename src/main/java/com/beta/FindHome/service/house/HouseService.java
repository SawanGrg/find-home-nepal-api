// HouseService.java
package com.beta.FindHome.service.house;

import com.beta.FindHome.dto.property.house.AddHouseRequestDTO;
import com.beta.FindHome.dto.property.house.UpdateHouseRequestDTO;
import com.beta.FindHome.model.House;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface HouseService {

    void addHome(AddHouseRequestDTO dto, List<MultipartFile> images, MultipartFile video);

    void updateHome(UpdateHouseRequestDTO dto, String houseId, List<MultipartFile> images, MultipartFile video);

    void deleteHouse(UUID houseId);

    House getHouseById(UUID houseId);
}

//package com.beta.FindHome.service.house;
//
//import com.beta.FindHome.dto.property.house.AddHouseRequestDTO;
//import com.beta.FindHome.dto.property.house.UpdateHouseRequestDTO;
//import com.beta.FindHome.model.House;
//import org.springframework.web.multipart.MultipartFile;
//import java.util.List;
//import java.util.UUID;
//
//public interface HouseService {
//
//    /**
//     * Adds a new house to the system along with assets like images and videos.
//     * @param houseDTO Contains house details to be added.
//     * @param images List of image files for the house.
//     * @param video Video file for the house.
//     */
//    void addHome(
//            AddHouseRequestDTO houseDTO,
//            List<MultipartFile> images,
//            MultipartFile video);
//
//    // Add a method to update house details
//    void updateHome(
//            UpdateHouseRequestDTO newHouseDetailsDTO,
//            String houseId,
//            List<MultipartFile> images,
//            MultipartFile video
//    );
//
//    void deleteHouse(UUID houseId);
//    public House getHouseById(UUID houseId);
//}
