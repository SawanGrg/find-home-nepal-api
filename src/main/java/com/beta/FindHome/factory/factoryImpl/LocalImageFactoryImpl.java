package com.beta.FindHome.factory.factoryImpl;

import com.beta.FindHome.exception.FileProcessingException;
import com.beta.FindHome.factory.interfaces.ImageFactoryInterface;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.concurrent.*;

@Service("local")
public class LocalImageFactoryImpl implements ImageFactoryInterface {

    private static final Logger logger = LoggerFactory.getLogger(LocalImageFactoryImpl.class);
    private static final int BUFFER_SIZE = 8192; // 8KB buffer size
    private static final int CORE_POOL_SIZE = Runtime.getRuntime().availableProcessors();
    private static final int MAX_POOL_SIZE = CORE_POOL_SIZE * 4;
    private static final long KEEP_ALIVE_TIME = 60L;
    private static final long UPLOAD_TIMEOUT = 30L;
    private static final long DELETE_TIMEOUT = 10L;

    private final Path storagePath;
    private final ThreadPoolExecutor executorService;

    public LocalImageFactoryImpl() {
        this.storagePath = Paths.get("uploads/images").toAbsolutePath().normalize();
        this.executorService = new ThreadPoolExecutor(
                CORE_POOL_SIZE,
                MAX_POOL_SIZE,
                KEEP_ALIVE_TIME,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(1000),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
        createDirectoryIfNotExists();
    }

    @Override
    public String save(MultipartFile file) throws FileProcessingException {
        if (file == null || file.isEmpty()) {
            throw new FileProcessingException("Cannot process empty or null file");
        }

        String filename = generateUniqueFilename(file.getOriginalFilename());
        Path destination = storagePath.resolve(filename);

        Future<Boolean> uploadFuture = executorService.submit(() -> {
            try (BufferedInputStream bis = new BufferedInputStream(file.getInputStream(), BUFFER_SIZE);
                 BufferedOutputStream bos = new BufferedOutputStream(
                         new FileOutputStream(destination.toFile()), BUFFER_SIZE)) {

                byte[] buffer = new byte[BUFFER_SIZE];
                int bytesRead;

                while ((bytesRead = bis.read(buffer)) != -1) {
                    bos.write(buffer, 0, bytesRead);
                }
                bos.flush();
                logger.debug("File saved successfully: {}", destination);
                return true;
            } catch (IOException e) {
                logger.error("File save failed for {}: {}", filename, e.getMessage());
                throw new CompletionException("File save operation failed", e);
            }
        });

        try {
            Boolean success = uploadFuture.get(UPLOAD_TIMEOUT, TimeUnit.SECONDS);
            if (success) {
                return "https://nayaghar.ddns.net:7000/uploads/images/" + filename;
            }
            throw new FileProcessingException("File upload failed without exception");
        } catch (InterruptedException e) {
            uploadFuture.cancel(true);
            Thread.currentThread().interrupt();
            throw new FileProcessingException("Upload interrupted", e);
        } catch (ExecutionException e) {
            throw new FileProcessingException("Upload failed: " + e.getCause().getMessage(), e.getCause());
        } catch (TimeoutException e) {
            uploadFuture.cancel(true);
            throw new FileProcessingException("Upload timed out after " + UPLOAD_TIMEOUT + " seconds", e);
        }
    }

    @Override
    public Boolean delete(String fileToDelete) throws FileProcessingException {
        String filename = extractFilenameFromUrl(fileToDelete);
        Path filePath = storagePath.resolve(filename);

        if (!Files.exists(filePath)) {
            logger.warn("File does not exist: {}", filePath);
            return false;
        }

        Future<Boolean> deleteFuture = executorService.submit(() -> {
            try {
                Files.delete(filePath);
                logger.debug("File deleted successfully: {}", filePath);
                return true;
            } catch (IOException e) {
                logger.error("File deletion failed for {}: {}", filename, e.getMessage());
                throw new CompletionException("File deletion failed", e);
            }
        });

        try {
            return deleteFuture.get(DELETE_TIMEOUT, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            deleteFuture.cancel(true);
            Thread.currentThread().interrupt();
            throw new FileProcessingException("Delete operation interrupted", e);
        } catch (ExecutionException e) {
            throw new FileProcessingException("Delete failed: " + e.getCause().getMessage(), e.getCause());
        } catch (TimeoutException e) {
            deleteFuture.cancel(true);
            throw new FileProcessingException("Delete operation timed out", e);
        }
    }

    private void createDirectoryIfNotExists() throws FileProcessingException {
        if (!Files.exists(storagePath)) {
            try {
                Files.createDirectories(storagePath);
                logger.info("Directory created: {}", storagePath);
            } catch (IOException e) {
                String errorMsg = "Failed to create storage directory: " + storagePath;
                logger.error(errorMsg);
                throw new FileProcessingException(errorMsg, e);
            }
        }
    }

    private String generateUniqueFilename(String originalFilename) throws FileProcessingException {
        try {
            String extension = originalFilename.substring(originalFilename.lastIndexOf('.'));
            return UUID.randomUUID().toString() + extension;
        } catch (Exception e) {
            throw new FileProcessingException("Invalid filename format: " + originalFilename, e);
        }
    }

    private String extractFilenameFromUrl(String url) throws FileProcessingException {
        try {
            return url.substring(url.lastIndexOf('/') + 1);
        } catch (Exception e) {
            throw new FileProcessingException("Invalid URL format: " + url, e);
        }
    }

    @PreDestroy
    public void shutdown() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}