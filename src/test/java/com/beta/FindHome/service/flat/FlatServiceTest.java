package com.beta.FindHome.service.flat;

import com.beta.FindHome.exception.FlatException;
import com.beta.FindHome.model.*;
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

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FlatServiceTest {

    // --- All 11 dependencies mocked ---
    @Mock private FlatRepository flatRepository;
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

    // --- Real service, mocks injected ---
    @InjectMocks
    private FlatServiceImpl flatService;

//    @Test
//    @DisplayName("deleteFlat: should delete flat and invalidate cache when flat exists")
//    void deleteFlat_shouldDeleteAndInvalidateCache_whenFlatExists() {
//
//        // ARRANGE
//        UUID flatId = UUID.randomUUID();
//
//        // tell mock: yes, this flat exists
//        when(flatRepository.existsById(flatId)).thenReturn(true);
//        when(redisUtils.delete("property:" + flatId)).thenReturn(true); // or whatever it returns
//
//        // doNothing is default for void methods, but being explicit is good practice
//        doNothing().when(flatRepository).deleteById(flatId);
//        doNothing().when(redisUtils).delete("property:" + flatId);
//
//        // ACT
//        assertDoesNotThrow(() -> flatService.deleteFlat(flatId));
//
//        // ASSERT — verify the right things were called
//        verify(flatRepository, times(1)).existsById(flatId);
//        verify(flatRepository, times(1)).deleteById(flatId);
//        verify(redisUtils, times(1)).delete("property:" + flatId);
//    }

    @Test
    @DisplayName("deleteFlat: should throw FlatException when flat does not exist")
    void deleteFlat_shouldThrowFlatException_whenFlatNotFound() {

        // ARRANGE
        UUID flatId = UUID.randomUUID();

        // tell mock: this flat does NOT exist
        when(flatRepository.existsById(flatId)).thenReturn(false);

        // ACT + ASSERT
        FlatException exception = assertThrows(
                FlatException.class,
                () -> flatService.deleteFlat(flatId)
        );

        assertEquals("Flat not found.", exception.getMessage());

        // deleteById must NEVER be called if flat doesn't exist
        verify(flatRepository, never()).deleteById(any());

        // cache must NOT be touched either
        verify(redisUtils, never()).delete(any());
    }
}