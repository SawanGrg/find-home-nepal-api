package com.beta.FindHome.service.flat;

import com.beta.FindHome.dto.common.assets.AddFlatAssetRequestDTO;
import com.beta.FindHome.dto.property.flat.AddFlatRequestDTO;
import com.beta.FindHome.dto.property.flat.UpdateFlatRequestDTO;
import com.beta.FindHome.exception.FlatException;
import com.beta.FindHome.exception.UserException;
import com.beta.FindHome.model.*;
import com.beta.FindHome.repository.AmenitiesRepository;
import com.beta.FindHome.repository.AreaRepository;
import com.beta.FindHome.repository.FlatRepository;
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
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class FlatServiceImpl implements FlatService {

    private final FlatRepository flatRepository;
    private final AmenitiesRepository amenitiesRepository;
    private final AreaRepository areaRepository;
    private final UserRepository userRepository;
    private final AssetService assetService;
    private final VideoService videoService;
    private final AmenitiesService amenitiesService;
    private final AreaService areaService;
    private final MapperUtil mapperUtil;
    private final RedisUtils redisUtils;

    @Autowired
    public FlatServiceImpl(
            FlatRepository flatRepository,
            AmenitiesRepository amenitiesRepository,
            AreaRepository areaRepository,
            UserRepository userRepository,
            AssetService assetService,
            VideoService videoService,
            AmenitiesService amenitiesService,
            AreaService areaService,
            MapperUtil mapperUtil,
            RedisUtils redisUtils
    ) {
        this.flatRepository = flatRepository;
        this.amenitiesRepository = amenitiesRepository;
        this.areaRepository = areaRepository;
        this.userRepository = userRepository;
        this.assetService = assetService;
        this.videoService = videoService;
        this.amenitiesService = amenitiesService;
        this.areaService = areaService;
        this.mapperUtil = mapperUtil;
        this.redisUtils = redisUtils;
    }

    // =====================================================================
    // ADD
    // =====================================================================

    @Override
    @Transactional
    public void addFlat(
            AddFlatRequestDTO dto,
            List<MultipartFile> images,
            MultipartFile video
    ) {
        dto.setImageUrls(images);
        dto.setVideoUrl(video.isEmpty() ? null : video);

        Users landlord = getAuthenticatedLandlord();

        Flat flat = mapperUtil.createFlatMapper(dto, landlord);
        Flat savedFlat = flatRepository.save(flat);

        areaService.addFlatArea(dto, savedFlat);
        amenitiesService.addFlatAmenities(dto, savedFlat);

        List<AddFlatAssetRequestDTO> assets = new ArrayList<>();

        if (dto.getImageUrls() != null) {
            assets.addAll(assetService.createFlatAssetList(dto.getImageUrls(), savedFlat));
        }

        if (dto.getVideoUrl() != null) {
            String videoURL = saveVideoSafely(dto.getVideoUrl());
            AddFlatAssetRequestDTO videoAsset = new AddFlatAssetRequestDTO();
            videoAsset.setUrl(videoURL);
            videoAsset.setAssetType("VIDEO");
            videoAsset.setFlat(savedFlat);
            assets.add(videoAsset);
        }

        assets.forEach(assetService::addFlatAssets);
        // PropertyLookup removed — JOINED inheritance handles type resolution
    }

    // =====================================================================
    // UPDATE
    // =====================================================================

    @Override
    @Transactional
    public void updateFlat(
            UpdateFlatRequestDTO dto,
            String flatId,
            List<MultipartFile> images,
            MultipartFile video
    ) {
        Users landlord = getAuthenticatedLandlord();

        Flat existingFlat = flatRepository.findById(UUID.fromString(flatId))
                .orElseThrow(() -> new FlatException("Flat not found."));

        log.info("Updating flat ID: {}", existingFlat.getId());

        Flat updatedFlat = mapperUtil.updateFlatMapper(dto, existingFlat, landlord);
        flatRepository.save(updatedFlat);

        if (dto.getAmenities() != null) {
            Amenities existing = amenitiesRepository.findByPropertyId(updatedFlat.getId())
                    .orElseThrow(() -> new FlatException(
                            "Amenities not found for flat ID: " + updatedFlat.getId()));
            Amenities updated = mapperUtil.createFlatAmenitiesMapper(
                    dto.getAmenities(), existing, updatedFlat);
            amenitiesService.updateAmenities(updated, updatedFlat);
        }

        if (dto.getLength() != null || dto.getBreadth() != null) {
            areaRepository.findByPropertyId(updatedFlat.getId()).ifPresent(existingArea -> {
                Area updatedArea = mapperUtil.createFlatAreaMapper(
                        dto, existingArea, updatedFlat);
                areaService.updateArea(updatedArea, updatedFlat);
            });
        }

        if (dto.getImageUrlsToDelete() != null) {
            dto.getImageUrlsToDelete().forEach(assetService::deleteAsset);
        }
        if (dto.getVideoUrlToDelete() != null) {
            assetService.deleteAsset(dto.getVideoUrlToDelete());
        }

        List<AddFlatAssetRequestDTO> newAssets = new ArrayList<>();
        if (images != null) {
            newAssets.addAll(assetService.createFlatAssetList(images, updatedFlat));
        }
        if (video != null && !video.isEmpty()) {
            String videoURL = saveVideoSafely(video);
            AddFlatAssetRequestDTO videoAsset = new AddFlatAssetRequestDTO();
            videoAsset.setUrl(videoURL);
            videoAsset.setAssetType("VIDEO");
            videoAsset.setFlat(updatedFlat);
            newAssets.add(videoAsset);
        }
        newAssets.forEach(assetService::addFlatAssets);

        String cacheKey = "property:" + flatId;
        redisUtils.delete(cacheKey);
        log.info("Flat updated and cache invalidated for ID: {}", flatId);
    }

    // =====================================================================
    // DELETE
    // =====================================================================

    @Override
    @Transactional
    public void deleteFlat(UUID flatId) {
        if (!flatRepository.existsById(flatId)) {
            throw new FlatException("Flat not found.");
        }
        flatRepository.deleteById(flatId);
        redisUtils.delete("property:" + flatId);
        log.info("Flat deleted for ID: {}", flatId);
    }

    // =====================================================================
    // GET
    // =====================================================================

    @Override
    @Transactional()
    public Flat getFlatDetailsById(UUID flatId) {
        return flatRepository.findActiveWithDetailsById(flatId)
                .orElseThrow(() -> new FlatException("Flat not found."));
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
            throw new FlatException("Error saving video: " + e.getMessage());
        }
    }
}

//package com.beta.FindHome.service.flat;
//
//import com.beta.FindHome.dao.AmenitiesDAO;
//import com.beta.FindHome.dao.AreaDAO;
//import com.beta.FindHome.dao.FlatDAO;
//import com.beta.FindHome.dao.UserDAO;
//import com.beta.FindHome.dto.common.assets.AddFlatAssetRequestDTO;
//import com.beta.FindHome.dto.property.flat.AddFlatRequestDTO;
//import com.beta.FindHome.dto.property.flat.UpdateFlatRequestDTO;
//import com.beta.FindHome.exception.FlatException;
//import com.beta.FindHome.exception.UserException;
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
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Lazy;
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
//@Slf4j
//@Service
//public class FlatServiceImpl implements FlatService {
//
//    private final UserDAO userDAOImpl;
//    private final AssetService assetServiceImpl;
//    private final VideoService videoService;
//    private final AmenitiesService amenitiesServiceImpl;
//    private final AreaService areaServiceImpl;
//    private final MapperUtil mapperUtil;
//    private final FlatDAO flatDAOImpl;
//    private final PropertyService propertyServiceImpl;
//    private final AmenitiesDAO amenitiesDAOImpl;
//    private final AreaDAO areaDAOImpl;
//    private final RedisUtils redisUtils;
//    private final UserRepository userRepository;
//
//    @Autowired
//    public FlatServiceImpl(
//                           UserDAO userDAOImpl,
//                           AssetService assetServiceImpl,
//                           VideoService videoService,
//                           AmenitiesService amenitiesServiceImpl,
//                           AreaService areaServiceImpl,
//                           MapperUtil mapperUtil,
//                           FlatDAO flatDAOImpl,
//                           @Lazy PropertyService propertyServiceImpl,
//                           AmenitiesDAO amenitiesDAOImpl,
//                           AreaDAO areaDAOImpl,
//                           RedisUtils redisUtils,
//                           UserRepository userRepository
//    ) {
//        this.userDAOImpl = userDAOImpl;
//        this.assetServiceImpl = assetServiceImpl;
//        this.videoService = videoService;
//        this.amenitiesServiceImpl = amenitiesServiceImpl;
//        this.areaServiceImpl = areaServiceImpl;
//        this.mapperUtil = mapperUtil;
//        this.flatDAOImpl = flatDAOImpl;
//        this.propertyServiceImpl = propertyServiceImpl;
//        this.amenitiesDAOImpl = amenitiesDAOImpl;
//        this.areaDAOImpl = areaDAOImpl;
//        this.redisUtils = redisUtils;
//        this.userRepository = userRepository;
//    }
//
//    @Transactional
//    @Retryable(
//            maxAttempts = 3,
//            backoff = @Backoff(delay = 2000)
//    )
//    public void addFlat(
//            AddFlatRequestDTO flatRequestDTO,
//            List<MultipartFile> images,
//            MultipartFile video
//            ) {
//
//        // Optionally, process the images and videos
//        flatRequestDTO.setImageUrls(images);
//        flatRequestDTO.setVideoUrl(video.isEmpty() ? null : video);
//
//        List<AddFlatAssetRequestDTO> assetRequestDTOList = new ArrayList<AddFlatAssetRequestDTO>();
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
//
//        Flat flat = mapperUtil.createFlatMapper(flatRequestDTO, landlord);
//        Flat addedFlat = flatDAOImpl.save(flat);
//        areaServiceImpl.addFlatArea(flatRequestDTO, addedFlat);
//
//        if (flatRequestDTO.getImageUrls() != null) {
//                assetRequestDTOList.addAll(
//                    assetServiceImpl.createFlatAssetList(
//                            flatRequestDTO.getImageUrls(), addedFlat
//                    ));
//        }
//
//        if(flatRequestDTO.getVideoUrl() != null){
//           String videoURL;
//           try {
//                videoURL =  videoService.saveVideo(flatRequestDTO.getVideoUrl());
//              } catch (Exception e) {
//                throw new FlatException("Error saving video: " + e.getMessage());
//           }
//            AddFlatAssetRequestDTO videoAsset = new AddFlatAssetRequestDTO();
//            videoAsset.setUrl(videoURL);
//            videoAsset.setAssetType("VIDEO");
//            videoAsset.setFlat(addedFlat);
//            assetRequestDTOList.add(videoAsset);
//        }
//
//        for (AddFlatAssetRequestDTO assetRequestDTO : assetRequestDTOList) {
//            assetServiceImpl.addFlatAssets(assetRequestDTO);
//        }
//
//        amenitiesServiceImpl.addFlatAmenities(flatRequestDTO, addedFlat);
//        this.propertyServiceImpl.addFlatPropertyLookup(addedFlat);
//    }
//
//    @Override
//    @Transactional
//    @Retryable(
//            maxAttempts = 3,
//            backoff = @Backoff(delay = 2000),
//            noRetryFor = {FlatException.class, UserException.class}
//    )
//    public void updateFlat(
//            UpdateFlatRequestDTO newFlatDetailsDTO,
//            String roomId,
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
//            Optional<Flat> existingFlat = flatDAOImpl.findById(UUID.fromString(roomId));
//            if (existingFlat.isEmpty()) {
//                throw new FlatException("Flat not found.");
//            }
//            log.info("Updating flat with ID: {}", existingFlat.get().getId());
//            log.info("New flat details: {}", newFlatDetailsDTO.toString());
//            Flat updatedFlat = mapperUtil.updateFlatMapper(newFlatDetailsDTO, existingFlat.get(), landlord);
//            log.info("Updated flat details: {}", updatedFlat.toString());
//            flatDAOImpl.save(updatedFlat);
//            if (newFlatDetailsDTO.getAmenities() != null) {
//                Amenities previousAmenities = amenitiesDAOImpl.getAmenitiesByFlatId(updatedFlat.getId())
//                        .orElseThrow(() -> new FlatException("Amenities not found for flat ID: " + updatedFlat.getId()));
//                Amenities updatedAmenities = mapperUtil.createFlatAmenitiesMapper(newFlatDetailsDTO.getAmenities(), previousAmenities, updatedFlat);
//                amenitiesServiceImpl.updateAmenities(updatedAmenities, null, null, updatedFlat);
//            }
//            if (newFlatDetailsDTO.getLength() != null || newFlatDetailsDTO.getBreadth() != null) {
//                Optional<Area> optionalArea = areaDAOImpl.getAreaByFlatId(updatedFlat.getId());
//
//                if (optionalArea.isPresent()) {
//                    Area previousArea = optionalArea.get();
//                    Area updatedArea = mapperUtil.createFlatAreaMapper(newFlatDetailsDTO, previousArea, updatedFlat);
//                    areaServiceImpl.updateArea(updatedArea, null, null, updatedFlat);
//                }
//            }
//            if (newFlatDetailsDTO.getImageUrlsToDelete() != null) {
//                for (UUID imageId : newFlatDetailsDTO.getImageUrlsToDelete()) {
//                    assetServiceImpl.deleteAsset(imageId);
//                }
//            }
//            if (newFlatDetailsDTO.getVideoUrlToDelete() != null) {
//                assetServiceImpl.deleteAsset(newFlatDetailsDTO.getVideoUrlToDelete());
//            }
//            List<AddFlatAssetRequestDTO> assetRequestDTOList = new ArrayList<>();
//            if (images != null) {
//                assetRequestDTOList.addAll(assetServiceImpl.createFlatAssetList(images, updatedFlat));
//            }
//            if (video != null) {
//                String videoURL;
//                try {
//                    videoURL = videoService.saveVideo(video);
//                } catch (Exception e) {
//                    throw new FlatException("Error saving video: " + e.getMessage());
//                }
//                AddFlatAssetRequestDTO videoAsset = new AddFlatAssetRequestDTO();
//                videoAsset.setUrl(videoURL);
//                videoAsset.setAssetType("VIDEO");
//                videoAsset.setFlat(updatedFlat);
//                assetRequestDTOList.add(videoAsset);
//            }
//            for (AddFlatAssetRequestDTO assetRequestDTO : assetRequestDTOList) {
//                assetServiceImpl.addFlatAssets(assetRequestDTO);
//            }
//            String key = "property:"+ roomId;
//            if(redisUtils.keyExists(key)){
//                redisUtils.delete(key);
//            }
//            redisUtils.save(key, this.propertyServiceImpl.getProperty(existingFlat.get().getId()));
//        } catch (Exception e) {
//            log.error("Error updating flat details", e); // Improve error logging
//            throw new FlatException("Error updating flat details: " + e.getMessage());
//        }
//    }
//
//    @Transactional
//    public void deleteFlat(UUID flatId) {
//        Optional<Flat> flat = flatDAOImpl.findById(flatId);
//        if(flat.isEmpty()) {
//            throw new FlatException("Flat not found.");
//        }
//        flatDAOImpl.deleteById(flatId);
//    }
//
//    @Transactional
//    public Flat getFlatDetailsById(UUID flatId) {
//        Optional<Flat> flat = flatDAOImpl.findById(flatId);
//        if(flat.isEmpty()) {
//            throw new FlatException("Flat not found.");
//        }
//        return this.flatDAOImpl.getFlatDetailsById(flatId);
//    }
//
//}
