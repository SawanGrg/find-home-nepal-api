package com.beta.FindHome.dto.common.area;

import java.util.UUID;

public record AreaDTO(
        UUID id,
        Float length,
        Float breadth,
        Float totalArea
) {}