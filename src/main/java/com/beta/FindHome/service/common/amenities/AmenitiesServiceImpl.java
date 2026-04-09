// AmenitiesServiceImpl.java
package com.beta.FindHome.service.common.amenities;

import com.beta.FindHome.dto.common.amenities.AddAmenitiesDTO;
import com.beta.FindHome.dto.property.flat.AddFlatRequestDTO;
import com.beta.FindHome.dto.property.house.AddHouseRequestDTO;
import com.beta.FindHome.dto.property.room.AddRoomRequestDTO;
import com.beta.FindHome.exception.AmenitiesException;
import com.beta.FindHome.model.Amenities;
import com.beta.FindHome.model.Property;
import com.beta.FindHome.repository.AmenitiesRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

@Service
public class AmenitiesServiceImpl implements AmenitiesService {

    private static final Logger logger = LoggerFactory.getLogger(AmenitiesServiceImpl.class);

    private final AmenitiesRepository amenitiesRepository;

    @Autowired
    public AmenitiesServiceImpl(AmenitiesRepository amenitiesRepository) {
        this.amenitiesRepository = amenitiesRepository;
    }

    @Transactional
    public void addHouseAmenities(AddHouseRequestDTO dto, Property house) {
        Assert.notNull(dto, "House request DTO cannot be null");
        Assert.notNull(house, "House cannot be null");
        saveAmenities(buildAmenities(dto.getAmenities(), house));
    }

    @Transactional
    public void addFlatAmenities(AddFlatRequestDTO dto, Property flat) {
        Assert.notNull(dto, "Flat request DTO cannot be null");
        Assert.notNull(flat, "Flat cannot be null");
        saveAmenities(buildAmenities(dto.getAmenities(), flat));
    }

    @Transactional
    public void addRoomAmenities(AddRoomRequestDTO dto, Property room) {
        Assert.notNull(dto, "Room request DTO cannot be null");
        Assert.notNull(room, "Room cannot be null");
        saveAmenities(buildAmenities(dto.getAmenities(), room));
    }

    @Transactional
    public void updateAmenities(Amenities amenities, Property property) {
        Assert.notNull(amenities, "Amenities cannot be null");
        Assert.notNull(property, "Property cannot be null");
        amenities.setProperty(property);
        amenitiesRepository.save(amenities);
        logger.info("Updated amenities for property ID: {}", property.getId());
    }

    // === PRIVATE HELPERS ===

    private Amenities buildAmenities(AddAmenitiesDTO dto, Property property) {
        Assert.notNull(dto, "Amenities DTO cannot be null");
        return Amenities.builder()
                .property(property)
                .hasParking(dto.isHasParking())
                .hasWifi(dto.isHasWifi())
                .hasSecurityStaff(dto.isHasSecurityStaff())
                .hasUnderGroundWaterTank(dto.isHasUnderGroundWaterTank())
                .hasTV(dto.isHasTV())
                .hasCCTV(dto.isHasCCTV())
                .hasAC(dto.isHasAC())
                .hasFridge(dto.isHasFridge())
                .hasBalcony(dto.isHasBalcony())
                .hasWater(dto.isHasWater())
                .hasSolarWaterHeater(dto.isHasSolarWaterHeater())
                .hasFan(dto.isHasFan())
                .furnishingStatus(dto.getFurnishingStatus())
                .build();
    }

    private void saveAmenities(Amenities amenities) {
        try {
            amenitiesRepository.save(amenities);
            logger.info("Saved amenities for property ID: {}", amenities.getProperty().getId());
        } catch (Exception e) {
            throw new AmenitiesException("Failed to save amenities: " + e.getMessage(), e);
        }
    }
}

//package com.beta.FindHome.service.common.amenities;
//
//import com.beta.FindHome.dao.AmenitiesDAO;
//import com.beta.FindHome.dto.common.amenities.AddAmenitiesDTO;
//import com.beta.FindHome.dto.property.flat.AddFlatRequestDTO;
//import com.beta.FindHome.dto.property.house.AddHouseRequestDTO;
//import com.beta.FindHome.dto.property.room.AddRoomRequestDTO;
//import com.beta.FindHome.enums.model.Furnish;
//import com.beta.FindHome.exception.AmenitiesException;
//import com.beta.FindHome.exception.DatabaseException;
//import com.beta.FindHome.model.Amenities;
//import com.beta.FindHome.model.Flat;
//import com.beta.FindHome.model.House;
//import com.beta.FindHome.model.Room;
//import org.glassfish.jaxb.runtime.v2.runtime.IllegalAnnotationsException;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.retry.annotation.Backoff;
//import org.springframework.retry.annotation.Retryable;
//import org.springframework.stereotype.Service;
//import org.springframework.util.Assert;
//
//@Service
//public class AmenitiesServiceImpl implements AmenitiesService {
//
//    private static final Logger logger = LoggerFactory.getLogger(AmenitiesServiceImpl.class);
//
//    private static final int MAX_RETRY_ATTEMPTS = 3;
//    private static final long RETRY_DELAY_MS = 2000;
//
//    private final AmenitiesDAO amenitiesDAO;
//
//    @Autowired
//    public AmenitiesServiceImpl(AmenitiesDAO amenitiesDAO) {
//        this.amenitiesDAO = amenitiesDAO;
//    }
//
//    @Override
//    @Retryable(
//            maxAttempts = MAX_RETRY_ATTEMPTS,
//            backoff = @Backoff(delay = RETRY_DELAY_MS),
//            retryFor = DatabaseException.class,
//            noRetryFor = IllegalAnnotationsException.class
//    )
//    public void addHouseAmenities(AddHouseRequestDTO houseRequestDTO, House house) {
//        try {
//            Assert.notNull(houseRequestDTO, "House request DTO cannot be null");
//            Assert.notNull(house, "House cannot be null");
//
//            AddAmenitiesDTO amenitiesDTO = houseRequestDTO.getAmenities();
//            Amenities amenities = createAmenities(amenitiesDTO, house);
//            saveAmenities(amenities, "house");
//        } catch (IllegalArgumentException e) {
//            logger.error("Invalid input while adding house amenities: {}", e.getMessage());
//            throw new AmenitiesException("Invalid house amenities data: " + e.getMessage(), e);
//        }
//    }
//
//    @Override
//    @Retryable(
//            maxAttempts = MAX_RETRY_ATTEMPTS,
//            backoff = @Backoff(delay = RETRY_DELAY_MS),
//            retryFor = DatabaseException.class,
//            noRetryFor = IllegalAnnotationsException.class
//    )
//    public void updateAmenities(Amenities amenities, House house, Room room, Flat flat) {
//        Assert.notNull(amenities, "Amenities cannot be null");
//
//        if (house != null) {
//            amenities.setHouse(house);
//            amenities.setRoom(null);
//            amenities.setFlat(null);
//        } else if (room != null) {
//            amenities.setRoom(room);
//            amenities.setHouse(null);
//            amenities.setFlat(null);
//        } else if (flat != null) {
//            amenities.setFlat(flat);
//            amenities.setHouse(null);
//            amenities.setRoom(null);
//        } else {
//            throw new AmenitiesException("At least one property type (house, room, or flat) must be provided");
//        }
//
//        try {
//            amenitiesDAO.updateAmenities(amenities);
//        } catch (DatabaseException e) {
//            String errorMsg = "Failed to update amenities: " + e.getMessage();
//            logger.error(errorMsg, e);
//            throw new AmenitiesException(errorMsg, e);
//        } catch (IllegalStateException e) {
//            logger.error("Invalid state while updating amenities: {}", e.getMessage());
//            throw new AmenitiesException(e.getMessage(), e);
//        }
//    }
//    /**
//     * Adds amenities to a flat.
//     *
//     * @param flatRequestDTO the flat request DTO containing amenities information
//     * @param flat          the flat entity to which amenities will be added
//     * @throws AmenitiesException if an error occurs while adding amenities
//     */
//    @Override
//    @Retryable(
//            maxAttempts = MAX_RETRY_ATTEMPTS,
//            backoff = @Backoff(delay = RETRY_DELAY_MS),
//            retryFor = DatabaseException.class,
//            noRetryFor = IllegalAnnotationsException.class
//    )
//    public void addFlatAmenities(AddFlatRequestDTO flatRequestDTO, Flat flat) {
//        try {
//            Assert.notNull(flatRequestDTO, "Flat request DTO cannot be null");
//            Assert.notNull(flat, "Flat cannot be null");
//
//            AddAmenitiesDTO amenitiesDTO = flatRequestDTO.getAmenities();
//            Amenities amenities = createAmenities(amenitiesDTO, flat);
//            saveAmenities(amenities, "flat");
//        } catch (IllegalArgumentException e) {
//            logger.error("Invalid input while adding house amenities: {}", e.getMessage());
//            throw new AmenitiesException("Invalid house amenities data: " + e.getMessage(), e);
//        }
//    }
//
//    @Override
//    @Retryable(
//            maxAttempts = MAX_RETRY_ATTEMPTS,
//            backoff = @Backoff(delay = RETRY_DELAY_MS),
//            retryFor = DatabaseException.class,
//            noRetryFor = IllegalAnnotationsException.class
//    )
//    public void addRoomAmenities(AddRoomRequestDTO roomRequestDTO, Room room) {
//        try {
//            Assert.notNull(roomRequestDTO, "Room request DTO cannot be null");
//            Assert.notNull(room, "Room cannot be null");
//
//            AddAmenitiesDTO amenitiesDTO = roomRequestDTO.getAmenities();
//            Amenities amenities = createAmenities(amenitiesDTO, room);
//            saveAmenities(amenities, "room");
//        } catch (IllegalArgumentException e) {
//            logger.error("Invalid input while adding house amenities: {}", e.getMessage());
//            throw new AmenitiesException("Invalid house amenities data: " + e.getMessage(), e);
//        }
//    }
//
//    private Amenities createAmenities(AddAmenitiesDTO amenitiesDTO, Object property) {
//        Assert.notNull(amenitiesDTO, "Amenities DTO cannot be null");
//        Assert.notNull(property, "Property cannot be null");
//
//        if (property instanceof House) {
//            return new Amenities(
//                    (House) property,
//                    amenitiesDTO.isHasParking(),
//                    amenitiesDTO.isHasWifi(),
//                    amenitiesDTO.isHasSecurityStaff(),
//                    amenitiesDTO.isHasUnderGroundWaterTank(),
//                    amenitiesDTO.isHasTV(),
//                    amenitiesDTO.isHasCCTV(),
//                    amenitiesDTO.isHasAC(),
//                    amenitiesDTO.isHasFridge(),
//                    amenitiesDTO.isHasBalcony(),
//                    amenitiesDTO.isHasWater(),
//                    amenitiesDTO.isHasSolarWaterHeater(),
//                    amenitiesDTO.isHasFan(),
//                    amenitiesDTO.getFurnishingStatus()
//            );
//        } else if (property instanceof Flat) {
//            return new Amenities(
//                    (Flat) property,
//                    amenitiesDTO.isHasParking(),
//                    amenitiesDTO.isHasWifi(),
//                    amenitiesDTO.isHasSecurityStaff(),
//                    amenitiesDTO.isHasUnderGroundWaterTank(),
//                    amenitiesDTO.isHasTV(),
//                    amenitiesDTO.isHasCCTV(),
//                    amenitiesDTO.isHasAC(),
//                    amenitiesDTO.isHasFridge(),
//                    amenitiesDTO.isHasBalcony(),
//                    amenitiesDTO.isHasWater(),
//                    amenitiesDTO.isHasSolarWaterHeater(),
//                    amenitiesDTO.isHasFan(),
//                    amenitiesDTO.getFurnishingStatus()
//            );
//        } else if (property instanceof Room) {
//            return new Amenities(
//                    (Room) property,
//                    amenitiesDTO.isHasParking(),
//                    amenitiesDTO.isHasWifi(),
//                    amenitiesDTO.isHasSecurityStaff(),
//                    amenitiesDTO.isHasUnderGroundWaterTank(),
//                    amenitiesDTO.isHasTV(),
//                    amenitiesDTO.isHasCCTV(),
//                    amenitiesDTO.isHasAC(),
//                    amenitiesDTO.isHasFridge(),
//                    amenitiesDTO.isHasBalcony(),
//                    amenitiesDTO.isHasWater(),
//                    amenitiesDTO.isHasSolarWaterHeater(),
//                    amenitiesDTO.isHasFan()
//            );
//        }
//        throw new IllegalArgumentException("Unsupported property type: " + property.getClass().getSimpleName());
//    }
//
//    private void saveAmenities(Amenities amenities, String propertyType) {
//        try {
//            amenitiesDAO.addAmenities(amenities);
//        } catch (DatabaseException e) {
//            throw new AmenitiesException(
//                    String.format("Failed to add amenities for %s: %s", propertyType, e.getMessage()),
//                    e
//            );
//        }
//    }
//}