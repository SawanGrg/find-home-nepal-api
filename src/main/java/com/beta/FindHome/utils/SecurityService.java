package com.beta.FindHome.utils;

import com.beta.FindHome.repository.FlatRepository;
import com.beta.FindHome.repository.HouseRepository;
import com.beta.FindHome.repository.RoomRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class SecurityService {

    private final RoomRepository roomRepository;
    private final FlatRepository flatRepository;
    private final HouseRepository houseRepository;

    public SecurityService(RoomRepository roomRepository,
                           FlatRepository flatRepository,
                           HouseRepository houseRepository) {
        this.roomRepository = roomRepository;
        this.flatRepository = flatRepository;
        this.houseRepository = houseRepository;
    }

    @Transactional(readOnly = true)
    public boolean isRoomOwner(Authentication authentication, UUID roomId) {

        validateAuth(authentication);
        String username = authentication.getName();

        boolean isOwner = roomRepository
                .existsActiveByIdAndLandlordUsername(roomId, username);

        if (!isOwner) {
            throw new AccessDeniedException(
                    "User is not the owner of this room"
            );
        }

        return true;
    }

    @Transactional(readOnly = true)
    public boolean isFlatOwner(Authentication authentication, UUID flatId) {

        validateAuth(authentication);
        String username = authentication.getName();

        boolean isOwner = flatRepository
                .existsActiveByIdAndLandlordUsername(flatId, username);

        if (!isOwner) {
            throw new AccessDeniedException(
                    "User is not the owner of this flat"
            );
        }

        return true;
    }

    @Transactional(readOnly = true)
    public boolean isHouseOwner(Authentication authentication, UUID houseId) {

        validateAuth(authentication);
        String username = authentication.getName();

        boolean isOwner = houseRepository
                .existsActiveByIdAndLandlordUsername(houseId, username);

        if (!isOwner) {
            throw new AccessDeniedException(
                    "User is not the owner of this house"
            );
        }

        return true;
    }

    @Transactional(readOnly = true)
    public boolean isPropertyOwner(Authentication authentication,
                                   UUID propertyId,
                                   String propertyType) {

        validateAuth(authentication);

        return switch (propertyType.toLowerCase()) {
            case "room" -> isRoomOwner(authentication, propertyId);
            case "flat" -> isFlatOwner(authentication, propertyId);
            case "house" -> isHouseOwner(authentication, propertyId);
            default -> throw new IllegalArgumentException(
                    "Invalid property type: " + propertyType
            );
        };
    }

    private void validateAuth(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("User not authenticated");
        }
    }
}