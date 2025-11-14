package com.example.EcoBazaar_module2.service;


import com.example.EcoBazaar_module2.model.Product;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;


@Service
@Slf4j
public class CarbonCalculatorService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final Map<String, Double> CATEGORY_CARBON_FACTORS = Map.of(
            "Electronics", 150.0,
            "Clothing", 15.0,
            "Food", 2.5,
            "Books", 1.5,
            "Toys", 8.0,
            "Furniture", 50.0,
            "Beauty", 5.0,
            "Sports", 10.0,
            "Home & Garden", 12.0,
            "Automotive", 200.0
    );

    private static final Map<String, Double> SHIPPING_FACTORS = Map.of(
            "Air", 0.5,
            "Sea", 0.01,
            "Road", 0.1,
            "Rail", 0.03
    );


    public BigDecimal calculateCarbonFootprint(Product product) {
        log.info("Calculating carbon footprint for product: {}", product.getName());

        double totalCarbon = 0.0;

        // 1. Manufacturing carbon
        double manufacturingCarbon = calculateManufacturingCarbon(product);
        totalCarbon += manufacturingCarbon;

        // 2. Shipping carbon
        double shippingCarbon = calculateShippingCarbon(product);
        totalCarbon += shippingCarbon;

        // 3. Packaging carbon (estimated 10% of product weight)
        double packagingCarbon = calculatePackagingCarbon(product);
        totalCarbon += packagingCarbon;

        // Apply adjustments for eco-friendly practices
        if (Boolean.TRUE.equals(product.getRenewableEnergyUsed())) {
            totalCarbon *= 0.7; // 30% reduction
        }

        if (Boolean.TRUE.equals(product.getRecyclable())) {
            totalCarbon *= 0.9; // 10% reduction
        }

        log.info("Calculated carbon footprint: {} kg CO2e", totalCarbon);
        return BigDecimal.valueOf(totalCarbon).setScale(3, RoundingMode.HALF_UP);
    }

    /**
     * Calculate manufacturing carbon based on category and weight
     */
    private double calculateManufacturingCarbon(Product product) {
        String category = product.getCategory() != null ? product.getCategory() : "General";
        double factor = CATEGORY_CARBON_FACTORS.getOrDefault(category, 20.0);

        BigDecimal weight = product.getWeightKg() != null ? product.getWeightKg() : BigDecimal.ONE;

        return factor * weight.doubleValue();
    }

    /**
     * Calculate shipping carbon based on distance and mode
     */
    private double calculateShippingCarbon(Product product) {
        BigDecimal weight = product.getWeightKg() != null ? product.getWeightKg() : BigDecimal.ONE;

        // Estimate shipping distance based on manufacturing location
        double distance = estimateShippingDistance(product.getManufacturingLocation());

        // Assume road transport by default
        double shippingFactor = SHIPPING_FACTORS.get("Road");

        double shippingCarbon = weight.doubleValue() * (distance / 1000.0) * shippingFactor;

        // Apply offset if enabled
        if (Boolean.TRUE.equals(product.getShippingCarbonOffset())) {
            shippingCarbon = 0.0;
        }

        return shippingCarbon;
    }

    /**
     * Calculate packaging carbon (simplified)
     */
    private double calculatePackagingCarbon(Product product) {
        BigDecimal weight = product.getWeightKg() != null ? product.getWeightKg() : BigDecimal.ONE;

        // Packaging is roughly 10% of product weight
        double packagingWeight = weight.doubleValue() * 0.1;

        // Paper/cardboard packaging factor: 1.5 kg CO2e per kg
        double packagingFactor = 1.5;

        if (Boolean.TRUE.equals(product.getRecyclable())) {
            packagingFactor *= 0.5; // Recycled packaging has lower impact
        }

        return packagingWeight * packagingFactor;
    }

    /**
     * Estimate shipping distance based on location
     */
    private double estimateShippingDistance(String location) {
        if (location == null) return 500.0; // Default 500km

        // Simplified distance estimation
        String lowerLocation = location.toLowerCase();
        if (lowerLocation.contains("local") || lowerLocation.contains("india")) {
            return 300.0;
        } else if (lowerLocation.contains("asia")) {
            return 2000.0;
        } else if (lowerLocation.contains("europe") || lowerLocation.contains("us")) {
            return 8000.0;
        }

        return 1000.0; // Default
    }

    /**
     * Generate detailed carbon breakdown as JSON string
     */
    public String generateCarbonBreakdown(Product product) {
        try {
            Map<String, Object> breakdown = new HashMap<>();

            double manufacturing = calculateManufacturingCarbon(product);
            double shipping = calculateShippingCarbon(product);
            double packaging = calculatePackagingCarbon(product);

            breakdown.put("manufacturing", round(manufacturing, 3));
            breakdown.put("shipping", round(shipping, 3));
            breakdown.put("packaging", round(packaging, 3));
            breakdown.put("total", round(manufacturing + shipping + packaging, 3));

            Map<String, Object> details = new HashMap<>();
            details.put("category", product.getCategory());
            details.put("weight_kg", product.getWeightKg());
            details.put("manufacturing_location", product.getManufacturingLocation());
            details.put("renewable_energy_used", product.getRenewableEnergyUsed());
            details.put("shipping_offset", product.getShippingCarbonOffset());

            breakdown.put("details", details);

            return objectMapper.writeValueAsString(breakdown);
        } catch (Exception e) {
            log.error("Error generating carbon breakdown", e);
            return "{}";
        }
    }

    /**
     * Helper method to round doubles
     */
    private double round(double value, int places) {
        return BigDecimal.valueOf(value)
                .setScale(places, RoundingMode.HALF_UP)
                .doubleValue();
    }

    /**
     * Validate carbon footprint data from external API
     * This would be used if integrating with third-party carbon APIs
     */
    public boolean validateCarbonData(BigDecimal carbonImpact, Product product) {
        if (carbonImpact == null || carbonImpact.compareTo(BigDecimal.ZERO) < 0) {
            return false;
        }

        // Calculate expected range
        BigDecimal calculated = calculateCarbonFootprint(product);
        BigDecimal lowerBound = calculated.multiply(BigDecimal.valueOf(0.5));
        BigDecimal upperBound = calculated.multiply(BigDecimal.valueOf(2.0));

        // Check if within reasonable range
        return carbonImpact.compareTo(lowerBound) >= 0 &&
                carbonImpact.compareTo(upperBound) <= 0;
    }
}