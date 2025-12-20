package com.example.EcoBazaar_module2.controller;

import com.example.EcoBazaar_module2.service.ImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

@RestController
@RequestMapping("/api/images")
public class ImageController {

    @Autowired
    private ImageService imageService;

    @Value("${file.upload-dir}")
    private String uploadDir;

    /**
     * Upload product image
     */
    @PostMapping("/products/{productId}")
    public ResponseEntity<?> uploadProductImage(
            @PathVariable Long productId,
            @RequestParam("file") MultipartFile file
    ) {
        try {
            String fileName = imageService.saveProductImage(file, productId);

            return ResponseEntity.ok(Map.of(
                    "message", "Product image uploaded successfully",
                    "fileName", fileName,
                    "url", "/api/images/" + fileName
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", e.getMessage()
            ));
        }
    }

    /**
     * Upload category icon
     */
    @PostMapping("/categories")
    public ResponseEntity<?> uploadCategoryIcon(
            @RequestParam("file") MultipartFile file,
            @RequestParam("categoryName") String categoryName
    ) {
        try {
            String fileName = imageService.saveCategoryIcon(file, categoryName);

            return ResponseEntity.ok(Map.of(
                    "message", "Category icon uploaded successfully",
                    "fileName", fileName,
                    "url", "/api/images/" + fileName
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", e.getMessage()
            ));
        }
    }

    /**
     * Serve image
     */
    @GetMapping("/{fileName:.+}")
    public ResponseEntity<Resource> getImage(@PathVariable String fileName) {
        try {
            Path path = Paths.get(uploadDir).resolve(fileName).normalize();
            Resource resource = new UrlResource(path.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                return ResponseEntity.notFound().build();
            }

            String contentType = "image/jpeg";
            if (fileName.endsWith(".png")) contentType = "image/png";
            else if (fileName.endsWith(".gif")) contentType = "image/gif";
            else if (fileName.endsWith(".webp")) contentType = "image/webp";

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(resource);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Delete image
     */
    @DeleteMapping("/{fileName}")
    public ResponseEntity<?> deleteImage(@PathVariable String fileName) {
        try {
            imageService.deleteImage(fileName);
            return ResponseEntity.ok(Map.of(
                    "message", "Image deleted successfully"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", e.getMessage()
            ));
        }
    }
}
