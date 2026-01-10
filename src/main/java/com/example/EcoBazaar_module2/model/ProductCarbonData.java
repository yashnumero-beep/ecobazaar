package com.example.EcoBazaar_module2.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "product_carbon_data")
public class ProductCarbonData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "product_id", nullable = false, unique = true)
    private Product product;

    @Column(nullable = false)
    private Double manufacturing = 0.0;

    @Column(nullable = false)
    private Double transportation = 0.0;

    @Column(nullable = false)
    private Double packaging = 0.0;

    @Column(nullable = false)
    private Double usage = 0.0;

    @Column(nullable = false)
    private Double disposal = 0.0;

    // Inside ProductCarbonData.java

    @Transient
    public Double getTotalCO2e() {
        // Treat nulls as 0.0 to prevent crashes
        double m = manufacturing != null ? manufacturing : 0.0;
        double t = transportation != null ? transportation : 0.0;
        double p = packaging != null ? packaging : 0.0;
        double u = usage != null ? usage : 0.0;
        double d = disposal != null ? disposal : 0.0;
        return m + t + p + u + d;
    }
}