package com.beta.FindHome.service.common.amenities;

import com.beta.FindHome.dto.common.amenities.AddAmenitiesDTO;
import com.beta.FindHome.dto.property.flat.AddFlatRequestDTO;
import com.beta.FindHome.dto.property.house.AddHouseRequestDTO;
import com.beta.FindHome.dto.property.room.AddRoomRequestDTO;
import com.beta.FindHome.exception.AmenitiesException;
import com.beta.FindHome.model.Amenities;
import com.beta.FindHome.model.Property;
import com.beta.FindHome.repository.AmenitiesRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AmenitiesServiceTest {

    static class TestProperty extends Property {
    }

    @Mock
    private AmenitiesRepository amenitiesRepository;

    @InjectMocks
    private AmenitiesServiceImpl amenitiesService;

    private Property property;
    private AddAmenitiesDTO amenitiesDTO;

    @BeforeEach
    void setup() {
        property = new TestProperty(); // ✅ use concrete subclass
        property.setId(java.util.UUID.randomUUID());

        amenitiesDTO = new AddAmenitiesDTO();
        amenitiesDTO.setHasParking(true);
        amenitiesDTO.setHasWifi(true);
        amenitiesDTO.setHasAC(true);
        amenitiesDTO.setHasFan(true);
    }

    // =====================================================
    // HOUSE TEST
    // =====================================================

    @Test
    void shouldAddHouseAmenitiesSuccessfully() {
        AddHouseRequestDTO dto = new AddHouseRequestDTO();
        dto.setAmenities(amenitiesDTO);

        when(amenitiesRepository.save(any(Amenities.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        amenitiesService.addHouseAmenities(dto, property);

        verify(amenitiesRepository, times(1)).save(any(Amenities.class));
    }

    // =====================================================
    // FLAT TEST
    // =====================================================

    @Test
    void shouldAddFlatAmenitiesSuccessfully() {
        AddFlatRequestDTO dto = new AddFlatRequestDTO();
        dto.setAmenities(amenitiesDTO);

        amenitiesService.addFlatAmenities(dto, property);

        verify(amenitiesRepository).save(any(Amenities.class));
    }

    // =====================================================
    // ROOM TEST
    // =====================================================

    @Test
    void shouldAddRoomAmenitiesSuccessfully() {
        AddRoomRequestDTO dto = new AddRoomRequestDTO();
        dto.setAmenities(amenitiesDTO);

        amenitiesService.addRoomAmenities(dto, property);

        verify(amenitiesRepository).save(any(Amenities.class));
    }

    // =====================================================
    // UPDATE TEST
    // =====================================================

    @Test
    void shouldUpdateAmenitiesSuccessfully() {
        Amenities amenities = new Amenities();

        amenitiesService.updateAmenities(amenities, property);

        assertEquals(property, amenities.getProperty());
        verify(amenitiesRepository).save(amenities);
    }

    // =====================================================
    // VALIDATION TESTS
    // =====================================================

    @Test
    void shouldThrowExceptionWhenHouseDTOIsNull() {
        assertThrows(IllegalArgumentException.class, () ->
                amenitiesService.addHouseAmenities(null, property));
    }

    @Test
    void shouldThrowExceptionWhenPropertyIsNull() {
        AddHouseRequestDTO dto = new AddHouseRequestDTO();
        dto.setAmenities(amenitiesDTO);

        assertThrows(IllegalArgumentException.class, () ->
                amenitiesService.addHouseAmenities(dto, null));
    }

    @Test
    void shouldThrowExceptionWhenAmenitiesDTOIsNull() {
        AddHouseRequestDTO dto = new AddHouseRequestDTO();
        dto.setAmenities(null);

        assertThrows(IllegalArgumentException.class, () ->
                amenitiesService.addHouseAmenities(dto, property));
    }

    // =====================================================
    // EXCEPTION HANDLING TEST
    // =====================================================

    @Test
    void shouldThrowAmenitiesExceptionWhenSaveFails() {
        AddHouseRequestDTO dto = new AddHouseRequestDTO();
        dto.setAmenities(amenitiesDTO);

        when(amenitiesRepository.save(any(Amenities.class)))
                .thenThrow(new RuntimeException("DB failure"));

        assertThrows(AmenitiesException.class, () ->
                amenitiesService.addHouseAmenities(dto, property));
    }
}