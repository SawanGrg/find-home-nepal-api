package com.beta.FindHome.utils;

import com.beta.FindHome.dto.auth.RegisterUserRequestDTO;
import com.beta.FindHome.dto.common.amenities.UpdateAmenitiesDTO;
import com.beta.FindHome.dto.property.GetAllPropertyResponseDTO;
import com.beta.FindHome.dto.property.flat.AddFlatRequestDTO;
import com.beta.FindHome.dto.property.flat.UpdateFlatRequestDTO;
import com.beta.FindHome.dto.property.house.AddHouseRequestDTO;
import com.beta.FindHome.dto.property.house.UpdateHouseRequestDTO;
import com.beta.FindHome.dto.property.room.AddRoomRequestDTO;
import com.beta.FindHome.dto.property.room.UpdateRoomRequestDTO;
import com.beta.FindHome.dto.user.ProfileResponseDTO;
import com.beta.FindHome.dto.user.UserDetailsListResponseDTO;
import com.beta.FindHome.dto.user.UserDetailsRequestDTO;
import com.beta.FindHome.dto.user.owner.RegisterOwnerRequestDTO;
import com.beta.FindHome.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class MapperUtil {

    private final PasswordEncoder passwordEncoder;

    @Autowired
    public MapperUtil(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    // =====================================================================
    // USER MAPPERS
    // =====================================================================

    public Users createUserMapper(RegisterUserRequestDTO dto) {
        return Users.builder()
                .firstName(dto.getFirstName())
                .middleName(dto.getMiddleName())
                .lastName(dto.getLastName())
                .userName(dto.getUserName())
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .phoneNumber(dto.getPhoneNumber())
                .dob(dto.getDob())
                .userGender(dto.getUserGender())
                .maritalStatus(dto.getMaritalStatus())
                .roleStatus(dto.getRoleStatus())
                .isVerified(dto.getIsVerified())
                .build();
    }

    public Users createOwnerMapper(RegisterOwnerRequestDTO dto) {
        return Users.builder()
                .firstName(dto.getFirstName())
                .middleName(dto.getMiddleName())
                .lastName(dto.getLastName())
                .userName(dto.getUserName())
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .phoneNumber(dto.getPhoneNumber())
                .dob(dto.getDob())
                .userGender(dto.getUserGender())
                .maritalStatus(dto.getMaritalStatus())
                .roleStatus(dto.getRoleStatus())
                .isVerified(false)
                .build();
    }

    public Users updateUserMapper(Users user, UserDetailsRequestDTO dto) {
        if (dto.getFirstName() != null)        user.setFirstName(dto.getFirstName());
        if (dto.getMiddleName() != null)       user.setMiddleName(dto.getMiddleName());
        if (dto.getLastName() != null)         user.setLastName(dto.getLastName());
        if (dto.getEmail() != null)            user.setEmail(dto.getEmail());
        if (dto.getPhoneNumber() != null)      user.setPhoneNumber(dto.getPhoneNumber());
        if (dto.getDob() != null)              user.setDob(dto.getDob());
        if (dto.getUserGender() != null)       user.setUserGender(dto.getUserGender());
        if (dto.getMaritalStatus() != null)    user.setMaritalStatus(dto.getMaritalStatus());
        if (dto.getRoleStatus() != null)       user.setRoleStatus(dto.getRoleStatus());
        if (dto.getCitizenshipFront() != null) user.setCitizenshipFront(dto.getCitizenshipFront());
        if (dto.getCitizenshipBack() != null)  user.setCitizenshipBack(dto.getCitizenshipBack());
        return user;
    }

    public ProfileResponseDTO createUserProfileDTO(Users user) {
        ProfileResponseDTO dto = new ProfileResponseDTO();
        dto.setId(user.getId());
        dto.setFirstName(user.getFirstName());
        dto.setMiddleName(user.getMiddleName());
        dto.setLastName(user.getLastName());
        dto.setUserName(user.getUserName());
        dto.setEmail(user.getEmail());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setCitizenshipFront(user.getCitizenshipFront());
        dto.setCitizenshipBack(user.getCitizenshipBack());
        dto.setDob(user.getDob());
        dto.setUserGender(user.getUserGender());
        dto.setMaritalStatus(user.getMaritalStatus());
        dto.setRoleStatus(user.getRoleStatus());
        dto.setIsVerified(user.isVerified());
        dto.setCreatedAt(String.valueOf(user.getCreatedAt()));
        return dto;
    }

    // Kept for backward compat — delegates to createUserProfileDTO
    public ProfileResponseDTO createOwnerUserProfileDTO(Users user) {
        return createUserProfileDTO(user);
    }

    public UserDetailsListResponseDTO convertToUserDetailsListResponseDTO(Users user) {
        return UserDetailsListResponseDTO.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .middleName(user.getMiddleName())
                .lastName(user.getLastName())
                .userName(user.getUserName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .dob(user.getDob())
                .isVerified(user.isVerified())
                .createdAt(user.getCreatedAt())
                .build();
    }

    // =====================================================================
    // HOUSE MAPPERS
    // =====================================================================

    public House createHouseMapper(AddHouseRequestDTO dto, Users landlord) {
        House house = new House();
        // Base Property fields
        house.setLandlord(landlord);
        house.setPrice(dto.getHousePrice());
        house.setDescription(dto.getHouseDescription());
        house.setAddress(new Address(dto.getDistrict(), dto.getCity(), dto.getWard(), dto.getTole()));
        house.setRules(dto.getHouseRules());
        house.setAvailable(true);
        house.setVerified(false);
        house.setDeleted(false);
        // House-specific fields
        house.setFloors(dto.getHouseFloors());
        house.setBedRooms(dto.getBedRooms());
        house.setBathRooms(dto.getBathRooms());
        house.setKitchen(dto.getKitchen());
        house.setLivingRoom(dto.getLivingRoom());
        return house;
    }

    public House updateHouseMapper(UpdateHouseRequestDTO dto, House existing, Users landlord) {
        existing.setLandlord(landlord);
        // Base Property fields — use DTO field names as-is
        if (dto.getHousePrice() != null)       existing.setPrice(dto.getHousePrice());
        if (dto.getHouseDescription() != null) existing.setDescription(dto.getHouseDescription());
        if (dto.getHouseRules() != null)       existing.setRules(dto.getHouseRules());
        if (dto.getIsAvailable() != null)      existing.setAvailable(dto.getIsAvailable());
        if (dto.getIsVerified() != null)       existing.setVerified(dto.getIsVerified());
        // House-specific fields
        if (dto.getHouseFloors() != null)      existing.setFloors(dto.getHouseFloors());
        if (dto.getBedRooms() != null)         existing.setBedRooms(dto.getBedRooms());
        if (dto.getBathRooms() != null)        existing.setBathRooms(dto.getBathRooms());
        if (dto.getKitchen() != null)          existing.setKitchen(dto.getKitchen());
        if (dto.getLivingRoom() != null)       existing.setLivingRoom(dto.getLivingRoom());
        updateAddress(existing, dto.getDistrict(), dto.getCity(), dto.getWard(), dto.getTole());
        return existing;
    }

    // =====================================================================
    // FLAT MAPPERS
    // =====================================================================

    public Flat createFlatMapper(AddFlatRequestDTO dto, Users landlord) {
        Flat flat = new Flat();
        flat.setLandlord(landlord);
        flat.setPrice(dto.getFlatPrice());
        flat.setDescription(dto.getFlatDescription());
        flat.setAddress(new Address(dto.getDistrict(), dto.getCity(), dto.getWard(), dto.getTole()));
        flat.setRules(dto.getFlatRules());
        flat.setAvailable(true);
        flat.setVerified(false);
        flat.setDeleted(false);
        flat.setBedRooms(dto.getBedRooms());
        flat.setBathRooms(dto.getBathRooms());
        flat.setKitchen(dto.getKitchen());
        flat.setLivingRoom(dto.getLivingRoom());
        return flat;
    }

    public Flat updateFlatMapper(UpdateFlatRequestDTO dto, Flat existing, Users landlord) {
        existing.setLandlord(landlord);
        if (dto.getFlatPrice() != null)       existing.setPrice(dto.getFlatPrice());
        if (dto.getFlatDescription() != null) existing.setDescription(dto.getFlatDescription());
        if (dto.getFlatRules() != null)       existing.setRules(dto.getFlatRules());
        if (dto.getIsAvailable() != null)     existing.setAvailable(dto.getIsAvailable());
        if (dto.getIsVerified() != null)      existing.setVerified(dto.getIsVerified());
        if (dto.getBedRooms() != null)        existing.setBedRooms(dto.getBedRooms());
        if (dto.getBathRooms() != null)       existing.setBathRooms(dto.getBathRooms());
        if (dto.getKitchen() != null)         existing.setKitchen(dto.getKitchen());
        if (dto.getLivingRoom() != null)      existing.setLivingRoom(dto.getLivingRoom());
        updateAddress(existing, dto.getDistrict(), dto.getCity(), dto.getWard(), dto.getTole());
        return existing;
    }

    // =====================================================================
    // ROOM MAPPERS
    // =====================================================================

    public Room createRoomMapper(AddRoomRequestDTO dto, Users landlord) {
        Room room = new Room();
        room.setLandlord(landlord);
        room.setPrice(dto.getRoomPrice());
        room.setDescription(dto.getRoomDescription());
        room.setAddress(new Address(dto.getDistrict(), dto.getCity(), dto.getWard(), dto.getTole()));
        room.setRules(dto.getRoomRules());
        room.setAvailable(true);
        room.setVerified(false);
        room.setDeleted(false);
        return room;
    }

    public Room updateRoomMapper(UpdateRoomRequestDTO dto, Room existing, Users landlord) {
        existing.setLandlord(landlord);
        if (dto.getRoomPrice() != null)       existing.setPrice(dto.getRoomPrice());
        if (dto.getRoomDescription() != null) existing.setDescription(dto.getRoomDescription());
        if (dto.getRoomRules() != null)       existing.setRules(dto.getRoomRules());
        if (dto.getIsAvailable() != null)     existing.setAvailable(dto.getIsAvailable());
        if (dto.getIsVerified() != null)      existing.setVerified(dto.getIsVerified());
        updateAddress(existing, dto.getDistrict(), dto.getCity(), dto.getWard(), dto.getTole());
        return existing;
    }

    // =====================================================================
    // AMENITIES MAPPERS
    // Method names kept as-is for backward compat — all delegate to shared logic
    // =====================================================================

    public Amenities createHouseAmenitiesMapper(UpdateAmenitiesDTO dto, Amenities amenities, House house) {
        return applyAmenitiesUpdate(dto, amenities, house);
    }

    public Amenities createRoomAmenitiesMapper(UpdateAmenitiesDTO dto, Amenities amenities, Room room) {
        return applyAmenitiesUpdate(dto, amenities, room);
    }

    public Amenities createFlatAmenitiesMapper(UpdateAmenitiesDTO dto, Amenities amenities, Flat flat) {
        return applyAmenitiesUpdate(dto, amenities, flat);
    }

    // =====================================================================
    // AREA MAPPERS
    // Method names kept as-is for backward compat — all delegate to shared logic
    // =====================================================================

    public Area createHouseAreaMapper(UpdateHouseRequestDTO dto, Area area, House house) {
        return applyAreaUpdate(area, house, dto.getLength(), dto.getBreadth());
    }

    public Area createRoomAreaMapper(UpdateRoomRequestDTO dto, Area area, Room room) {
        return applyAreaUpdate(area, room, dto.getLength(), dto.getBreadth());
    }

    public Area createFlatAreaMapper(UpdateFlatRequestDTO dto, Area area, Flat flat) {
        return applyAreaUpdate(area, flat, dto.getLength(), dto.getBreadth());
    }

    // =====================================================================
    // PRIVATE HELPERS
    // =====================================================================

    private Amenities applyAmenitiesUpdate(UpdateAmenitiesDTO dto, Amenities amenities, Property property) {
        amenities.setProperty(property);
        if (dto.getHasParking() != null)              amenities.setHasParking(dto.getHasParking());
        if (dto.getHasWifi() != null)                 amenities.setHasWifi(dto.getHasWifi());
        if (dto.getHasSecurityStaff() != null)        amenities.setHasSecurityStaff(dto.getHasSecurityStaff());
        if (dto.getHasUnderGroundWaterTank() != null) amenities.setHasUnderGroundWaterTank(dto.getHasUnderGroundWaterTank());
        if (dto.getHasTV() != null)                   amenities.setHasTV(dto.getHasTV());
        if (dto.getHasCCTV() != null)                 amenities.setHasCCTV(dto.getHasCCTV());
        if (dto.getHasAC() != null)                   amenities.setHasAC(dto.getHasAC());
        if (dto.getHasFridge() != null)               amenities.setHasFridge(dto.getHasFridge());
        if (dto.getHasBalcony() != null)              amenities.setHasBalcony(dto.getHasBalcony());
        if (dto.getHasWater() != null)                amenities.setHasWater(dto.getHasWater());
        if (dto.getHasSolarWaterHeater() != null)     amenities.setHasSolarWaterHeater(dto.getHasSolarWaterHeater());
        if (dto.getHasFan() != null)                  amenities.setHasFan(dto.getHasFan());
        if (dto.getFurnishingStatus() != null)        amenities.setFurnishingStatus(dto.getFurnishingStatus());
        return amenities;
    }

    private Area applyAreaUpdate(Area area, Property property, Float length, Float breadth) {
        area.setProperty(property);
        if (length != null)  area.setLength(length);
        if (breadth != null) area.setBreadth(breadth);
        if (area.getLength() != null && area.getBreadth() != null) {
            area.setTotalArea(area.getLength() * area.getBreadth());
        }
        return area;
    }

    private void updateAddress(Property property, String district, String city, String ward, String tole) {
        Address address = property.getAddress();
        if (address == null) address = new Address();
        if (district != null) address.setDistrict(district);
        if (city != null)     address.setCity(city);
        if (ward != null)     address.setWard(ward);
        if (tole != null)     address.setTole(tole);
        property.setAddress(address);
    }
}