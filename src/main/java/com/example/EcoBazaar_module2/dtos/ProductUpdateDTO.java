package com.example.EcoBazaar_module2.dtos;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

@Data
@NoArgsConstructor
public class ProductUpdateDTO {

    @Size(min = 2, max = 200)
    private String name;

    private String description;

    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal price;

    private String category;
    private String subCategory;
    private String brand;

    @Min(value = 0)
    private Integer stockQuantity;

    @DecimalMin(value = "0.0")
    private BigDecimal weightKg;

    private String dimensions;
    private String manufacturingLocation;

    @DecimalMin(value = "0.0")
    private BigDecimal carbonImpact;

    private Boolean ecoCertified;
    private String ecoCertificationDetails;
    private Boolean recyclable;
    private Boolean biodegradable;
    private Boolean renewableEnergyUsed;
    private Boolean shippingCarbonOffset;

    // All manual getters and setters removed.
}