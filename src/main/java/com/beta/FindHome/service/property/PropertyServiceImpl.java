package com.beta.FindHome.service.property;

import com.beta.FindHome.dto.common.amenities.AmenitiesDTO;
import com.beta.FindHome.dto.common.area.AreaDTO;
import com.beta.FindHome.dto.common.assets.AssetDTO;
import com.beta.FindHome.dto.property.GetAllPropertyResponseDTO;
import com.beta.FindHome.dto.property.GetSpecificPropertyResponseDTO;
import com.beta.FindHome.dto.property.PropertyRequestDTO;
import com.beta.FindHome.enums.filter.FilterType;
import com.beta.FindHome.exception.ResourceNotFoundException;
import com.beta.FindHome.model.*;
import com.beta.FindHome.repository.FlatRepository;
import com.beta.FindHome.repository.HouseRepository;
import com.beta.FindHome.repository.PropertyRepository;
import com.beta.FindHome.repository.RoomRepository;
import com.beta.FindHome.service.house.SpecificationFilter;
import com.beta.FindHome.utils.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PropertyServiceImpl implements PropertyService {

    private final PropertyRepository propertyRepository;
    private final HouseRepository houseRepository;
    private final RoomRepository roomRepository;
    private final FlatRepository flatRepository;
    private final RedisUtils redisUtils;

    private final Map<String, Object> cache = new ConcurrentHashMap<>();

    @Autowired
    public PropertyServiceImpl(
            PropertyRepository propertyRepository,
            HouseRepository houseRepository,
            RoomRepository roomRepository,
            FlatRepository flatRepository,
            RedisUtils redisUtils
    ) {
        this.propertyRepository = propertyRepository;
        this.houseRepository = houseRepository;
        this.roomRepository = roomRepository;
        this.flatRepository = flatRepository;
        this.redisUtils = redisUtils;
    }

    // =====================================================================
    // LANDLORD LOOKUP
    // =====================================================================

    @Override
    @Transactional(readOnly = true)
    public Users findLandLordIdByPropertyId(UUID propertyId) {
        return propertyRepository.findByIdWithLandlord(propertyId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Property not found with ID: " + propertyId))
                .getLandlord();
    }

    // =====================================================================
    // GET ALL PROPERTIES
    // =====================================================================

    @Override
    @Transactional(readOnly = true)
    public Page<GetAllPropertyResponseDTO> getAllPropertyList(
            PropertyRequestDTO dto,
            int page,
            int size
    ) {
        String cacheKey = buildListCacheKey(dto, page, size);

        if (isCacheableRequest(dto, page, size)) {
            Page<GetAllPropertyResponseDTO> cached =
                    (Page<GetAllPropertyResponseDTO>) cache.get(cacheKey);
            if (cached != null) {
                log.debug("Cache hit for key: {}", cacheKey);
                return cached;
            }
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<GetAllPropertyResponseDTO> result = switch (dto.getFilterType()) {
            case HOUSE -> houseFilter(dto, pageable);
            case ROOM  -> roomFilter(dto, pageable);
            case FLAT  -> flatFilter(dto, pageable);
            case ALL   -> allFilter(dto, pageable);
        };

        if (isCacheableRequest(dto, page, size)) {
            cache.put(cacheKey, result);
        }
        return result;
    }

    // =====================================================================
    // GET SPECIFIC PROPERTY
    // =====================================================================

    @Override
    @Transactional(readOnly = true)
    public GetSpecificPropertyResponseDTO getProperty(UUID id) {
        String cacheKey = "property:" + id;

        GetSpecificPropertyResponseDTO cached =
                redisUtils.get(cacheKey, GetSpecificPropertyResponseDTO.class);
        if (cached != null) return cached;

        Property property = propertyRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Property not found with ID: " + id));

        GetSpecificPropertyResponseDTO response = convertToSpecificDTO(property);
        redisUtils.save(cacheKey, response);
        return response;
    }

    // =====================================================================
    // FILTER HELPERS
    // =====================================================================

    private Page<GetAllPropertyResponseDTO> houseFilter(
            PropertyRequestDTO dto,
            Pageable pageable
    ) {
        return houseRepository
                .findAll(SpecificationFilter.houseFilterByCriteria(dto), pageable)
                .map(this::convertHouseToListDTO);
    }

    private Page<GetAllPropertyResponseDTO> roomFilter(
            PropertyRequestDTO dto,
            Pageable pageable
    ) {
        return roomRepository
                .findAll(SpecificationFilter.roomFilterByCriteria(dto), pageable)
                .map(this::convertRoomToListDTO);
    }

    private Page<GetAllPropertyResponseDTO> flatFilter(
            PropertyRequestDTO dto,
            Pageable pageable
    ) {
        return flatRepository
                .findAll(SpecificationFilter.flatFilterByCriteria(dto), pageable)
                .map(this::convertFlatToListDTO);
    }

    private Page<GetAllPropertyResponseDTO> allFilter(
            PropertyRequestDTO dto,
            Pageable pageable
    ) {
        List<GetAllPropertyResponseDTO> houses = houseRepository
                .findAll(SpecificationFilter.houseFilterByCriteria(dto), pageable)
                .map(this::convertHouseToListDTO)
                .getContent();

        List<GetAllPropertyResponseDTO> rooms = roomRepository
                .findAll(SpecificationFilter.roomFilterByCriteria(dto), pageable)
                .map(this::convertRoomToListDTO)
                .getContent();

        List<GetAllPropertyResponseDTO> flats = flatRepository
                .findAll(SpecificationFilter.flatFilterByCriteria(dto), pageable)
                .map(this::convertFlatToListDTO)
                .getContent();

        List<GetAllPropertyResponseDTO> all = new ArrayList<>();
        all.addAll(houses);
        all.addAll(rooms);
        all.addAll(flats);

        long total = houseRepository.count(SpecificationFilter.houseFilterByCriteria(dto))
                + roomRepository.count(SpecificationFilter.roomFilterByCriteria(dto))
                + flatRepository.count(SpecificationFilter.flatFilterByCriteria(dto));

        return new PageImpl<>(all, pageable, total);
    }

    // =====================================================================
    // DTO CONVERSIONS — LIST
    // =====================================================================

    private GetAllPropertyResponseDTO convertHouseToListDTO(House house) {
        GetAllPropertyResponseDTO dto = buildBaseListDTO(house, "HOUSE");
        dto.setHouseFloors(house.getFloors());
        dto.setBedRooms(house.getBedRooms());
        dto.setBathRooms(house.getBathRooms());
        dto.setKitchen(house.getKitchen());
        dto.setLivingRoom(house.getLivingRoom());
        return dto;
    }

    private GetAllPropertyResponseDTO convertFlatToListDTO(Flat flat) {
        GetAllPropertyResponseDTO dto = buildBaseListDTO(flat, "FLAT");
        dto.setBedRooms(flat.getBedRooms());
        dto.setBathRooms(flat.getBathRooms());
        dto.setKitchen(flat.getKitchen());
        dto.setFlatLivingRoom(flat.getLivingRoom());
        return dto;
    }

    private GetAllPropertyResponseDTO convertRoomToListDTO(Room room) {
        return buildBaseListDTO(room, "ROOM");
    }

    private GetAllPropertyResponseDTO buildBaseListDTO(Property property, String type) {
        GetAllPropertyResponseDTO dto = new GetAllPropertyResponseDTO();
        dto.setId(property.getId());
        dto.setPropertyType(type);
        dto.setPrice(property.getPrice());
        dto.setDescription(property.getDescription());
        dto.setDistrict(property.getAddress().getDistrict());
        dto.setCity(property.getAddress().getCity());
        dto.setWard(property.getAddress().getWard());
        dto.setTole(property.getAddress().getTole());
        dto.setRules(property.getRules());
        dto.setIsAvailable(property.isAvailable());
        dto.setIsVerified(property.isVerified());
        dto.setIsDeleted(property.isDeleted());
        dto.setCreatedAt(property.getCreatedAt());

        if (property.getAssets() != null) {
            dto.setAssets(property.getAssets().stream()
                    .filter(a -> "IMAGE".equals(a.getAssetType()))
                    .map(a -> new AssetDTO(a.getId(), a.getAssetType(), a.getAssetURL()))
                    .toList());
        }

        if (property.getAmenities() != null) {
            dto.setAmenities(toAmenitiesDTO(property.getAmenities()));
        }

        if (property.getArea() != null) {
            dto.setArea(toAreaDTO(property.getArea()));
        }

        return dto;
    }

    // =====================================================================
    // DTO CONVERSIONS — SPECIFIC
    // =====================================================================

    private GetSpecificPropertyResponseDTO convertToSpecificDTO(Property property) {
        GetSpecificPropertyResponseDTO dto = buildBaseSpecificDTO(property);

        if (property instanceof House house) {
            dto.setHouseFloors(house.getFloors());
            dto.setBedRooms(house.getBedRooms());
            dto.setBathRooms(house.getBathRooms());
            dto.setKitchen(house.getKitchen());
            dto.setLivingRoom(house.getLivingRoom());
        } else if (property instanceof Flat flat) {
            dto.setBedRooms(flat.getBedRooms());
            dto.setBathRooms(flat.getBathRooms());
            dto.setKitchen(flat.getKitchen());
            dto.setLivingRoom(flat.getLivingRoom());
        }

        return dto;
    }

    private GetSpecificPropertyResponseDTO buildBaseSpecificDTO(Property property) {
        GetSpecificPropertyResponseDTO dto = new GetSpecificPropertyResponseDTO();
        dto.setId(property.getId());
        dto.setUserName(property.getLandlord().getUserName());
        dto.setPrice(property.getPrice());
        dto.setDescription(property.getDescription());
        dto.setDistrict(property.getAddress().getDistrict());
        dto.setCity(property.getAddress().getCity());
        dto.setWard(property.getAddress().getWard());
        dto.setTole(property.getAddress().getTole());
        dto.setRules(property.getRules());
        dto.setIsAvailable(property.isAvailable());
        dto.setIsVerified(property.isVerified());

        if (property.getAssets() != null) {
            dto.setAssets(property.getAssets().stream()
                    .map(a -> new AssetDTO(a.getId(), a.getAssetType(), a.getAssetURL()))
                    .toList());
        }

        if (property.getAmenities() != null) {
            dto.setAmenities(toAmenitiesDTO(property.getAmenities()));
        }

        if (property.getArea() != null) {
            dto.setArea(toAreaDTO(property.getArea()));
        }

        return dto;
    }

    // =====================================================================
    // PRIVATE MAPPING HELPERS
    // =====================================================================

    private AmenitiesDTO toAmenitiesDTO(Amenities a) {
        return new AmenitiesDTO(
                a.getId(),
                a.isHasParking(),
                a.isHasWifi(),
                a.isHasSecurityStaff(),
                a.isHasUnderGroundWaterTank(),
                a.isHasTV(),
                a.isHasCCTV(),
                a.isHasAC(),
                a.isHasFridge(),
                a.isHasBalcony(),
                a.isHasWater(),
                a.isHasSolarWaterHeater(),
                a.isHasFan(),
                a.getFurnishingStatus()
        );
    }

    private AreaDTO toAreaDTO(Area ar) {
        return new AreaDTO(
                ar.getId(),
                ar.getLength(),
                ar.getBreadth(),
                ar.getTotalArea()
        );
    }

    // =====================================================================
    // CACHE HELPERS
    // =====================================================================

    private String buildListCacheKey(PropertyRequestDTO dto, int page, int size) {
        return "property_list:" + dto.getFilterType().name()
                + ":page" + page + ":size" + size;
    }

    private boolean isCacheableRequest(PropertyRequestDTO request, int page, int size) {
        if (page != 0 || size != 5) return false;
        if (request.getFilterType() == FilterType.ALL) return false;
        if (!Boolean.TRUE.equals(request.getIsAvailable())   ||
                !Boolean.FALSE.equals(request.getIsDeleted())    ||
                !Boolean.TRUE.equals(request.getIsVerified()))   return false;

        return request.getLandlordId()              == null &&
                request.getMinPrice()                == null &&
                request.getMaxPrice()                == null &&
                request.getDistrict()                == null &&
                request.getCity()                    == null &&
                request.getWard()                    == null &&
                request.getTole()                    == null &&
                request.getSortBy()                  == null &&
                request.getHasParking()              == null &&
                request.getHasWifi()                 == null &&
                request.getHasSecurityStaff()        == null &&
                request.getHasUnderGroundWaterTank() == null &&
                request.getHasTV()                   == null &&
                request.getHasCCTV()                 == null &&
                request.getHasAC()                   == null &&
                request.getHasFridge()               == null &&
                request.getHasBalcony()              == null &&
                request.getHasWater()                == null &&
                request.getHasSolarWaterHeater()     == null &&
                request.getHasFan()                  == null;
    }

    @Scheduled(fixedRate = 300000)
    public void clearCache() {
        cache.clear();
        log.info("Property list cache cleared");
    }
}
//package com.beta.FindHome.service.property;
//
//import com.beta.FindHome.dto.property.GetAllPropertyResponseDTO;
//import com.beta.FindHome.dto.property.GetSpecificPropertyResponseDTO;
//import com.beta.FindHome.dto.property.PropertyRequestDTO;
//import com.beta.FindHome.enums.filter.FilterType;
//import com.beta.FindHome.model.*;
//import com.beta.FindHome.repository.FlatRepository;
//import com.beta.FindHome.repository.HouseRepository;
//import com.beta.FindHome.repository.PropertyLookupRepository;
//import com.beta.FindHome.repository.RoomRepository;
//import com.beta.FindHome.service.flat.FlatService;
//import com.beta.FindHome.service.house.HouseService;
//import com.beta.FindHome.service.house.SpecificationFilter;
//import com.beta.FindHome.service.room.RoomService;
//import com.beta.FindHome.utils.MapperUtil;
//import com.beta.FindHome.utils.RedisUtils;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Lazy;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageImpl;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.domain.Pageable;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Service;
//
//import java.time.LocalDateTime;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//import java.util.UUID;
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.stream.Collectors;
//
//
//@Service
//public class PropertyServiceImpl implements PropertyService {
//
//    private final RoomRepository roomRepository;
//    private final HouseRepository houseRepository;
//    private final FlatRepository flatRepository;
//    private final MapperUtil mapperUtil;
//    private final PropertyLookupRepository propertyLookupRepository;
//    private final RoomService roomService;
//    private final HouseService houseService;
//    private final FlatService flatService;
//    private final RedisUtils redisUtils;
//
//    private final Map<String, Object> cache = new ConcurrentHashMap<>();
//
//    @Autowired
//    public PropertyServiceImpl(
//                               RoomRepository roomRepository,
//                               HouseRepository houseRepository,
//                               FlatRepository flatRepository,
//                               MapperUtil mapperUtil,
//                               PropertyLookupRepository propertyLookupRepository,
//                               @Lazy RoomService roomService,
//                               @Lazy HouseService houseService,
//                               @Lazy FlatService flatService,
//                                 RedisUtils redisUtils
//    ) {
//        this.roomRepository = roomRepository;
//        this.houseRepository = houseRepository;
//        this.flatRepository = flatRepository;
//        this.mapperUtil = mapperUtil;
//        this.propertyLookupRepository = propertyLookupRepository;
//        this.roomService = roomService;
//        this.houseService = houseService;
//        this.flatService = flatService;
//        this.redisUtils = redisUtils;
//    }
//
//    //Extract user id (landlord id) based on the property id
//    public Users findLandLordIdByPropertyId(UUID propertyId) {
//        // Extract property type from property lookup database
//        PropertyLookup propertyLookup = this.propertyLookupRepository.findByPropertyId(propertyId);
//
//        if (propertyLookup == null) {
//            throw new IllegalArgumentException("Property not found with ID: " + propertyId);
//        }
//
//        // Fetch landlord based on the property type
//        switch (propertyLookup.getTableName().toUpperCase()) {
//            case "HOUSE": {
//                House houseEntity = houseRepository.findById(propertyId)
//                        .orElseThrow(() -> new IllegalArgumentException("House not found with ID: " + propertyId));
//                return houseEntity.getLandlordId();
//            }
//            case "FLAT": {
//                Flat flatEntity = flatRepository.findById(propertyId)
//                        .orElseThrow(() -> new IllegalArgumentException("Flat not found with ID: " + propertyId));
//                return flatEntity.getLandlordId();
//            }
//            case "ROOM": {
//                Room roomEntity = roomRepository.findById(propertyId)
//                        .orElseThrow(() -> new IllegalArgumentException("Room not found with ID: " + propertyId));
//                return roomEntity.getLandlordId();
//            }
//            default:
//                throw new IllegalArgumentException("Unknown property type: " + propertyLookup.getTableName());
//        }
//    }
//
//    //    ----------------------get all property list------------------------------------------------
//    public Page<GetAllPropertyResponseDTO> getAllPropertyList(
//            PropertyRequestDTO propertyRequestDTO,
//            int page,
//            int size
//    ) {
//        String cacheKey = "property_list:" + propertyRequestDTO.getFilterType().name() + ":page0:size5";
//        if (isCacheableRequest(propertyRequestDTO, page, size)) {
//            Page<GetAllPropertyResponseDTO> cachedResponse = (Page<GetAllPropertyResponseDTO>) cache.get(cacheKey);
//            if (cachedResponse != null) {
//                return cachedResponse;
//            }
//        }
//        Pageable pageable = PageRequest.of(page, size);
//        Page<GetAllPropertyResponseDTO> result;
//
//        switch (propertyRequestDTO.getFilterType()) {
//            case HOUSE:
//                result = houseFilter(propertyRequestDTO, pageable);
//                break;
//            case ROOM:
//                result = roomFilter(propertyRequestDTO, pageable);
//                break;
//            case FLAT:
//                result = flatFilter(propertyRequestDTO, pageable);
//                break;
//            case ALL:
//                result = allFilter(propertyRequestDTO, pageable);
//                break;
//            default:
//                throw new IllegalArgumentException("Unsupported filter type: " + propertyRequestDTO.getFilterType());
//        }
//
//        if (isCacheableRequest(propertyRequestDTO, page, size)) {
//            cache.put(cacheKey, result);
//        }
//        return result;
//    }
//
////    ----------------------get specific property------------------------------------------------
//    public GetSpecificPropertyResponseDTO getProperty(UUID id) {
//        String key = "property:" + id;
//        GetSpecificPropertyResponseDTO cachedResponse = redisUtils.get(key, GetSpecificPropertyResponseDTO.class);
//        if (cachedResponse != null) {
//            return cachedResponse;
//        }
//        PropertyLookup propertyLookup = propertyLookupRepository.findByPropertyId(id);
//        if (propertyLookup == null) {
//            throw new IllegalArgumentException("Property not found");
//        }
//        GetSpecificPropertyResponseDTO response;
//        switch (propertyLookup.getTableName()) {
//            case "ROOM":
//                Room room = this.roomService.getRoomById(id);
//                response = convertRoomToSpecificDTO(room);
//                break;
//            case "HOUSE":
//                House house = this.houseService.getHouseById(id);
//                response = convertHouseToSpecificDTO(house);
//                break;
//            case "FLAT":
//                Flat flat = this.flatService.getFlatDetailsById(id);
//                response = convertFlatToSpecificDTO(flat);
//                break;
//            default:
//                throw new IllegalArgumentException("Unsupported property type: " + propertyLookup.getTableName());
//        }
//        redisUtils.save(key, response);
//        return response;
//    }
//
//
////   saves property lookup details in database for house, room and flat
//    public void addHousePropertyLookup(House house){
//        PropertyLookup propertyLookup = new PropertyLookup();
//        propertyLookup.setTableName("HOUSE");
//        propertyLookup.setPropertyId(house.getId());
//        propertyLookup.setCreatedAt(LocalDateTime.now());
//        propertyLookupRepository.save(propertyLookup);
//    }
//    public void addRoomPropertyLookup(Room room){
//        PropertyLookup propertyLookup = new PropertyLookup();
//        propertyLookup.setTableName("ROOM");
//        propertyLookup.setPropertyId(room.getId());
//        propertyLookup.setCreatedAt(LocalDateTime.now());
//        propertyLookupRepository.save(propertyLookup);
//    }
//    public void addFlatPropertyLookup(Flat flat){
//        PropertyLookup propertyLookup = new PropertyLookup();
//        propertyLookup.setTableName("FLAT");
//        propertyLookup.setPropertyId(flat.getId());
//        propertyLookup.setCreatedAt(LocalDateTime.now());
//        propertyLookupRepository.save(propertyLookup);
//    }
//
////    ----------------------filter flat, room, house property logic ------------------------------------------------
//    private Page<GetAllPropertyResponseDTO> houseFilter(PropertyRequestDTO propertyRequestDTO, Pageable pageable) {
//        Page<House> housePage = houseRepository.findAll(
//                SpecificationFilter.houseFilterByCriteria(propertyRequestDTO),
//                pageable
//        );
//        Page<GetAllPropertyResponseDTO> housePageTO = housePage
//                .map(this::convertHouseToListDTO);
//        return housePageTO;
//    }
//    private Page<GetAllPropertyResponseDTO> roomFilter(PropertyRequestDTO propertyRequestDTO, Pageable pageable) {
//        Page<Room> roomPage = roomRepository.findAll(
//                SpecificationFilter.roomFilterByCriteria(propertyRequestDTO),
//                pageable
//        );
//        Page<GetAllPropertyResponseDTO> roomPageTO = roomPage
//                .map(this::convertRoomToListDTO);
//        return roomPageTO;
//    }
//    private Page<GetAllPropertyResponseDTO> flatFilter(PropertyRequestDTO propertyRequestDTO, Pageable pageable) {
//        Page<Flat> flatPage = flatRepository.findAll(
//                SpecificationFilter.flatFilterByCriteria(propertyRequestDTO),
//                pageable
//        );
//        Page<GetAllPropertyResponseDTO> flatPageTO = flatPage
//                .map(this::convertFlatToListDTO);
//        return flatPageTO;
//    }
//    private Page<GetAllPropertyResponseDTO> allFilter(PropertyRequestDTO propertyRequestDTO, Pageable pageable) {
//        // Fetch all houses, rooms, and flats based on the filter criteria
//        List<House> houses = houseRepository.findAll(SpecificationFilter.houseFilterByCriteria(propertyRequestDTO));
//        List<Room> rooms = roomRepository.findAll(SpecificationFilter.roomFilterByCriteria(propertyRequestDTO));
//        List<Flat> flats = flatRepository.findAll(SpecificationFilter.flatFilterByCriteria(propertyRequestDTO));
//
//        // Convert all entities to DTOs
//        List<GetAllPropertyResponseDTO> allProperties = new ArrayList<>();
//        allProperties.addAll(houses.stream().map(this::convertHouseToListDTO).collect(Collectors.toList()));
//        allProperties.addAll(rooms.stream().map(this::convertRoomToListDTO).collect(Collectors.toList()));
//        allProperties.addAll(flats.stream().map(this::convertFlatToListDTO).collect(Collectors.toList()));
//
//        // Apply pagination manually
//        int totalElements = allProperties.size();
//        int start = (int) pageable.getOffset();
//        int end = Math.min((start + pageable.getPageSize()), totalElements);
//
//        List<GetAllPropertyResponseDTO> paginatedProperties = allProperties.subList(start, end);
//
//        // Return a Page object with the paginated results
//        return new PageImpl<>(paginatedProperties, pageable, totalElements);
//    }
//
//    //    ----------------------list property dto conversion------------------------------------------------
//    public GetAllPropertyResponseDTO convertFlatToListDTO(Flat flat){
//            GetAllPropertyResponseDTO dto = new GetAllPropertyResponseDTO();
//
//        dto.setId(flat.getId());
//        dto.setPropertyType("FLAT");
//        dto.setPrice(flat.getFlatPrice());
//        dto.setDescription(flat.getFlatDescription());
//        dto.setDistrict(flat.getDistrict());
//        dto.setCity(flat.getCity());
//        dto.setWard(flat.getWard());
//        dto.setTole(flat.getTole());
//        dto.setRules(flat.getFlatRules());
//        dto.setIsAvailable(flat.getIsAvailable());
//        dto.setIsVerified(flat.getIsVerified());
//        dto.setIsDeleted(flat.getIsDeleted());
//
//        dto.setBedRooms(flat.getBedRooms());
//        dto.setBathRooms(flat.getBathRooms());
//        dto.setKitchen(flat.getKitchen());
//        dto.setFlatLivingRoom(flat.getLivingRoom());
//        dto.setCreatedAt(flat.getCreatedAt());
//
//
//        // Set assets (filter only image assets)
//        if (flat.getAssets() != null) {
//            List<Assets> imageAssets = flat.getAssets().stream()
//                    .filter(asset -> "IMAGE".equals(asset.getAssetType()))
//                    .collect(Collectors.toList());
//            dto.setAssets(imageAssets);
//        }
//        if (flat.getAmenities() != null) {
//            Amenities amenities = flat.getAmenities();
//            dto.setAmenities(amenities);
//
//        }
//        if (flat.getArea() != null) {
//            Area area = flat.getArea();
//            dto.setArea(area);
//        }
//        return dto;
//    }
//    public GetAllPropertyResponseDTO convertHouseToListDTO(House house) {
//        GetAllPropertyResponseDTO dto = new GetAllPropertyResponseDTO();
//        dto.setId(house.getId());
//        dto.setPropertyType("HOUSE");
//        dto.setPrice(house.getHousePrice());
//        dto.setDescription(house.getHouseDescription());
//        dto.setDistrict(house.getDistrict());
//        dto.setCity(house.getCity());
//        dto.setWard(house.getWard());
//        dto.setTole(house.getTole());
//        dto.setRules(house.getHouseRules());
//        dto.setIsAvailable(house.getIsAvailable());
//        dto.setIsVerified(house.getIsVerified());
//        dto.setIsDeleted(house.getIsDeleted());
//        dto.setHouseFloors(house.getHouseFloors());
//        dto.setBedRooms(house.getBedRooms());
//        dto.setBathRooms(house.getBathRooms());
//        dto.setKitchen(house.getKitchen());
//        dto.setLivingRoom(house.getLivingRoom());
//        dto.setCreatedAt(house.getCreatedAt());
//
//
//        // Set assets (filter only image assets)
//        if (house.getAssets() != null) {
//            List<Assets> imageAssets = house.getAssets().stream()
//                    .filter(asset -> "IMAGE".equals(asset.getAssetType()))
//                    .collect(Collectors.toList());
//            dto.setAssets(imageAssets);
//        }
//        if (house.getAmenities() != null) {
//            Amenities amenities = house.getAmenities();
//            dto.setAmenities(amenities);
//        }
//        if (house.getArea() != null) {
//            Area area = house.getArea();
//            dto.setArea(area);
//        }
//        return dto;
//    }
//    public GetAllPropertyResponseDTO convertRoomToListDTO(Room room) {
//        GetAllPropertyResponseDTO dto = new GetAllPropertyResponseDTO();
//        dto.setId(room.getId());
//        dto.setPropertyType("ROOM");
//        dto.setPrice(room.getRoomPrice());
//        dto.setDescription(room.getRoomDescription());
//        dto.setDistrict(room.getDistrict());
//        dto.setCity(room.getCity());
//        dto.setWard(room.getWard());
//        dto.setTole(room.getTole());
//        dto.setRules(room.getRoomRules());
//        dto.setIsAvailable(room.getIsAvailable());
//        dto.setIsVerified(room.getIsVerified());
//        dto.setIsDeleted(room.getIsDeleted());
//        dto.setCreatedAt(room.getCreatedAt());
//
//        // Set assets (filter only image assets)
//        if (room.getAssets() != null) {
//            List<Assets> imageAssets = room.getAssets().stream()
//                    .filter(asset -> "IMAGE".equals(asset.getAssetType()))
//                    .collect(Collectors.toList());
//            dto.setAssets(imageAssets);
//        }
//        if (room.getAmenities() != null) {
//            Amenities amenities = room.getAmenities();
//            dto.setAmenities(amenities);
//
//        }
//        if (room.getArea() != null) {
//            Area area = room.getArea();
//            dto.setArea(area);
//        }
//        return dto;
//    }
//
//// ----------------------specific property dto conversion------------------------------------------------
//    public GetSpecificPropertyResponseDTO convertRoomToSpecificDTO(Room room) {
//    GetSpecificPropertyResponseDTO dto = new GetSpecificPropertyResponseDTO();
//    dto.setId(room.getId());
//    dto.setUserName(room.getLandlordId().getUsername());
//    dto.setPrice(room.getRoomPrice());
//    dto.setDescription(room.getRoomDescription());
//    dto.setDistrict(room.getDistrict());
//    dto.setCity(room.getCity());
//    dto.setWard(room.getWard());
//    dto.setTole(room.getTole());
//    dto.setRules(room.getRoomRules());
//    dto.setIsAvailable(room.getIsAvailable());
//    dto.setIsVerified(room.getIsVerified());
//
//    // Set assets (filter only image assets)
//    if (room.getAssets() != null) {
//        dto.setAssets(room.getAssets());
//    }
//    if (room.getAmenities() != null) {
//        dto.setAmenities(room.getAmenities());  // Single object
//    }
//
//    if (room.getArea() != null) {
//        dto.setArea(room.getArea());  // Single object
//    }
//    return dto;
//}
//    public GetSpecificPropertyResponseDTO convertHouseToSpecificDTO(House house){
//            GetSpecificPropertyResponseDTO dto = new GetSpecificPropertyResponseDTO();
//        dto.setId(house.getId());
//        dto.setPrice(house.getHousePrice());
//        dto.setDescription(house.getHouseDescription());
//        dto.setDistrict(house.getDistrict());
//        dto.setCity(house.getCity());
//        dto.setWard(house.getWard());
//        dto.setTole(house.getTole());
//        dto.setRules(house.getHouseRules());
//        dto.setIsAvailable(house.getIsAvailable());
//        dto.setIsVerified(house.getIsVerified());
//
//        dto.setHouseFloors(house.getHouseFloors());
//        dto.setBedRooms(house.getBedRooms());
//        dto.setBathRooms(house.getBathRooms());
//        dto.setKitchen(house.getKitchen());
//        dto.setLivingRoom(house.getLivingRoom());
//
//        // Set assets (filter only image assets)
//        if (house.getAssets() != null) {
//            dto.setAssets(house.getAssets());
//        }
//
//        if (house.getAmenities() != null) {
//            dto.setAmenities(house.getAmenities());
//        }
//
//        if (house.getArea() != null) {
//            dto.setArea(house.getArea());
//        }
//        return dto;
//    }
//    public GetSpecificPropertyResponseDTO convertFlatToSpecificDTO(Flat flat){
//        GetSpecificPropertyResponseDTO dto = new GetSpecificPropertyResponseDTO();
//        dto.setId(flat.getId());
//        dto.setPrice(flat.getFlatPrice());
//        dto.setDescription(flat.getFlatDescription());
//        dto.setDistrict(flat.getDistrict());
//        dto.setCity(flat.getCity());
//        dto.setWard(flat.getWard());
//        dto.setTole(flat.getTole());
//        dto.setRules(flat.getFlatRules());
//        dto.setIsAvailable(flat.getIsAvailable());
//        dto.setIsVerified(flat.getIsVerified());
//
//        dto.setBedRooms(flat.getBedRooms());
//        dto.setBathRooms(flat.getBathRooms());
//        dto.setKitchen(flat.getKitchen());
//        dto.setLivingRoom(flat.getLivingRoom());
//
//        // Set assets (filter only image assets)
//        if (flat.getAssets() != null) {
//            dto.setAssets(flat.getAssets());
//        }
//
//        if (flat.getAmenities() != null) {
//            dto.setAmenities(flat.getAmenities());
//        }
//
//        if (flat.getArea() != null) {
//            dto.setArea(flat.getArea());
//        }
//        return dto;
//    }
//
//    private boolean isCacheableRequest(PropertyRequestDTO request, int page, int size) {
//        // Check if page and size match the cacheable criteria
//        if (page != 0 || size != 5) {
//            return false;
//        }
//
//        // Check if filter type is one of the basic types (not ALL)
//        if (request.getFilterType() == FilterType.ALL) {
//            return false;
//        }
//
//        if (    !Boolean.TRUE.equals(request.getIsAvailable()) ||
//                !Boolean.FALSE.equals(request.getIsDeleted()) ||
//                !Boolean.TRUE.equals(request.getIsVerified())
//        ) {
//            return false;
//        }
//
//        // Check if any additional filters are applied
//        return request.getLandlordId() == null &&
//                request.getMinPrice() == null &&
//                request.getMaxPrice() == null &&
//                request.getDistrict() == null &&
//                request.getCity() == null &&
//                request.getWard() == null &&
//                request.getTole() == null &&
//                request.getSortBy() == null &&
//                // Check all amenities filters
//                request.getHasParking() == null &&
//                request.getHasWifi() == null &&
//                request.getHasSecurityStaff() == null &&
//                request.getHasUnderGroundWaterTank() == null &&
//                request.getHasTV() == null &&
//                request.getHasCCTV() == null &&
//                request.getHasAC() == null &&
//                request.getHasFridge() == null &&
//                request.getHasBalcony() == null &&
//                request.getHasWater() == null &&
//                request.getHasSolarWaterHeater() == null &&
//                request.getHasFan() == null;
//    }
//
//    @Scheduled(fixedRate = 300000) // Run every 5 minutes
//    public void clearCache() {
//        cache.clear();
//    }
//}
