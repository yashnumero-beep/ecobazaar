package com.example.EcoBazaar_module2.controller;

import com.example.EcoBazaar_module2.model.Product;
import com.example.EcoBazaar_module2.model.ProductCarbonData;
import com.example.EcoBazaar_module2.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAllProducts() {
        List<Product> products = productService.getAllVerifiedProducts();
        return ResponseEntity.ok(products.stream()
                .map(this::toProductDTO)
                .collect(Collectors.toList()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getProduct(@PathVariable Long id) {
        Product product = productService.getProductById(id);
        return ResponseEntity.ok(toProductDTO(product));
    }

    @PostMapping("/seller/add")
    public ResponseEntity<?> addProduct(@RequestBody Map<String, Object> request) {
        try {
            Long sellerId = Long.valueOf(request.get("sellerId").toString());
            String name = request.get("name").toString();
            String description = request.get("description").toString();
            Double price = Double.valueOf(request.get("price").toString());
            String category = request.get("category").toString();
            String imageUrl = request.getOrDefault("imageUrl", "").toString();

            ProductCarbonData carbonData = new ProductCarbonData();
            carbonData.setManufacturing(Double.valueOf(request.getOrDefault("manufacturing", 0.0).toString()));
            carbonData.setTransportation(Double.valueOf(request.getOrDefault("transportation", 0.0).toString()));
            carbonData.setPackaging(Double.valueOf(request.getOrDefault("packaging", 0.0).toString()));
            carbonData.setUsage(Double.valueOf(request.getOrDefault("usage", 0.0).toString()));
            carbonData.setDisposal(Double.valueOf(request.getOrDefault("disposal", 0.0).toString()));

            Product product = productService.createProduct(sellerId, name, description, price,
                    category, imageUrl, carbonData);

            return ResponseEntity.ok(toProductDTO(product));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/seller/{sellerId}")
    public ResponseEntity<List<Map<String, Object>>> getSellerProducts(@PathVariable Long sellerId) {
        List<Product> products = productService.getSellerProducts(sellerId);
        return ResponseEntity.ok(products.stream()
                .map(this::toProductDTO)
                .collect(Collectors.toList()));
    }

    @GetMapping("/admin/pending")
    public ResponseEntity<List<Map<String, Object>>> getPendingProducts() {
        List<Product> products = productService.getPendingProducts();
        return ResponseEntity.ok(products.stream()
                .map(this::toProductDTO)
                .collect(Collectors.toList()));
    }

    @PutMapping("/admin/verify/{id}")
    public ResponseEntity<?> verifyProduct(@PathVariable Long id, @RequestParam Long adminId) {
        try {
            productService.verifyProduct(adminId, id);
            return ResponseEntity.ok(Map.of("message", "Product verified"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    private Map<String, Object> toProductDTO(Product product) {
        Map<String, Object> dto = new HashMap<>();
        dto.put("id", product.getId());
        dto.put("name", product.getName());
        dto.put("description", product.getDescription());
        dto.put("price", product.getPrice());
        dto.put("imageUrl", product.getImageUrl());
        dto.put("category", product.getCategory());
        dto.put("carbonFootprint", product.getTotalCarbonFootprint());
        dto.put("ecoRating", product.getEcoRating());
        dto.put("verified", product.isVerified());
        dto.put("sellerId", product.getSeller().getId());
        dto.put("sellerName", product.getSeller().getFullName());

        if (product.getCarbonData() != null) {
            Map<String, Double> breakdown = new HashMap<>();
            breakdown.put("manufacturing", product.getCarbonData().getManufacturing());
            breakdown.put("transportation", product.getCarbonData().getTransportation());
            breakdown.put("packaging", product.getCarbonData().getPackaging());
            breakdown.put("usage", product.getCarbonData().getUsage());
            breakdown.put("disposal", product.getCarbonData().getDisposal());
            dto.put("carbonBreakdown", breakdown);
        }

        return dto;
    }
}