package com.example.EcoBazaar_module2.dtos;

import java.math.BigDecimal;

public class ProductFilterDTO {

    private String category;
    private Boolean ecoCertified;
    private BigDecimal maxCarbonImpact;
    private BigDecimal minEcoRating;
    private Boolean recyclable;
    private String sortBy; // price, carbon, rating
    private String sortOrder; // asc, desc

    public ProductFilterDTO() {
    }

    public ProductFilterDTO(String category, Boolean ecoCertified, BigDecimal maxCarbonImpact, BigDecimal minEcoRating, Boolean recyclable, String sortBy, String sortOrder) {
        this.category = category;
        this.ecoCertified = ecoCertified;
        this.maxCarbonImpact = maxCarbonImpact;
        this.minEcoRating = minEcoRating;
        this.recyclable = recyclable;
        this.sortBy = sortBy;
        this.sortOrder = sortOrder;
    }

    // Getters and Setters
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public Boolean getEcoCertified() { return ecoCertified; }
    public void setEcoCertified(Boolean ecoCertified) { this.ecoCertified = ecoCertified; }

    public BigDecimal getMaxCarbonImpact() { return maxCarbonImpact; }
    public void setMaxCarbonImpact(BigDecimal maxCarbonImpact) { this.maxCarbonImpact = maxCarbonImpact; }

    public BigDecimal getMinEcoRating() { return minEcoRating; }
    public void setMinEcoRating(BigDecimal minEcoRating) { this.minEcoRating = minEcoRating; }

    public Boolean getRecyclable() { return recyclable; }
    public void setRecyclable(Boolean recyclable) { this.recyclable = recyclable; }

    public String getSortBy() { return sortBy; }
    public void setSortBy(String sortBy) { this.sortBy = sortBy; }

    public String getSortOrder() { return sortOrder; }
    public void setSortOrder(String sortOrder) { this.sortOrder = sortOrder; }

    // ✅ Manual Builder Implementation
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String category;
        private Boolean ecoCertified;
        private BigDecimal maxCarbonImpact;
        private BigDecimal minEcoRating;
        private Boolean recyclable;
        private String sortBy;
        private String sortOrder;

        public Builder category(String category) {
            this.category = category;
            return this;
        }

        public Builder ecoCertified(Boolean ecoCertified) {
            this.ecoCertified = ecoCertified;
            return this;
        }

        public Builder maxCarbonImpact(BigDecimal maxCarbonImpact) {
            this.maxCarbonImpact = maxCarbonImpact;
            return this;
        }

        public Builder minEcoRating(BigDecimal minEcoRating) {
            this.minEcoRating = minEcoRating;
            return this;
        }

        public Builder recyclable(Boolean recyclable) {
            this.recyclable = recyclable;
            return this;
        }

        public Builder sortBy(String sortBy) {
            this.sortBy = sortBy;
            return this;
        }

        public Builder sortOrder(String sortOrder) {
            this.sortOrder = sortOrder;
            return this;
        }

        public ProductFilterDTO build() {
            return new ProductFilterDTO(category, ecoCertified, maxCarbonImpact, minEcoRating, recyclable, sortBy, sortOrder);
        }
    }
}
