package com.beta.FindHome.service.common.area;

import com.beta.FindHome.dto.property.house.AddHouseRequestDTO;
import com.beta.FindHome.exception.AreaException;
import com.beta.FindHome.model.Area;
import com.beta.FindHome.model.Property;
import com.beta.FindHome.repository.AreaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AreaServiceTest {

    @Mock
    private AreaRepository areaRepository;

    @InjectMocks
    private AreaServiceImpl areaService;

    private Property property;
    private AddHouseRequestDTO dto;

    // ✅ Setup using concrete subclass
    @BeforeEach
    void setup() {
        property = new TestProperty();
        property.setId(UUID.randomUUID());

        dto = new AddHouseRequestDTO();
        dto.setLength(10f);
        dto.setBreadth(5f);
    }

    // ✅ Concrete subclass for abstract Property
    static class TestProperty extends Property {}

    @Test
    @DisplayName("addHouseArea: should throw exception when dto is null")
    void addHouseArea_shouldThrowException_whenDtoIsNull() {

        assertThrows(IllegalArgumentException.class,
                () -> areaService.addHouseArea(null, property));

        verify(areaRepository, never()).save(any());
    }

    // =====================================================================
    // updateArea()
    // =====================================================================

    @Test
    @DisplayName("updateArea: should update area successfully")
    void updateArea_shouldUpdateSuccessfully() {

        Area area = new Area();

        areaService.updateArea(area, property);

        assertEquals(property, area.getProperty());
        verify(areaRepository).save(area);
    }

    @Test
    @DisplayName("updateArea: should throw exception when area is null")
    void updateArea_shouldThrowException_whenAreaIsNull() {

        assertThrows(IllegalArgumentException.class,
                () -> areaService.updateArea(null, property));

        verify(areaRepository, never()).save(any());
    }

    // =====================================================================
    // Exception handling
    // =====================================================================

    @Test
    @DisplayName("addHouseArea: should wrap exception into AreaException when DB fails")
    void addHouseArea_shouldThrowAreaException_whenSaveFails() {

        when(areaRepository.save(any()))
                .thenThrow(new RuntimeException("DB failure"));

        AreaException ex = assertThrows(
                AreaException.class,
                () -> areaService.addHouseArea(dto, property)
        );

        assertTrue(ex.getMessage().contains("Failed to save area"));
    }
}