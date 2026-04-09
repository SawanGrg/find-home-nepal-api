package com.beta.FindHome.service.house;

import com.beta.FindHome.exception.HouseException;
import com.beta.FindHome.model.House;
import com.beta.FindHome.repository.*;
import com.beta.FindHome.service.common.amenities.AmenitiesService;
import com.beta.FindHome.service.common.area.AreaService;
import com.beta.FindHome.service.common.assets.AssetService;
import com.beta.FindHome.service.property.PropertyService;
import com.beta.FindHome.service.video.VideoService;
import com.beta.FindHome.utils.MapperUtil;
import com.beta.FindHome.utils.RedisUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HouseServiceTest {

    @Mock private HouseRepository houseRepository;
    @Mock private AmenitiesRepository amenitiesRepository;
    @Mock private AreaRepository areaRepository;
    @Mock private UserRepository userRepository;
    @Mock private AssetService assetService;
    @Mock private VideoService videoService;
    @Mock private AmenitiesService amenitiesService;
    @Mock private AreaService areaService;
    @Mock private MapperUtil mapperUtil;
    @Mock private PropertyService propertyService;
    @Mock private RedisUtils redisUtils;

    @InjectMocks
    private HouseServiceImpl houseService;

    // =====================================================================
    // deleteHouse()
    // =====================================================================

    @Test
    @DisplayName("deleteHouse: should delete house and invalidate cache when house exists")
    void deleteHouse_shouldDeleteAndInvalidateCache_whenHouseExists() {

        // ARRANGE
        UUID houseId = UUID.randomUUID();
        when(houseRepository.existsById(houseId)).thenReturn(true);

        // ACT
        assertDoesNotThrow(() -> houseService.deleteHouse(houseId));

        // ASSERT
        verify(houseRepository, times(1)).deleteById(houseId);
        verify(redisUtils, times(1)).delete("property:" + houseId);
    }

    @Test
    @DisplayName("deleteHouse: should throw HouseException and not touch DB/cache when house not found")
    void deleteHouse_shouldThrowException_whenHouseNotFound() {

        // ARRANGE
        UUID houseId = UUID.randomUUID();
        when(houseRepository.existsById(houseId)).thenReturn(false);

        // ACT + ASSERT
        HouseException exception = assertThrows(
                HouseException.class,
                () -> houseService.deleteHouse(houseId)
        );

        assertEquals("House not found.", exception.getMessage());

        verify(houseRepository, never()).deleteById(any());
        verify(redisUtils, never()).delete(any());
    }

    // =====================================================================
    // getHouseById()
    // =====================================================================

    @Test
    @DisplayName("getHouseById: should return house when it exists")
    void getHouseById_shouldReturnHouse_whenHouseExists() {

        // ARRANGE
        UUID houseId = UUID.randomUUID();

        House mockHouse = new House();
        mockHouse.setId(houseId);

        when(houseRepository.findActiveWithDetailsById(houseId))
                .thenReturn(Optional.of(mockHouse));

        // ACT
        House result = houseService.getHouseById(houseId);

        // ASSERT
        assertNotNull(result);
        assertEquals(houseId, result.getId());

        verify(houseRepository, times(1))
                .findActiveWithDetailsById(houseId);
    }

    @Test
    @DisplayName("getHouseById: should throw HouseException when house not found")
    void getHouseById_shouldThrowException_whenHouseNotFound() {

        // ARRANGE
        UUID houseId = UUID.randomUUID();

        when(houseRepository.findActiveWithDetailsById(houseId))
                .thenReturn(Optional.empty());

        // ACT + ASSERT
        HouseException exception = assertThrows(
                HouseException.class,
                () -> houseService.getHouseById(houseId)
        );

        assertEquals("House not found.", exception.getMessage());
    }
}