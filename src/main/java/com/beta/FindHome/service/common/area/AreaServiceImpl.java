// AreaServiceImpl.java
package com.beta.FindHome.service.common.area;

import com.beta.FindHome.dto.property.flat.AddFlatRequestDTO;
import com.beta.FindHome.dto.property.house.AddHouseRequestDTO;
import com.beta.FindHome.dto.property.room.AddRoomRequestDTO;
import com.beta.FindHome.exception.AreaException;
import com.beta.FindHome.model.Area;
import com.beta.FindHome.model.Property;
import com.beta.FindHome.repository.AreaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AreaServiceImpl implements AreaService {

    private static final Logger logger = LoggerFactory.getLogger(AreaServiceImpl.class);

    private final AreaRepository areaRepository;

    @Autowired
    public AreaServiceImpl(AreaRepository areaRepository) {
        this.areaRepository = areaRepository;
    }

    @Transactional
    public void addHouseArea(AddHouseRequestDTO dto, Property house) {
        validateInput(dto, house);
        saveArea(house, dto.getLength(), dto.getBreadth());
    }

    @Transactional
    public void addFlatArea(AddFlatRequestDTO dto, Property flat) {
        validateInput(dto, flat);
        saveArea(flat, dto.getLength(), dto.getBreadth());
    }

    @Transactional
    public void addRoomArea(AddRoomRequestDTO dto, Property room) {
        validateInput(dto, room);
        saveArea(room, dto.getLength(), dto.getBreadth());
    }

    @Transactional
    public void updateArea(Area area, Property property) {
        if (area == null) throw new IllegalArgumentException("Area cannot be null");
        if (property == null) throw new IllegalArgumentException("Property cannot be null");

        area.setProperty(property);
        areaRepository.save(area);
        logger.info("Updated area ID: {} for property ID: {}", area.getId(), property.getId());
    }

    // === PRIVATE HELPERS ===

    private void saveArea(Property property, Float length, Float breadth) {
        try {
            Area area = new Area();
            area.setProperty(property);
            area.setLength(length);
            area.setBreadth(breadth);
            if (length != null && breadth != null) {
                area.setTotalArea(length * breadth);
            }
            areaRepository.save(area);
            logger.info("Saved area for property ID: {}", property.getId());
        } catch (Exception e) {
            throw new AreaException("Failed to save area for property: " + property.getId(), e);
        }
    }

    private void validateInput(Object dto, Object property) {
        if (dto == null || property == null) {
            throw new IllegalArgumentException("DTO and property entity cannot be null");
        }
    }
}

//package com.beta.FindHome.service.common.area;
//
//import com.beta.FindHome.dao.AreaDAO;
//import com.beta.FindHome.dto.property.flat.AddFlatRequestDTO;
//import com.beta.FindHome.dto.property.house.AddHouseRequestDTO;
//import com.beta.FindHome.dto.property.room.AddRoomRequestDTO;
//import com.beta.FindHome.exception.AreaException;
//import com.beta.FindHome.exception.DataPersistenceException;
//import com.beta.FindHome.exception.EntityNotFoundException;
//import com.beta.FindHome.model.Area;
//import com.beta.FindHome.model.Flat;
//import com.beta.FindHome.model.House;
//import com.beta.FindHome.model.Room;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.dao.OptimisticLockingFailureException;
//import org.springframework.retry.annotation.Backoff;
//import org.springframework.retry.annotation.Recover;
//import org.springframework.retry.annotation.Retryable;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.UUID;
//
//@Service
//@Transactional
//public class AreaServiceImpl implements AreaService {
//
//    private static final Logger logger = LoggerFactory.getLogger(AreaServiceImpl.class);
//    private final AreaDAO areaDAO;
//
//    @Autowired
//    public AreaServiceImpl(AreaDAO areaDAO) {
//        this.areaDAO = areaDAO;
//    }
//
//    @Override
//    @Retryable(
//            value = {OptimisticLockingFailureException.class, DataPersistenceException.class},
//            maxAttempts = 3,
//            backoff = @Backoff(delay = 1000, multiplier = 2),
//            noRetryFor = {IllegalArgumentException.class, EntityNotFoundException.class}
//    )
//    public void addHouseArea(AddHouseRequestDTO houseDTO, House house) {
//        validateInput(houseDTO, house);
//
//        logger.debug("Adding area for house with ID: {}", house.getId());
//        Area area = new Area(house, houseDTO.getLength(), houseDTO.getBreadth());
//        persistArea(area, "house", house.getId());
//    }
//
//    @Override
//    @Retryable(
//            value = {OptimisticLockingFailureException.class, DataPersistenceException.class},
//            maxAttempts = 3,
//            backoff = @Backoff(delay = 1000, multiplier = 2),
//            noRetryFor = {IllegalArgumentException.class, EntityNotFoundException.class}
//    )
//    public void addFlatArea(AddFlatRequestDTO flatDTO, Flat flat) {
//        validateInput(flatDTO, flat);
//
//        logger.debug("Adding area for flat with ID: {}", flat.getId());
//        Area area = new Area(flat, flatDTO.getLength(), flatDTO.getBreadth());
//        persistArea(area, "flat", flat.getId());
//    }
//
//    @Override
//    @Retryable(
//            value = {OptimisticLockingFailureException.class, DataPersistenceException.class},
//            maxAttempts = 3,
//            backoff = @Backoff(delay = 1000, multiplier = 2),
//            noRetryFor = {IllegalArgumentException.class, EntityNotFoundException.class}
//    )
//    public void addRoomArea(AddRoomRequestDTO roomDTO, Room room) {
//        validateInput(roomDTO, room);
//
//        logger.debug("Adding area for room with ID: {}", room.getId());
//        Area area = new Area(room, roomDTO.getLength(), roomDTO.getBreadth());
//        persistArea(area, "room", room.getId());
//    }
//
//    @Override
//    @Retryable(
//            value = {OptimisticLockingFailureException.class, DataPersistenceException.class},
//            maxAttempts = 3,
//            backoff = @Backoff(delay = 1500, multiplier = 1.5),
//            noRetryFor = {IllegalArgumentException.class, EntityNotFoundException.class}
//    )
//    public void updateArea(Area area, House house, Room room, Flat flat) {
//        validateAreaUpdate(area, house, room, flat);
//
//        logger.debug("Updating area with ID: {}", area.getId());
//        configureAreaRelationships(area, house, room, flat);
//        areaDAO.updateArea(area);
//        logger.info("Successfully updated area with ID: {}", area.getId());
//    }
//
//    @Recover
//    public void recoverAddOperation(RuntimeException e, Object dto, Object property) {
//        String propertyType = property.getClass().getSimpleName().toLowerCase();
//        String errorMsg = String.format("Failed to add area for %s after retries: %s",
//                propertyType, e.getMessage());
//        logger.error(errorMsg, e);
//        throw new AreaException(errorMsg, e);
//    }
//
//    @Recover
//    public void recoverUpdateOperation(RuntimeException e, Area area, House house, Room room, Flat flat) {
//        String errorMsg = String.format("Failed to update area ID %s after retries: %s",
//                area.getId(), e.getMessage());
//        logger.error(errorMsg, e);
//        throw new AreaException(errorMsg, e);
//    }
//
//    private void persistArea(Area area, String propertyType, UUID propertyId) {
//        try {
//            areaDAO.saveOrUpdateArea(area);
//            logger.info("Successfully added area for {} with ID: {}", propertyType, propertyId);
//        } catch (DataPersistenceException e) {
//            throw new AreaException("Failed to persist area for " + propertyType, e);
//        }
//    }
//
//    private void validateInput(Object dto, Object property) {
//        if (dto == null || property == null) {
//            throw new IllegalArgumentException("DTO and property entity cannot be null");
//        }
//    }
//
//    private void validateAreaUpdate(Area area, House house, Room room, Flat flat) {
//        if (area == null) {
//            throw new IllegalArgumentException("Area cannot be null");
//        }
//        if (house == null && room == null && flat == null) {
//            throw new IllegalArgumentException("At least one property must be provided");
//        }
//    }
//
//    private void configureAreaRelationships(Area area, House house, Room room, Flat flat) {
//        if (house != null) {
//            area.setHouse(house);
//            area.setRoom(null);
//            area.setFlat(null);
//            logger.debug("Associating area with house ID: {}", house.getId());
//        } else if (room != null) {
//            area.setRoom(room);
//            area.setHouse(null);
//            area.setFlat(null);
//            logger.debug("Associating area with room ID: {}", room.getId());
//        } else {
//            area.setFlat(flat);
//            area.setHouse(null);
//            area.setRoom(null);
//            logger.debug("Associating area with flat ID: {}", flat.getId());
//        }
//    }
//}