// AssetService.java
package com.beta.FindHome.service.common.assets;

import com.beta.FindHome.dto.common.assets.AddFlatAssetRequestDTO;
import com.beta.FindHome.dto.common.assets.AddHouseAssetRequestDTO;
import com.beta.FindHome.dto.common.assets.AddRoomAssetRequestDTO;
import com.beta.FindHome.model.Flat;
import com.beta.FindHome.model.House;
import com.beta.FindHome.model.Room;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface AssetService {

    void addHouseAssets(AddHouseAssetRequestDTO dto);

    void addFlatAssets(AddFlatAssetRequestDTO dto);

    void addRoomAssets(AddRoomAssetRequestDTO dto);

    boolean deleteAsset(UUID assetId);

    List<AddHouseAssetRequestDTO> createHouseAssetList(List<MultipartFile> images, House house);

    List<AddFlatAssetRequestDTO> createFlatAssetList(List<MultipartFile> images, Flat flat);

    List<AddRoomAssetRequestDTO> createRoomAssetList(List<MultipartFile> images, Room room);
}