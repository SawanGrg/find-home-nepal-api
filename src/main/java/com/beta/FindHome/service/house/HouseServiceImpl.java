package com.beta.FindHome.service.house;

import com.beta.FindHome.dto.common.assets.AddHouseAssetRequestDTO;
import com.beta.FindHome.dto.property.house.AddHouseRequestDTO;
import com.beta.FindHome.dto.property.house.UpdateHouseRequestDTO;
import com.beta.FindHome.exception.HouseException;
import com.beta.FindHome.exception.UserException;
import com.beta.FindHome.model.*;
import com.beta.FindHome.repository.AmenitiesRepository;
import com.beta.FindHome.repository.AreaRepository;
import com.beta.FindHome.repository.HouseRepository;
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
public class HouseServiceImpl implements HouseService {

    private final HouseRepository houseRepository;
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
    public HouseServiceImpl(
            HouseRepository houseRepository,
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
        this.houseRepository = houseRepository;
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
    public void addHome(
            AddHouseRequestDTO dto,
            List<MultipartFile> images,
            MultipartFile video
    ) {
        dto.setImageUrls(images);
        dto.setVideoUrl(video.isEmpty() ? null : video);

        Users landlord = getAuthenticatedLandlord();

        House house = mapperUtil.createHouseMapper(dto, landlord);
        House savedHouse = houseRepository.save(house);

        areaService.addHouseArea(dto, savedHouse);
        amenitiesService.addHouseAmenities(dto, savedHouse);

        List<AddHouseAssetRequestDTO> assets = new ArrayList<>();

        if (dto.getImageUrls() != null) {
            assets.addAll(assetService.createHouseAssetList(dto.getImageUrls(), savedHouse));
        }

        if (dto.getVideoUrl() != null) {
            String videoURL = saveVideoSafely(dto.getVideoUrl());
            AddHouseAssetRequestDTO videoAsset = new AddHouseAssetRequestDTO();
            videoAsset.setUrl(videoURL);
            videoAsset.setAssetType("VIDEO");
            videoAsset.setHouse(savedHouse);
            assets.add(videoAsset);
        }

        assets.forEach(assetService::addHouseAssets);
        // PropertyLookup removed — JOINED inheritance handles type resolution
    }

    // =====================================================================
    // UPDATE
    // =====================================================================

    @Override
    @Transactional
    public void updateHome(
            UpdateHouseRequestDTO dto,
            String houseId,
            List<MultipartFile> images,
            MultipartFile video
    ) {
        Users landlord = getAuthenticatedLandlord();

        House existingHouse = houseRepository.findById(UUID.fromString(houseId))
                .orElseThrow(() -> new HouseException(
                        "House with ID " + houseId + " does not exist."));

        House updatedHouse = mapperUtil.updateHouseMapper(dto, existingHouse, landlord);
        houseRepository.save(updatedHouse);

        if (dto.getAmenities() != null) {
            Amenities existing = amenitiesRepository.findByPropertyId(existingHouse.getId())
                    .orElseThrow(() -> new HouseException(
                            "Amenities not found for house ID: " + houseId));
            Amenities updated = mapperUtil.createHouseAmenitiesMapper(
                    dto.getAmenities(), existing, updatedHouse);
            amenitiesService.updateAmenities(updated, updatedHouse);
        }

        if (dto.getLength() != null && dto.getBreadth() != null) {
            areaRepository.findByPropertyId(existingHouse.getId()).ifPresent(existingArea -> {
                Area updatedArea = mapperUtil.createHouseAreaMapper(
                        dto, existingArea, updatedHouse);
                areaService.updateArea(updatedArea, updatedHouse);
            });
        }

        if (dto.getImageUrlsToDelete() != null) {
            dto.getImageUrlsToDelete().forEach(assetService::deleteAsset);
        }
        if (dto.getVideoUrlToDelete() != null) {
            assetService.deleteAsset(dto.getVideoUrlToDelete());
        }

        List<AddHouseAssetRequestDTO> newAssets = new ArrayList<>();
        if (images != null) {
            newAssets.addAll(assetService.createHouseAssetList(images, updatedHouse));
        }
        if (video != null && !video.isEmpty()) {
            String videoURL = saveVideoSafely(video);
            AddHouseAssetRequestDTO videoAsset = new AddHouseAssetRequestDTO();
            videoAsset.setUrl(videoURL);
            videoAsset.setAssetType("VIDEO");
            videoAsset.setHouse(updatedHouse);
            newAssets.add(videoAsset);
        }
        newAssets.forEach(assetService::addHouseAssets);

        // Invalidate cache — force fresh fetch on next request
        String cacheKey = "property:" + houseId;
        redisUtils.delete(cacheKey);
        log.info("House updated and cache invalidated for ID: {}", houseId);
    }

    // =====================================================================
    // DELETE
    // =====================================================================

    @Override
    @Transactional
    public void deleteHouse(UUID houseId) {
        if (!houseRepository.existsById(houseId)) {
            throw new HouseException("House not found.");
        }
        houseRepository.deleteById(houseId);
        redisUtils.delete("property:" + houseId);
        log.info("House deleted for ID: {}", houseId);
    }

    // =====================================================================
    // GET
    // =====================================================================

    @Override
    @Transactional()
    public House getHouseById(UUID houseId) {
        return houseRepository.findActiveWithDetailsById(houseId)
                .orElseThrow(() -> new HouseException("House not found."));
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
            throw new HouseException("Error saving video: " + e.getMessage());
        }
    }
}

//package com.beta.FindHome.service.house;
//
//import java.util.*;
//
//import com.beta.FindHome.dao.AmenitiesDAO;
//import com.beta.FindHome.dao.AreaDAO;
//import com.beta.FindHome.dao.HouseDAO;
//import com.beta.FindHome.dao.UserDAO;
//import com.beta.FindHome.dto.common.assets.AddHouseAssetRequestDTO;
//import com.beta.FindHome.dto.property.house.AddHouseRequestDTO;
//import com.beta.FindHome.dto.property.house.UpdateHouseRequestDTO;
//import com.beta.FindHome.exception.HouseException;
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
//import com.beta.FindHome.exception.UserException;
//
//import org.springframework.web.multipart.MultipartFile;
//
//@Service
//public class HouseServiceImpl implements HouseService {
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
//    private final PropertyService propertyServiceImpl;
//    private final RedisUtils redisUtils;
//    private final UserRepository userRepository;
//
//    @Autowired
//    public HouseServiceImpl(HouseDAO houseDAOImpl,
//                            UserDAO userDAOImpl,
//                            AssetService assetServiceImpl,
//                            VideoService videoService,
//                            AmenitiesService amenitiesServiceImpl,
//                            AreaService areaServiceImpl,
//                            MapperUtil mapperUtil,
//                            AmenitiesDAO amenitiesDAOImpl,
//                            AreaDAO areaDAOImpl,
//                            PropertyService propertyServiceImpl,
//                            RedisUtils redisUtils,
//                            UserRepository userRepository)
//    {
//        this.houseDAOImpl = houseDAOImpl;
//        this.userDAOImpl = userDAOImpl;
//        this.assetServiceImpl = assetServiceImpl;
//        this.videoService = videoService;
//        this.amenitiesServiceImpl = amenitiesServiceImpl;
//        this.areaServiceImpl = areaServiceImpl;
//        this.mapperUtil = mapperUtil;
//        this.amenitiesDAOImpl = amenitiesDAOImpl;
//        this.areaDAOImpl = areaDAOImpl;
//        this.propertyServiceImpl = propertyServiceImpl;
//        this.redisUtils = redisUtils;
//        this.userRepository = userRepository;
//    }
//
//    @Override
//    @Transactional
//    @Retryable(
//            maxAttempts = 2,
//            backoff = @Backoff(delay = 2000)
//    )
//    public void addHome(
//            AddHouseRequestDTO houseDTO,
//            List<MultipartFile> images,
//            MultipartFile video
//    ) {
//        houseDTO.setImageUrls(images);
//        houseDTO.setVideoUrl(video.isEmpty() ? null : video);
//
//        List<AddHouseAssetRequestDTO> assetRequestDTOList = new ArrayList<AddHouseAssetRequestDTO>();
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        String username = authentication.getName();
//        Users user = userRepository.findByUserName(username);
//        if (user == null) {
//            throw new UserException("User not found.");
//        }
//        Users landlord = userDAOImpl.findById(user.getId())
//                .orElseThrow(() -> new UserException("User has not been registered yet."));
//
//        if(landlord == null) {
//            throw new UserException("User has not been registered yet.");
//        }
//        House house = mapperUtil.createHouseMapper(houseDTO, landlord);
//        House addedHouse = houseDAOImpl.save(house);
//        areaServiceImpl.addHouseArea(houseDTO, addedHouse);
//
//        if (houseDTO.getImageUrls() != null) {
//                assetRequestDTOList.addAll(
//                    assetServiceImpl.createHouseAssetList(
//                            houseDTO.getImageUrls(),
//                            addedHouse
//                    ));
//        }
//
//        if(houseDTO.getVideoUrl() != null){
//           String videoURL;
//           try {
//                videoURL = videoService.saveVideo(houseDTO.getVideoUrl());
//              } catch (Exception e) {
//                throw new RuntimeException("Error saving video: " + e.getMessage(), e);
//           }
//            AddHouseAssetRequestDTO videoAsset = new AddHouseAssetRequestDTO();
//            videoAsset.setUrl(videoURL);
//            videoAsset.setAssetType("VIDEO");
//            videoAsset.setHouse(addedHouse);
//            assetRequestDTOList.add(videoAsset);
//        }
//
//        for (AddHouseAssetRequestDTO assetRequestDTO : assetRequestDTOList) {
//            assetServiceImpl.addHouseAssets(assetRequestDTO);
//        }
//        amenitiesServiceImpl.addHouseAmenities(houseDTO, addedHouse);
//        this.propertyServiceImpl.addHousePropertyLookup(addedHouse);
//    }
//
//    @Override
//    @Transactional
//    @Retryable(
//            maxAttempts = 2,
//            backoff = @Backoff(delay = 2000)
//    )
//    public void updateHome(
//            UpdateHouseRequestDTO newHouseDetailsDTO,
//            String houseId,
//            List<MultipartFile> images,
//            MultipartFile video
//    ) {
//        try {
//            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//            String username = authentication.getName();
//            Users user = userRepository.findByUserName(username);
//            if (user == null) {
//                throw new UserException("User not found.");
//            }
//            Users landlord = userDAOImpl.findById(user.getId())
//                    .orElseThrow(() -> new UserException("User has not been registered yet."));
//            if (landlord == null) {
//                throw new UserException("User has not been registered yet.");
//            }
//            House existingHouse = houseDAOImpl.findById(UUID.fromString(houseId))
//                    .orElseThrow(
//                            () -> new HouseException("House with ID " + houseId + " does not exist."));
//            if (existingHouse == null) {
//                throw new HouseException("House with ID " + houseId + " does not exist.");
//            }
//            House updatedHouse = mapperUtil.updateHouseMapper(newHouseDetailsDTO, existingHouse, landlord);
//            System.out.println("House update process started.");
//            houseDAOImpl.updateHouse(updatedHouse);
//            if (newHouseDetailsDTO.getAmenities() != null) {
//                Amenities previousAmenities = amenitiesDAOImpl.getAmenitiesByHouseId(UUID.fromString(houseId))
//                        .orElseThrow(() -> new HouseException("Amenities not found for house ID: " + houseId));
//                Amenities updatedAmenitiesDTO = mapperUtil.createHouseAmenitiesMapper(newHouseDetailsDTO.getAmenities(), previousAmenities, updatedHouse);
//                amenitiesServiceImpl.updateAmenities(updatedAmenitiesDTO, updatedHouse, null, null);
//            }
//
//            if(newHouseDetailsDTO.getLength() != null && newHouseDetailsDTO.getBreadth() != null) {
//                Optional<Area> optionalArea = areaDAOImpl.getAreaByHouseId(UUID.fromString(houseId));
//
//                if (optionalArea.isPresent()) {
//                    Area previousArea = optionalArea.get();
//                    Area updatedArea = mapperUtil.createHouseAreaMapper(newHouseDetailsDTO, previousArea, updatedHouse);
//                    areaServiceImpl.updateArea(updatedArea, updatedHouse, null, null);
//                }
//            }
//
//            if (newHouseDetailsDTO.getImageUrlsToDelete() != null) {
//                for (UUID imageId : newHouseDetailsDTO.getImageUrlsToDelete()) {
//                    assetServiceImpl.deleteAsset(imageId);
//                }
//            }
//            if(newHouseDetailsDTO.getVideoUrlToDelete() != null){
//                assetServiceImpl.deleteAsset(newHouseDetailsDTO.getVideoUrlToDelete());
//            }
//
//            List<AddHouseAssetRequestDTO> assetRequestDTOList = new ArrayList<AddHouseAssetRequestDTO>();
//            if (images != null) {
//                assetRequestDTOList.addAll(
//                        assetServiceImpl.createHouseAssetList(
//                                images, updatedHouse
//                        ));
//            }
//
//            if(video != null){
//                String videoURL;
//                try {
//                    videoURL = videoService.saveVideo(video);
//                } catch (Exception e) {
//                    throw new RuntimeException("Error saving video: " + e.getMessage(), e);
//                }
//                AddHouseAssetRequestDTO videoAsset = new AddHouseAssetRequestDTO();
//                videoAsset.setUrl(videoURL);
//                videoAsset.setAssetType("VIDEO");
//                videoAsset.setHouse(updatedHouse);
//                assetRequestDTOList.add(videoAsset);
//            }
//
//            for (AddHouseAssetRequestDTO assetRequestDTO : assetRequestDTOList) {
//                assetServiceImpl.addHouseAssets(assetRequestDTO);
//            }
//            String key = "property:"+ houseId;
//            if(redisUtils.keyExists(key)){
//                redisUtils.delete(key);
//            }
//            redisUtils.save(key, this.propertyServiceImpl.getProperty(existingHouse.getId()));
//
//            System.out.println("House update process completed successfully.");
//        } catch (Exception e) {
//            throw new HouseException("Unexpected error during house update.", e);
//        }
//    }
//
//    @Transactional
//    public void deleteHouse(UUID houseId) {
//        House house = houseDAOImpl.findById(houseId)
//                .orElseThrow(() -> new HouseException("House not found."));
//        if(house == null) {
//            throw new HouseException("House not found.");
//        }
//        houseDAOImpl.deleteById(houseId);
//    }
//
//    @Transactional
//    public House getHouseById(UUID houseId) {
//        House house = houseDAOImpl.findById(houseId)
//                .orElseThrow(() -> new HouseException("House not found."));
//        if (house == null) {
//            throw new HouseException("House not found.");
//        }
//        return houseDAOImpl.getHouseDetailsById(houseId)
//                .orElseThrow(() -> new HouseException("House not found."));
//    }
//}
