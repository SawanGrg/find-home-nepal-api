// RoomServiceImpl.java
package com.beta.FindHome.service.room;

import com.beta.FindHome.dto.common.assets.AddRoomAssetRequestDTO;
import com.beta.FindHome.dto.property.room.AddRoomRequestDTO;
import com.beta.FindHome.dto.property.room.UpdateRoomRequestDTO;
import com.beta.FindHome.exception.*;
import com.beta.FindHome.model.*;
import com.beta.FindHome.repository.AmenitiesRepository;
import com.beta.FindHome.repository.AreaRepository;
import com.beta.FindHome.repository.RoomRepository;
import com.beta.FindHome.repository.UserRepository;
import com.beta.FindHome.service.common.amenities.AmenitiesService;
import com.beta.FindHome.service.common.area.AreaService;
import com.beta.FindHome.service.common.assets.AssetService;
import com.beta.FindHome.service.property.PropertyService;
import com.beta.FindHome.service.video.VideoService;
import com.beta.FindHome.utils.MapperUtil;
import com.beta.FindHome.utils.RedisUtils;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;
    private final AmenitiesRepository amenitiesRepository;
    private final AreaRepository areaRepository;
    private final UserRepository userRepository;
    private final AssetService assetService;
    private final VideoService videoService;
    private final AmenitiesService amenitiesService;
    private final AreaService areaService;
    private final MapperUtil mapperUtil;
    private final PropertyService propertyService;
    private final RedisUtils redisUtils;

    @Autowired
    public RoomServiceImpl(
            RoomRepository roomRepository,
            AmenitiesRepository amenitiesRepository,
            AreaRepository areaRepository,
            UserRepository userRepository,
            AssetService assetService,
            VideoService videoService,
            AmenitiesService amenitiesService,
            AreaService areaService,
            MapperUtil mapperUtil,
            PropertyService propertyService,
            RedisUtils redisUtils
    ) {
        this.roomRepository = roomRepository;
        this.amenitiesRepository = amenitiesRepository;
        this.areaRepository = areaRepository;
        this.userRepository = userRepository;
        this.assetService = assetService;
        this.videoService = videoService;
        this.amenitiesService = amenitiesService;
        this.areaService = areaService;
        this.mapperUtil = mapperUtil;
        this.propertyService = propertyService;
        this.redisUtils = redisUtils;
    }

    // =====================================================================
    // ADD
    // =====================================================================

    @Override
    @Transactional
    public void addRoom(
            AddRoomRequestDTO dto,
            List<MultipartFile> images,
            MultipartFile video
    ) {
        dto.setImageUrls(images);
        dto.setVideoUrl(video.isEmpty() ? null : video);

        Users landlord = getAuthenticatedLandlord();

        Room room = mapperUtil.createRoomMapper(dto, landlord);
        Room savedRoom = roomRepository.save(room);

        areaService.addRoomArea(dto, savedRoom);
        amenitiesService.addRoomAmenities(dto, savedRoom);

        List<AddRoomAssetRequestDTO> assets = new ArrayList<>();

        if (dto.getImageUrls() != null) {
            assets.addAll(assetService.createRoomAssetList(dto.getImageUrls(), savedRoom));
        }

        if (dto.getVideoUrl() != null) {
            String videoURL = saveVideoSafely(dto.getVideoUrl());
            AddRoomAssetRequestDTO videoAsset = new AddRoomAssetRequestDTO();
            videoAsset.setUrl(videoURL);
            videoAsset.setAssetType("VIDEO");
            videoAsset.setRoom(savedRoom);
            assets.add(videoAsset);
        }

        assets.forEach(assetService::addRoomAssets);
        // PropertyLookup removed — JOINED inheritance handles type resolution
    }

    // =====================================================================
    // UPDATE
    // =====================================================================

    @Override
    @Transactional
    public void updateRoom(
            UpdateRoomRequestDTO dto,
            String roomId,
            List<MultipartFile> images,
            MultipartFile video
    ) {
        Users landlord = getAuthenticatedLandlord();

        Room existingRoom = roomRepository.findById(UUID.fromString(roomId))
                .orElseThrow(() -> new RoomException("Room not found."));

        Room updatedRoom = mapperUtil.updateRoomMapper(dto, existingRoom, landlord);
        roomRepository.save(updatedRoom);

        if (dto.getAmenities() != null) {
            Amenities existing = amenitiesRepository.findByPropertyId(existingRoom.getId())
                    .orElseThrow(() -> new AmenitiesException(
                            "Amenities not found for room ID: " + roomId));
            Amenities updated = mapperUtil.createRoomAmenitiesMapper(
                    dto.getAmenities(), existing, existingRoom);
            amenitiesService.updateAmenities(updated, updatedRoom);
        }

        if (dto.getLength() != null || dto.getBreadth() != null) {
            areaRepository.findByPropertyId(existingRoom.getId()).ifPresent(existingArea -> {
                Area updatedArea = mapperUtil.createRoomAreaMapper(
                        dto, existingArea, updatedRoom);
                areaService.updateArea(updatedArea, updatedRoom);
            });
        }

        if (dto.getImageUrlsToDelete() != null) {
            dto.getImageUrlsToDelete().forEach(assetService::deleteAsset);
        }
        if (dto.getVideoUrlToDelete() != null) {
            assetService.deleteAsset(dto.getVideoUrlToDelete());
        }

        List<AddRoomAssetRequestDTO> newAssets = new ArrayList<>();
        if (images != null) {
            newAssets.addAll(assetService.createRoomAssetList(images, updatedRoom));
        }
        if (video != null && !video.isEmpty()) {
            String videoURL = saveVideoSafely(video);
            AddRoomAssetRequestDTO videoAsset = new AddRoomAssetRequestDTO();
            videoAsset.setUrl(videoURL);
            videoAsset.setAssetType("VIDEO");
            videoAsset.setRoom(updatedRoom);
            newAssets.add(videoAsset);
        }
        newAssets.forEach(assetService::addRoomAssets);

        String cacheKey = "property:" + roomId;
        redisUtils.delete(cacheKey);
        log.info("Room updated and cache invalidated for ID: {}", roomId);
    }

    // =====================================================================
    // DELETE
    // =====================================================================

    @Override
    @Transactional
    public void deleteRoom(UUID roomId) {
        if (!roomRepository.existsById(roomId)) {
            throw new RoomException("Room not found.");
        }
        roomRepository.deleteById(roomId);
        redisUtils.delete("property:" + roomId);
        log.info("Room deleted for ID: {}", roomId);
    }

    // =====================================================================
    // GET
    // =====================================================================

    @Override
    @Transactional()
    public Room getRoomById(UUID roomId) {
        return roomRepository.findActiveWithDetailsById(roomId)
                .orElseThrow(() -> new RoomException("Room not found."));
    }

    // =====================================================================
    // PRIVATE HELPERS
    // =====================================================================

    private Users getAuthenticatedLandlord() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        Users user = userRepository.findByUserName(username);
        if (user == null) {
            throw new UserException("Authenticated user not found.");
        }
        return user;
    }

    private String saveVideoSafely(MultipartFile video) {
        try {
            return videoService.saveVideo(video);
        } catch (Exception e) {
            throw new RoomException("Error saving video: " + e.getMessage());
        }
    }
}

//package com.beta.FindHome.service.room;
//
//import com.beta.FindHome.dao.*;
//import com.beta.FindHome.dto.common.assets.AddRoomAssetRequestDTO;
//import com.beta.FindHome.dto.property.room.AddRoomRequestDTO;
//import com.beta.FindHome.dto.property.room.UpdateRoomRequestDTO;
//import com.beta.FindHome.exception.*;
//import com.beta.FindHome.model.*;
//import com.beta.FindHome.repository.UserRepository;
//import com.beta.FindHome.service.common.amenities.AmenitiesService;
//import com.beta.FindHome.service.common.area.AreaService;
//import com.beta.FindHome.service.common.assets.AssetService;
//import com.beta.FindHome.service.property.PropertyService;
//import com.beta.FindHome.service.video.VideoService;
//import com.beta.FindHome.utils.MapperUtil;
//import com.beta.FindHome.utils.RedisUtils;
//import jakarta.transaction.Transactional;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.retry.annotation.Backoff;
//import org.springframework.retry.annotation.Retryable;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.stereotype.Service;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Optional;
//import java.util.UUID;
//
//@Service
//public class RoomServiceImpl implements RoomService {
//
//    private final HouseDAO houseDAOImpl;
//    private final UserDAO userDAOImpl;
//    private final AssetService assetServiceImpl;
//    private final VideoService videoService;
//    private final AmenitiesService amenitiesServiceImpl;
//    private final AreaService areaServiceImpl;
//    private final MapperUtil mapperUtil;
//    private final AmenitiesDAO amenitiesDAOImpl;
//    private final AreaDAO areaDAOImpl;
//    private final RoomDAO roomDAOImpl;
//    private final PropertyService propertyServiceImpl;
//    private final RedisUtils redisUtils;
//    private final UserRepository userRepository;
//
//    @Autowired
//    public RoomServiceImpl(HouseDAO houseDAOImpl,
//                           UserDAO userDAOImpl,
//                           AssetService assetServiceImpl,
//                           VideoService videoService,
//                           AmenitiesService amenitiesServiceImpl,
//                           AreaService areaServiceImpl,
//                           MapperUtil mapperUtil,
//                           AmenitiesDAO amenitiesDAOImpl,
//                           AreaDAO areaDAOImpl,
//                           RoomDAO roomDAOImpl,
//                           PropertyService propertyServiceImpl,
//                           RedisUtils redisUtils,
//                           UserRepository userRepository
//    ) {
//        this.houseDAOImpl = houseDAOImpl;
//        this.userDAOImpl = userDAOImpl;
//        this.assetServiceImpl = assetServiceImpl;
//        this.videoService = videoService;
//        this.amenitiesServiceImpl = amenitiesServiceImpl;
//        this.areaServiceImpl = areaServiceImpl;
//        this.mapperUtil = mapperUtil;
//        this.amenitiesDAOImpl = amenitiesDAOImpl;
//        this.areaDAOImpl = areaDAOImpl;
//        this.roomDAOImpl = roomDAOImpl;
//        this.propertyServiceImpl = propertyServiceImpl;
//        this.redisUtils = redisUtils;
//        this.userRepository = userRepository;
//    }
//
//    @Transactional
//    @Retryable(
//            maxAttempts = 3,                   // Number of retry attempts
//            backoff = @Backoff(delay = 2000)   // Backoff delay in milliseconds
//    )
//    public void addRoom(
//            AddRoomRequestDTO roomRequestDTO,
//            List<MultipartFile> images,
//            MultipartFile video
//    ) {
//
//        // Optionally, process the images and videos
//        roomRequestDTO.setImageUrls(images);
//        roomRequestDTO.setVideoUrl(video.isEmpty() ? null : video);
//
//        List<AddRoomAssetRequestDTO> assetRequestDTOList = new ArrayList<AddRoomAssetRequestDTO>();
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        String username = authentication.getName();
//        Users user = userRepository.findByUserName(username);
//        if (user == null) {
//            throw new UserException("User not found.");
//        }
//        Users landlord = userDAOImpl.findById(user.getId())
//                .orElseThrow(() -> new UserException("User not found."));
//
//        if(landlord == null) {
//            throw new UserException("User has not been registered yet.");
//        }
//
//        Room room = mapperUtil.createRoomMapper(roomRequestDTO, landlord);
//        Room addedRoom = roomDAOImpl.save(room);
//        areaServiceImpl.addRoomArea(roomRequestDTO, addedRoom);
//
//        if (roomRequestDTO.getImageUrls() != null) {
//            assetRequestDTOList.addAll(
//                    assetServiceImpl.createRoomAssetList(
//                            roomRequestDTO.getImageUrls(), addedRoom
//                    ));
//        }
//
//        if(roomRequestDTO.getVideoUrl() != null){
//            String videoURL;
//            try {
//                videoURL = videoService.saveVideo(roomRequestDTO.getVideoUrl());
//            } catch (Exception e) {
//                throw new RoomException("Error saving video: " + e.getMessage());
//            }
//            AddRoomAssetRequestDTO videoAsset = new AddRoomAssetRequestDTO();
//            videoAsset.setUrl(videoURL);
//            videoAsset.setAssetType("VIDEO");
//            videoAsset.setRoom(addedRoom);
//            assetRequestDTOList.add(videoAsset);
//        }
//
//        for (AddRoomAssetRequestDTO assetRequestDTO : assetRequestDTOList) {
//            assetServiceImpl.addRoomAssets(assetRequestDTO);
//        }
//
//        amenitiesServiceImpl.addRoomAmenities(roomRequestDTO, addedRoom);
//        this.propertyServiceImpl.addRoomPropertyLookup(addedRoom);
//    }
//
//    // Add a method to update house details
//    @Transactional
//    @Retryable(
//            maxAttempts = 3,
//            backoff = @Backoff(delay = 1000)
//    )
//    public void updateRoom(
//            UpdateRoomRequestDTO newRoomDetailsDTO,
//            String roomId,
//            List<MultipartFile> images,
//            MultipartFile video
//    ) {
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        String username = authentication.getName();
//        Users user = userRepository.findByUserName(username);
//        if (user == null) {
//            throw new UserException("User not found.");
//        }
//        Users landlord = userDAOImpl.findById(user.getId()).orElseThrow(
//                () -> new UserException("User not found.")
//        );
//        if (landlord == null) {
//            throw new UserException("User has not been registered yet.");
//        }
//        Room existingRoom = roomDAOImpl.findById(UUID.fromString(roomId))
//                .orElseThrow(() -> new RoomException("Room not found."));
//        if (existingRoom == null) {
//            throw new ResourceNotFoundException("Room not found.");
//        }
//        Room updatedRoom = mapperUtil.updateRoomMapper(newRoomDetailsDTO, existingRoom, landlord);
//        roomDAOImpl.update(updatedRoom);
//
//        if (newRoomDetailsDTO.getAmenities() != null) {
//            Amenities previousAmenities = amenitiesDAOImpl.getAmenitiesByRoomId(UUID.fromString(roomId))
//                    .orElseThrow(() -> new AmenitiesException("Amenities not found."));
//            Amenities updatedAmenities = mapperUtil.createRoomAmenitiesMapper(newRoomDetailsDTO.getAmenities(),previousAmenities, existingRoom);
//            amenitiesServiceImpl.updateAmenities(updatedAmenities, null, updatedRoom, null);
//        }
//
//        if (newRoomDetailsDTO.getLength() != null || newRoomDetailsDTO.getBreadth() != null) {
//            Optional<Area> optionalArea = areaDAOImpl.getAreaByRoomId(UUID.fromString(roomId));
//
//            if (optionalArea.isPresent()) {
//                Area previousArea = optionalArea.get();
//                Area updatedArea = mapperUtil.createRoomAreaMapper(newRoomDetailsDTO, previousArea, updatedRoom);
//                areaServiceImpl.updateArea(updatedArea, null, updatedRoom, null);
//            }
//        }
//
//        if (newRoomDetailsDTO.getImageUrlsToDelete() != null) {
//            for (UUID imageId : newRoomDetailsDTO.getImageUrlsToDelete()) {
//                assetServiceImpl.deleteAsset(imageId);
//            }
//        }
//        if(newRoomDetailsDTO.getVideoUrlToDelete() != null){
//            assetServiceImpl.deleteAsset(newRoomDetailsDTO.getVideoUrlToDelete());
//        }
//        List<AddRoomAssetRequestDTO> assetRequestDTOList = new ArrayList<AddRoomAssetRequestDTO>();
//        if (images != null) {
//            assetRequestDTOList.addAll(
//                    assetServiceImpl.createRoomAssetList(
//                            images, updatedRoom
//                    ));
//        }
//        if (video != null) {
//            String videoURL;
//            try {
//                videoURL = videoService.saveVideo(video);
//            } catch (Exception e) {
//                throw new RoomException("Error saving video: " + e.getMessage());
//            }
//            AddRoomAssetRequestDTO videoAsset = new AddRoomAssetRequestDTO();
//            videoAsset.setUrl(videoURL);
//            videoAsset.setAssetType("VIDEO");
//            videoAsset.setRoom(updatedRoom);
//            assetRequestDTOList.add(videoAsset);
//        }
//
//        for (AddRoomAssetRequestDTO assetRequestDTO : assetRequestDTOList) {
//            assetServiceImpl.addRoomAssets(assetRequestDTO);
//        }
//        String key = "property:"+ roomId;
//        if(redisUtils.keyExists(key)){
//            redisUtils.delete(key);
//        }
//        redisUtils.save(key, this.propertyServiceImpl.getProperty(existingRoom.getId()));
//    }
//
//    @Transactional
//    public void deleteRoom(UUID roomId) {
//        Room room = roomDAOImpl.findById(roomId)
//                .orElseThrow(() -> new RoomException("Room not found."));
//        if (room == null) {
//            throw new RoomException("Room not found.");
//        }
//        roomDAOImpl.delete(roomId);
//    }
//
//    @Transactional
//    public Room getRoomById(UUID roomId) {
//        Room room = roomDAOImpl.findById(roomId)
//                .orElseThrow(() -> new RoomException("Room not found."));
//        if (room == null) {
//            throw new RoomException("Room not found.");
//        }
//        return roomDAOImpl.getRoomDetailsById(roomId);
//    }
//}
