package com.example.EcoBazaar_module2.dtos;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductRequestDTO {

    @NotBlank(message = "Product name is required")
    @Size(min = 2, max = 200)
    private String name;

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal price;

    @NotNull(message = "Seller ID is required")
    private Long sellerId;

    private String sellerName;

    private String category;
    private String subCategory;
    private String brand;
    // imageUrl removed, will be handled by MultipartFile

    @Min(value = 0)
    private Integer stockQuantity;

    @DecimalMin(value = "0.0")
    private BigDecimal weightKg;

    private String dimensions;
    private String manufacturingLocation;

    // Carbon data (optional - can be auto-calculated)
    @DecimalMin(value = "0.0")
    private BigDecimal carbonImpact;

    // Eco features
    @Builder.Default
    private Boolean ecoCertified = false;
    private String ecoCertificationDetails;
    @Builder.Default
    private Boolean recyclable = false;
    @Builder.Default
    private Boolean biodegradable = false;
    @Builder.Default
    private Boolean renewableEnergyUsed = false;
    @Builder.Default
    private Boolean shippingCarbonOffset = false;

    // All manual constructors, getters, setters, and Builder class removed.
}