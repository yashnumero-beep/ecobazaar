package com.example.EcoBazaar_module2.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Product name is required")
    @Size(min = 2, max = 200)
    @Column(nullable = false)
    private String name;

    @NotBlank(message = "Product description is required")
    @Column(columnDefinition = "TEXT")
    private String description;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false)
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @NotNull(message = "Seller ID is required")
    @Column(name = "seller_id", nullable = false)
    private Long sellerId;

    @Column(name = "seller_name")
    private String sellerName;

    // Carbon Footprint Data
    @NotNull(message = "Carbon impact is required")
    @DecimalMin(value = "0.0")
    @Column(name = "carbon_impact", nullable = false, precision = 10, scale = 3)
    private BigDecimal carbonImpact;

    @Column(name = "carbon_calculation_method")
    @Enumerated(EnumType.STRING)
    private CarbonCalculationMethod carbonCalculationMethod;

    @Column(name = "carbon_breakdown", columnDefinition = "TEXT")
    private String carbonBreakdown;

    @Builder.Default
    @Column(name = "eco_certified")
    private Boolean ecoCertified = false;

    @Column(name = "eco_certification_details")
    private String ecoCertificationDetails;

    @Column(name = "eco_rating")
    @DecimalMin(value = "0.0")
    @DecimalMax(value = "5.0")
    private BigDecimal ecoRating;

    // Product Details
    @Column(name = "category")
    private String category;

    @Column(name = "sub_category")
    private String subCategory;

    @Column(name = "brand")
    private String brand;

    // CHANGED: Store image URL instead of binary data
    @Column(name = "image_url", columnDefinition = "TEXT")
    private String imageUrl;

    @Builder.Default
    @Column(name = "stock_quantity")
    private Integer stockQuantity = 0;

    @Column(name = "weight_kg", precision = 10, scale = 3)
    private BigDecimal weightKg;

    @Column(name = "dimensions")
    private String dimensions;

    @Column(name = "manufacturing_location")
    private String manufacturingLocation;

    @Builder.Default
    @Column(name = "shipping_carbon_offset")
    private Boolean shippingCarbonOffset = false;

    @Builder.Default
    @Column(name = "recyclable")
    private Boolean recyclable = false;

    @Builder.Default
    @Column(name = "biodegradable")
    private Boolean biodegradable = false;

    @Builder.Default
    @Column(name = "renewable_energy_used")
    private Boolean renewableEnergyUsed = false;

    // Status
    @Builder.Default
    @Column(name = "active")
    private Boolean active = true;

    @Builder.Default
    @Column(name = "verified")
    private Boolean verified = false;

    @Column(name = "verification_date")
    private LocalDateTime verificationDate;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Enums
    public enum CarbonCalculationMethod {
        MANUAL_INPUT,
        API_CALCULATED,
        LCA_CERTIFIED,
        ESTIMATED
    }

    // Helper methods
    @Transient
    public String getEcoLabel() {
        if (carbonImpact == null) return "UNKNOWN";
        double impact = carbonImpact.doubleValue();
        if (impact < 1.0) return "EXCELLENT";
        if (impact < 5.0) return "GOOD";
        if (impact < 10.0) return "MODERATE";
        if (impact < 20.0) return "POOR";
        return "HIGH_IMPACT";
    }

    @Transient
    public String getEcoBadgeColor() {
        String label = getEcoLabel();
        switch (label) {
            case "EXCELLENT": return "#059669";
            case "GOOD": return "#10b981";
            case "MODERATE": return "#f59e0b";
            case "POOR": return "#f97316";
            case "HIGH_IMPACT": return "#ef4444";
            default: return "#6b7280";
        }
    }
}