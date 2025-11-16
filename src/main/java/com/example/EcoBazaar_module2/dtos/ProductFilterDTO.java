package com.example.EcoBazaar_module2.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductFilterDTO {

    private String category;
    private Boolean ecoCertified;
    private BigDecimal maxCarbonImpact;
    private BigDecimal minEcoRating;
    private Boolean recyclable;
    private String sortBy; // price, carbon, rating
    private String sortOrder; // asc, desc

    // All manual constructors, getters, setters, and Builder class removed.
}