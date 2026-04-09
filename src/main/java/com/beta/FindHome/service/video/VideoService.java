package com.beta.FindHome.service.video;

import org.springframework.web.multipart.MultipartFile;

public interface VideoService {
    String saveVideo(MultipartFile file) throws Exception;
}