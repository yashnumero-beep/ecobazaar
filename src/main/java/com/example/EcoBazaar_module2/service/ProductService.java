package com.example.EcoBazaar_module2.service;


import com.example.EcoBazaar_module2.dtos.ProductFilterDTO;
import com.example.EcoBazaar_module2.dtos.ProductRequestDTO;
import com.example.EcoBazaar_module2.dtos.ProductResponseDTO;
import com.example.EcoBazaar_module2.exceptions.ResourceNotFoundException;
import com.example.EcoBazaar_module2.model.Product;
import com.example.EcoBazaar_module2.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;
    private final CarbonCalculatorService carbonCalculatorService;
    private final FileStorageService fileStorageService;
    /**
     * Create a new product
     */
    @Transactional
    public ProductResponseDTO createProduct(ProductRequestDTO request, MultipartFile file) {
        log.info("Creating new product: {}", request.getName());

        String imageUrl = null;
        if (file != null && !file.isEmpty()) {
            imageUrl = fileStorageService.storeFile(file);
        } else {
            imageUrl = request.getImageUrl();
        }
        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .sellerId(request.getSellerId())
                .sellerName(request.getSellerName())
                .category(request.getCategory())
                .subCategory(request.getSubCategory())
                .brand(request.getBrand())
                .imageUrl(imageUrl)
                .stockQuantity(request.getStockQuantity())
                .weightKg(request.getWeightKg())
                .dimensions(request.getDimensions())
                .manufacturingLocation(request.getManufacturingLocation())
                .ecoCertified(request.getEcoCertified())
                .ecoCertificationDetails(request.getEcoCertificationDetails())
                .recyclable(request.getRecyclable())
                .biodegradable(request.getBiodegradable())
                .renewableEnergyUsed(request.getRenewableEnergyUsed())
                .shippingCarbonOffset(request.getShippingCarbonOffset())
                .active(true)
                .verified(false)
                .build();

        // Calculate or set carbon footprint
        if (request.getCarbonImpact() != null) {
            product.setCarbonImpact(request.getCarbonImpact());
            product.setCarbonCalculationMethod(Product.CarbonCalculationMethod.MANUAL_INPUT);
        } else {
            // Auto-calculate carbon footprint
            BigDecimal calculatedImpact = carbonCalculatorService.calculateCarbonFootprint(product);
            product.setCarbonImpact(calculatedImpact);
            product.setCarbonCalculationMethod(Product.CarbonCalculationMethod.API_CALCULATED);
        }

        // Calculate eco rating
        product.setEcoRating(calculateEcoRating(product));

        // Generate carbon breakdown
        product.setCarbonBreakdown(carbonCalculatorService.generateCarbonBreakdown(product));

        Product savedProduct = productRepository.save(product);
        log.info("Product created successfully with ID: {}", savedProduct.getId());

        return mapToResponseDTO(savedProduct);
    }

    /**
     * Update existing product
     */
    @Transactional
    public ProductResponseDTO updateProduct(Long id, ProductRequestDTO request,MultipartFile file) {
        log.info("Updating product with ID: {}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));


        // Update fields
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setCategory(request.getCategory());
        product.setSubCategory(request.getSubCategory());
        product.setBrand(request.getBrand());
        if (file != null && !file.isEmpty()) {
            String newImageUrl = fileStorageService.storeFile(file);
            product.setImageUrl(newImageUrl);
        } else if (Objects.nonNull(request.getImageUrl())) {
            product.setImageUrl(request.getImageUrl());
        }        product.setStockQuantity(request.getStockQuantity());
        product.setWeightKg(request.getWeightKg());
        product.setDimensions(request.getDimensions());
        product.setManufacturingLocation(request.getManufacturingLocation());
        product.setEcoCertified(request.getEcoCertified());
        product.setEcoCertificationDetails(request.getEcoCertificationDetails());
        product.setRecyclable(request.getRecyclable());
        product.setBiodegradable(request.getBiodegradable());
        product.setRenewableEnergyUsed(request.getRenewableEnergyUsed());
        product.setShippingCarbonOffset(request.getShippingCarbonOffset());

        // Update carbon impact if provided
        if (request.getCarbonImpact() != null) {
            product.setCarbonImpact(request.getCarbonImpact());
            product.setCarbonCalculationMethod(Product.CarbonCalculationMethod.MANUAL_INPUT);
        }

        // Recalculate eco rating
        product.setEcoRating(calculateEcoRating(product));
        product.setCarbonBreakdown(carbonCalculatorService.generateCarbonBreakdown(product));

        Product updatedProduct = productRepository.save(product);
        log.info("Product updated successfully");

        return mapToResponseDTO(updatedProduct);
    }

    /**
     * Get product by ID
     */
    public ProductResponseDTO getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));
        return mapToResponseDTO(product);
    }

    /**
     * Get all active products
     */
    public List<ProductResponseDTO> getAllProducts() {
        return productRepository.findByActiveTrue()
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Search products by keyword
     */
    public List<ProductResponseDTO> searchProducts(String keyword) {
        return productRepository.searchByKeyword(keyword)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Filter products with advanced criteria
     */
    public List<ProductResponseDTO> filterProducts(ProductFilterDTO filter) {
        return productRepository.findWithFilters(
                        filter.getCategory(),
                        filter.getEcoCertified(),
                        filter.getMaxCarbonImpact(),
                        filter.getMinEcoRating(),
                        filter.getRecyclable()
                ).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get low carbon products
     */
    public List<ProductResponseDTO> getLowCarbonProducts(BigDecimal maxImpact) {
        return productRepository.findLowCarbonProducts(maxImpact)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get eco-certified products
     */
    public List<ProductResponseDTO> getEcoCertifiedProducts() {
        return productRepository.findByEcoCertifiedTrueAndActiveTrue()
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get eco-friendly alternatives for a product
     */
    public List<ProductResponseDTO> getEcoAlternatives(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        return productRepository.findEcoAlternativesInCategory(product.getCategory(), productId)
                .stream()
                .limit(5)
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Delete product (soft delete)
     */
    @Transactional
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));
        product.setActive(false);
        productRepository.save(product);
        log.info("Product soft deleted with ID: {}", id);
    }

    /**
     * Calculate eco rating based on multiple factors
     */
    private BigDecimal calculateEcoRating(Product product) {
        double rating = 5.0;

        // Carbon impact penalty
        double carbonImpact = product.getCarbonImpact().doubleValue();
        if (carbonImpact > 20) rating -= 2.0;
        else if (carbonImpact > 10) rating -= 1.5;
        else if (carbonImpact > 5) rating -= 1.0;
        else if (carbonImpact > 1) rating -= 0.5;

        // Bonuses for eco-friendly features
        if (Boolean.TRUE.equals(product.getEcoCertified())) rating += 0.5;
        if (Boolean.TRUE.equals(product.getRecyclable())) rating += 0.3;
        if (Boolean.TRUE.equals(product.getBiodegradable())) rating += 0.3;
        if (Boolean.TRUE.equals(product.getRenewableEnergyUsed())) rating += 0.4;
        if (Boolean.TRUE.equals(product.getShippingCarbonOffset())) rating += 0.2;

        // Cap at 5.0
        rating = Math.min(rating, 5.0);
        rating = Math.max(rating, 0.0);

        return BigDecimal.valueOf(rating).setScale(1, RoundingMode.HALF_UP);
    }

    /**
     * Map entity to DTO
     */
    private ProductResponseDTO mapToResponseDTO(Product product) {
        return ProductResponseDTO.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .sellerId(product.getSellerId())
                .sellerName(product.getSellerName())
                .category(product.getCategory())
                .subCategory(product.getSubCategory())
                .brand(product.getBrand())
                .imageUrl(product.getImageUrl())
                .stockQuantity(product.getStockQuantity())
                .weightKg(product.getWeightKg())
                .dimensions(product.getDimensions())
                .manufacturingLocation(product.getManufacturingLocation())
                .carbonImpact(product.getCarbonImpact())
                .carbonCalculationMethod(product.getCarbonCalculationMethod().name())
                .carbonBreakdown(product.getCarbonBreakdown())
                .ecoCertified(product.getEcoCertified())
                .ecoCertificationDetails(product.getEcoCertificationDetails())
                .ecoRating(product.getEcoRating())
                .ecoLabel(product.getEcoLabel())
                .ecoBadgeColor(product.getEcoBadgeColor())
                .recyclable(product.getRecyclable())
                .biodegradable(product.getBiodegradable())
                .renewableEnergyUsed(product.getRenewableEnergyUsed())
                .shippingCarbonOffset(product.getShippingCarbonOffset())
                .active(product.getActive())
                .verified(product.getVerified())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}