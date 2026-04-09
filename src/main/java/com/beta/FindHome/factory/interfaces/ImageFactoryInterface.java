package com.beta.FindHome.factory.interfaces;

import org.springframework.web.multipart.MultipartFile;

import java.awt.image.BufferedImage;
import java.util.UUID;

public interface ImageFactoryInterface {

    String save(MultipartFile file);
    Boolean delete(String fileToDelete);

}
