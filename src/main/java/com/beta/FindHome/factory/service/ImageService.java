package com.beta.FindHome.factory.service;

import com.beta.FindHome.factory.concrete.ImageFactory;
import com.beta.FindHome.factory.interfaces.ImageFactoryInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.awt.image.BufferedImage;
import java.util.UUID;

@Service
public class ImageService {

    private final ImageFactory imageFactory;

    @Value("${image.storage.channel}")
    private String storageChannel;

    @Autowired
    public ImageService(ImageFactory imageFactory) {
        this.imageFactory = imageFactory;
    }

    public String saveImage(MultipartFile file) {
        ImageFactoryInterface imageFactoryInterface = imageFactory.checkImageChannel(storageChannel);
        if (imageFactoryInterface != null) {
            return imageFactoryInterface.save(file);
        }
        throw new IllegalArgumentException("Invalid image factory interface");
    }

    public Boolean deleteImage(String fileToDelete) {
        ImageFactoryInterface imageFactoryInterface = imageFactory.checkImageChannel(storageChannel);
        if (imageFactoryInterface != null) {
            return imageFactoryInterface.delete(fileToDelete);
        }
        throw new IllegalArgumentException("Invalid image factory interface");
    }
}
