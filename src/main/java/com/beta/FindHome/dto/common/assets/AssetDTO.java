package com.beta.FindHome.dto.common.assets;

import java.util.UUID;

public record AssetDTO(
        UUID id,
        String assetType,
        String assetURL
) {}
