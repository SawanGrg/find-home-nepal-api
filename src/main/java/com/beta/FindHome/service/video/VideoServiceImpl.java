package com.beta.FindHome.service.video;

import com.beta.FindHome.exception.UserException;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

@Component
public class VideoServiceImpl implements VideoService {

    @Value("${video.directory}")
    private String videoDirectory;

    // Allowed video file extensions
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(
            ".mp4", ".mov", ".avi", ".wmv", ".flv", ".mkv", ".webm"
    );

    // Maximum file size (10MB)
    private static final long MAX_FILE_SIZE = 200 * 1024 * 1024;

    @PostConstruct
    public void init() {
        File directory = new File(videoDirectory);
        if (!directory.exists()) {
            directory.mkdirs();
        }
    }

    @Override
    public String saveVideo(MultipartFile file) throws UserException {
        // Validate file is not empty
        if (file.isEmpty()) {
            throw new UserException("Cannot upload an empty file");
        }

        // Validate file size
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new UserException("File size exceeds maximum limit of 200MB");
        }

        // Validate file extension
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            throw new UserException("Invalid file name");
        }

        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(fileExtension)) {
            throw new UserException("Invalid file type. Allowed formats: " +
                    String.join(", ", ALLOWED_EXTENSIONS));
        }

        // Validate content type
        if (!isVideoFile(file)) {
            throw new UserException("Invalid content type. Please upload a video file.");
        }

        try {
            // Generate unique filename
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
            String timestamp = now.format(formatter);
            String uniqueFileName = timestamp + "_" + originalFilename;

            // Save file
            Path filePath = Paths.get(videoDirectory, uniqueFileName);
            Files.write(filePath, file.getBytes());

            // Return accessible URL
            String filepath = "uploads/videos/" + uniqueFileName;
            return "https://nayaghar.ddns.net:7000/" + filepath.replace("\\", "/");
        } catch (IOException e) {
            throw new UserException("Could not store video file. Error: " + e.getMessage());
        }
    }

    private boolean isVideoFile(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType != null;
    }
}