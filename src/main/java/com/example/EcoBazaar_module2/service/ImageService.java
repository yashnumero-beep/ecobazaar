package com.example.EcoBazaar_module2.service;

import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

@Service
public class ImageService {

    @Value("${file.upload-dir}")
    private String uploadDir;

    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png", "gif", "webp");
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    /**
     * Simplified: Save product image with organized naming
     * Format: product_{productId}_{timestamp}.{ext}
     */
    public String saveProductImage(MultipartFile file, Long productId) throws IOException {
        validateImage(file);

        String extension = FilenameUtils.getExtension(file.getOriginalFilename());
        if (extension == null || extension.isEmpty()) {
            extension = "jpg";
        }

        // Simple, predictable naming: product_ID_timestamp.ext
        String fileName = String.format("product_%d_%d.%s",
                productId,
                System.currentTimeMillis(),
                extension
        );

        Path uploadPath = Paths.get(uploadDir);
        Files.createDirectories(uploadPath);

        Path filePath = uploadPath.resolve(fileName);
        Files.copy(file.getInputStream(), filePath);

        return fileName;
    }

    /**
     * Save category icon
     */
    public String saveCategoryIcon(MultipartFile file, String categoryName) throws IOException {
        validateImage(file);

        String extension = FilenameUtils.getExtension(file.getOriginalFilename());
        String fileName = String.format("category_%s.%s",
                categoryName.toLowerCase().replaceAll("\\s+", "_"),
                extension
        );

        Path uploadPath = Paths.get(uploadDir);
        Files.createDirectories(uploadPath);

        Path filePath = uploadPath.resolve(fileName);
        Files.copy(file.getInputStream(), filePath);

        return fileName;
    }

    /**
     * Delete image by filename
     */
    public void deleteImage(String fileName) throws IOException {
        Path filePath = Paths.get(uploadDir).resolve(fileName);
        Files.deleteIfExists(filePath);
    }

    /**
     * Validate uploaded image
     */
    private void validateImage(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IOException("File is empty");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IOException("File size exceeds maximum limit of 5MB");
        }

        String extension = FilenameUtils.getExtension(file.getOriginalFilename());
        if (extension == null || !ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new IOException("Invalid file type. Allowed: " + ALLOWED_EXTENSIONS);
        }

        // Validate MIME type
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IOException("File must be an image");
        }
    }
}