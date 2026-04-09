package com.beta.FindHome.service.property;

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
import com.beta.FindHome.utils.RedisUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PropertyServiceTest {

    @Mock private PropertyRepository propertyRepository;
    @Mock private HouseRepository houseRepository;
    @Mock private RoomRepository roomRepository;
    @Mock private FlatRepository flatRepository;
    @Mock private RedisUtils redisUtils;

    @InjectMocks
    private PropertyServiceImpl propertyService;

    // ============================================================
    // findLandLordIdByPropertyId()
    // ============================================================

    @Test
    @DisplayName("findLandLordIdByPropertyId: should return landlord when property exists")
    void findLandLordId_shouldReturnLandlord() {

        UUID id = UUID.randomUUID();

        Users user = new Users();
        user.setUserName("testUser");

        Flat property = new Flat();
        property.setLandlord(user);

        when(propertyRepository.findByIdWithLandlord(id))
                .thenReturn(Optional.of(property));

        Users result = propertyService.findLandLordIdByPropertyId(id);

        assertNotNull(result);
        assertEquals("testUser", result.getUserName());
    }

    @Test
    @DisplayName("findLandLordIdByPropertyId: should throw when not found")
    void findLandLordId_shouldThrow_whenNotFound() {

        UUID id = UUID.randomUUID();

        when(propertyRepository.findByIdWithLandlord(id))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> propertyService.findLandLordIdByPropertyId(id));
    }

    // ============================================================
    // getProperty() — REDIS CACHE + DB FALLBACK
    // ============================================================

    @Test
    @DisplayName("getProperty: should return from Redis cache if present")
    void getProperty_shouldReturnFromCache() {

        UUID id = UUID.randomUUID();

        GetSpecificPropertyResponseDTO cached = new GetSpecificPropertyResponseDTO();
        cached.setId(id);

        when(redisUtils.get("property:" + id, GetSpecificPropertyResponseDTO.class))
                .thenReturn(cached);

        GetSpecificPropertyResponseDTO result = propertyService.getProperty(id);

        assertNotNull(result);
        assertEquals(id, result.getId());

        // DB should NOT be called
        verify(propertyRepository, never()).findByIdWithDetails(any());
    }

    @Test
    @DisplayName("getProperty: should fetch from DB and cache result when not in Redis")
    void getProperty_shouldFetchFromDB_andCache() {

        UUID id = UUID.randomUUID();

        when(redisUtils.get(any(), eq(GetSpecificPropertyResponseDTO.class)))
                .thenReturn(null);

        // create concrete property
        Flat property = new Flat();
        property.setId(id);
        property.setPrice(BigDecimal.valueOf(10000));
        property.setDescription("Nice flat");

        Address address = new Address();
        address.setCity("Kathmandu");
        address.setDistrict("Kathmandu");
        address.setWard("1");
        address.setTole("TestTole");
        property.setAddress(address);

        Users user = new Users();
        user.setUserName("owner");
        property.setLandlord(user);

        when(propertyRepository.findByIdWithDetails(id))
                .thenReturn(Optional.of(property));

        GetSpecificPropertyResponseDTO result = propertyService.getProperty(id);

        assertNotNull(result);
        assertEquals("owner", result.getUserName());

        verify(redisUtils).save(eq("property:" + id), any());
    }

    @Test
    @DisplayName("getProperty: should throw when property not found")
    void getProperty_shouldThrow_whenNotFound() {

        UUID id = UUID.randomUUID();

        when(redisUtils.get(any(), any())).thenReturn(null);
        when(propertyRepository.findByIdWithDetails(id))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> propertyService.getProperty(id));
    }

}