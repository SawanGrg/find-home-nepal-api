package com.beta.FindHome.service.common.assets;

import com.beta.FindHome.dto.common.assets.*;
import com.beta.FindHome.exception.AssetException;
import com.beta.FindHome.factory.interfaces.ImageFactoryInterface;
import com.beta.FindHome.model.*;
import com.beta.FindHome.repository.AssetsRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class AssetServiceImpl implements AssetService {

    private static final Logger logger = LoggerFactory.getLogger(AssetServiceImpl.class);

    private final AssetsRepository assetsRepository;
    private final ImageFactoryInterface imageFactory;

    @Autowired
    public AssetServiceImpl(AssetsRepository assetsRepository,
                            ImageFactoryInterface imageFactory) {
        this.assetsRepository = assetsRepository;
        this.imageFactory = imageFactory;
    }

    @Transactional
    public void addHouseAssets(AddHouseAssetRequestDTO dto) {
        saveAsset(dto.getHouse(), dto.getAssetType(), dto.getUrl(), "house");
    }

    @Transactional
    public void addFlatAssets(AddFlatAssetRequestDTO dto) {
        saveAsset(dto.getFlat(), dto.getAssetType(), dto.getUrl(), "flat");
    }

    @Transactional
    public void addRoomAssets(AddRoomAssetRequestDTO dto) {
        saveAsset(dto.getRoom(), dto.getAssetType(), dto.getUrl(), "room");
    }

    @Transactional
    public boolean deleteAsset(UUID assetId) {
        if (assetId == null) throw new AssetException("Asset ID cannot be null.");

        if (!assetsRepository.existsById(assetId)) {
            throw new AssetException("Asset not found with ID: " + assetId);
        }

        assetsRepository.deleteById(assetId);
        logger.info("Asset with ID: {} deleted successfully", assetId);
        return true;
    }

    public List<AddHouseAssetRequestDTO> createHouseAssetList(List<MultipartFile> images, House house) {
        List<AddHouseAssetRequestDTO> list = new ArrayList<>();
        for (MultipartFile image : images) {
            if (!image.isEmpty()) {
                AddHouseAssetRequestDTO dto = new AddHouseAssetRequestDTO();
                dto.setUrl(imageFactory.save(image));
                dto.setAssetType("IMAGE");
                dto.setHouse(house);
                list.add(dto);
            }
        }
        return list;
    }

    public List<AddFlatAssetRequestDTO> createFlatAssetList(List<MultipartFile> images, Flat flat) {
        List<AddFlatAssetRequestDTO> list = new ArrayList<>();
        for (MultipartFile image : images) {
            if (!image.isEmpty()) {
                AddFlatAssetRequestDTO dto = new AddFlatAssetRequestDTO();
                dto.setUrl(imageFactory.save(image));
                dto.setAssetType("IMAGE");
                dto.setFlat(flat);
                list.add(dto);
            }
        }
        return list;
    }

    public List<AddRoomAssetRequestDTO> createRoomAssetList(List<MultipartFile> images, Room room) {
        List<AddRoomAssetRequestDTO> list = new ArrayList<>();
        for (MultipartFile image : images) {
            if (!image.isEmpty()) {
                AddRoomAssetRequestDTO dto = new AddRoomAssetRequestDTO();
                dto.setUrl(imageFactory.save(image));
                dto.setAssetType("IMAGE");
                dto.setRoom(room);
                list.add(dto);
            }
        }
        return list;
    }

    // === PRIVATE HELPERS ===

    private void saveAsset(Property property, String assetType, String url, String context) {
        try {
            Assets asset = new Assets();
            asset.setProperty(property);
            asset.setAssetType(assetType);
            asset.setAssetURL(url);
            Assets saved = assetsRepository.save(asset);
            logger.debug("Created {} asset with ID: {}", context, saved.getId());
        } catch (DataIntegrityViolationException e) {
            throw new AssetException("Duplicate or invalid data for " + context + " asset", e);
        } catch (Exception e) {
            throw new AssetException("Could not create " + context + " asset", e);
        }
    }
}

//package com.beta.FindHome.service.common.assets;
//
//import com.beta.FindHome.dao.AssetsDAO;
//import com.beta.FindHome.dto.common.assets.*;
//import com.beta.FindHome.exception.AssetException;
//import com.beta.FindHome.factory.interfaces.ImageFactoryInterface;
//import com.beta.FindHome.factory.service.ImageService;
//import com.beta.FindHome.model.*;
//import jakarta.transaction.Transactional;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.dao.DataIntegrityViolationException;
//import org.springframework.stereotype.Service;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Optional;
//import java.util.UUID;
//
//@Service
//public class AssetServiceImpl implements AssetService {
//
//    private static final Logger logger = LoggerFactory.getLogger(AssetServiceImpl.class);
//
//    private final AssetsDAO assetsDAOImpl;
//    private final ImageFactoryInterface imageFactory;
//    private final ImageService imageService;
//
//    @Autowired
//    public AssetServiceImpl(AssetsDAO assetsDAOImpl,
//                            ImageFactoryInterface imageFactory,
//                            ImageService imageService) {
//        this.assetsDAOImpl = assetsDAOImpl;
//        this.imageFactory = imageFactory;
//        this.imageService = imageService;
//    }
//
//    @Override
//    @Transactional
//    public void addHouseAssets(AddHouseAssetRequestDTO assetRequestDTO) {
//        createAsset(
//                new Assets(assetRequestDTO.getHouse(), assetRequestDTO.getAssetType(), assetRequestDTO.getUrl()),
//                assetRequestDTO.getAssetType(),
//                "house");
//    }
//
//    @Override
//    public void addFlatAssets(AddFlatAssetRequestDTO assetRequestDTO) {
//        createAsset(
//                new Assets(assetRequestDTO.getFlat(), assetRequestDTO.getAssetType(), assetRequestDTO.getUrl()),
//                assetRequestDTO.getAssetType(),
//                "flat");
//    }
//
//    @Override
//    @Transactional
//    public void addRoomAssets(AddRoomAssetRequestDTO assetRequestDTO) {
//        createAsset(
//                new Assets(assetRequestDTO.getRoom(), assetRequestDTO.getAssetType(), assetRequestDTO.getUrl()),
//                assetRequestDTO.getAssetType()
//                , "room");
//    }
//
//    /**
//     * Common reusable asset creation logic with centralized exception handling
//     */
//    private void createAsset(Assets assets, String assetType, String context) {
//        try {
//            Assets persisted = assetsDAOImpl.createAsset(assets);
//            if (persisted.getId() == null) {
//                throw new AssetException("Failed to create " + context + " asset. Asset Type: " + assetType);
//            }
//            logger.debug("Successfully created {} asset with ID: {}", context, persisted.getId());
//
//        } catch (DataIntegrityViolationException e) {
//            logger.error("Data integrity violation while creating {} asset", context, e);
//            throw new AssetException("Duplicate or invalid data for " + context + " asset. Asset Type: " + assetType, e);
//        } catch (Exception e) {
//            logger.error("Unexpected error while creating {} asset", context, e);
//            throw new AssetException("Could not create " + context + " asset. Asset Type: " + assetType, e);
//        }
//    }
//
//    @Override
//    public List<AddHouseAssetRequestDTO> createHouseAssetList(List<MultipartFile> imageUrls, House house) {
//        List<AddHouseAssetRequestDTO> assetRequestDTOList = new ArrayList<>();
//        for (MultipartFile image : imageUrls) {
//            if (!image.isEmpty()) {
//                String savedImageUrl = imageFactory.save(image);
//                AddHouseAssetRequestDTO assetRequestDTO = new AddHouseAssetRequestDTO();
//                assetRequestDTO.setUrl(savedImageUrl);
//                assetRequestDTO.setAssetType("IMAGE");
//                assetRequestDTO.setHouse(house);
//                assetRequestDTOList.add(assetRequestDTO);
//            }
//        }
//        return assetRequestDTOList;
//    }
//
//    @Override
//    public List<AddFlatAssetRequestDTO> createFlatAssetList(List<MultipartFile> imageUrls, Flat flat) {
//        List<AddFlatAssetRequestDTO> assetRequestDTOList = new ArrayList<>();
//        for (MultipartFile image : imageUrls) {
//            if (!image.isEmpty()) {
//                String savedImageUrl = imageFactory.save(image);
//                AddFlatAssetRequestDTO assetRequestDTO = new AddFlatAssetRequestDTO();
//                assetRequestDTO.setUrl(savedImageUrl);
//                assetRequestDTO.setAssetType("IMAGE");
//                assetRequestDTO.setFlat(flat);
//                assetRequestDTOList.add(assetRequestDTO);
//            }
//        }
//        return assetRequestDTOList;
//    }
//
//    @Override
//    public List<AddRoomAssetRequestDTO> createRoomAssetList(List<MultipartFile> imageUrls, Room room) {
//        List<AddRoomAssetRequestDTO> assetRequestDTOList = new ArrayList<>();
//        for (MultipartFile image : imageUrls) {
//            if (!image.isEmpty()) {
//                String savedImageUrl = imageFactory.save(image);
//                AddRoomAssetRequestDTO assetRequestDTO = new AddRoomAssetRequestDTO();
//                assetRequestDTO.setUrl(savedImageUrl);
//                assetRequestDTO.setAssetType("IMAGE");
//                assetRequestDTO.setRoom(room);
//                assetRequestDTOList.add(assetRequestDTO);
//            }
//        }
//        return assetRequestDTOList;
//    }
//
//    @Override
//    @Transactional
//    public boolean deleteAsset(UUID assetId) {
//        if (assetId == null) {
//            throw new AssetException("Asset ID cannot be null.");
//        }
//
//        Optional<Assets> optionalAsset = assetsDAOImpl.getAssetById(assetId);
//        if (optionalAsset.isEmpty()) {
//            throw new AssetException("Asset not found with ID: " + assetId);
//        }
//
//        boolean isDeleted = assetsDAOImpl.deleteAssetById(assetId);
//        if (!isDeleted) {
//            throw new AssetException("Failed to delete asset with ID: " + assetId);
//        }
//
//        logger.info("Asset with ID: {} deleted successfully", assetId);
//        return true;
//    }
//}
