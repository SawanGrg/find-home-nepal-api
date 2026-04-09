// RoomService.java
package com.beta.FindHome.service.room;

import com.beta.FindHome.dto.property.room.AddRoomRequestDTO;
import com.beta.FindHome.dto.property.room.UpdateRoomRequestDTO;
import com.beta.FindHome.model.Room;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface RoomService {

    void addRoom(AddRoomRequestDTO dto, List<MultipartFile> images, MultipartFile video);

    void updateRoom(UpdateRoomRequestDTO dto, String roomId, List<MultipartFile> images, MultipartFile video);

    void deleteRoom(UUID roomId);

    Room getRoomById(UUID roomId);
}

//package com.beta.FindHome.service.room;
//
//import com.beta.FindHome.dto.property.GetSpecificPropertyResponseDTO;
//import com.beta.FindHome.dto.property.room.AddRoomRequestDTO;
//import com.beta.FindHome.dto.property.house.AddHouseRequestDTO;
//import com.beta.FindHome.dto.property.house.UpdateHouseRequestDTO;
//import com.beta.FindHome.dto.property.room.UpdateRoomRequestDTO;
//import com.beta.FindHome.model.Room;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.util.List;
//import java.util.UUID;
//
//public interface RoomService {
//
//    /**
//     * Adds a new house to the system along with assets like images and videos.
//     * @param roomRequestDTO Contains house details to be added.
//     * @param images List of image files for the house.
//     * @param video Video file for the house.
//     */
//    void addRoom(
//            AddRoomRequestDTO roomRequestDTO,
//            List<MultipartFile> images,
//            MultipartFile video);
//
//    // Add a method to update room details
//    public void updateRoom(
//            UpdateRoomRequestDTO newRoomDetailsDTO,
//            String roomId,
//            List<MultipartFile> images,
//            MultipartFile video
//    );
//
//    void deleteRoom(UUID houseId);
//
//    public Room getRoomById(UUID roomId);
//}
