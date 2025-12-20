package com.example.EcoBazaar_module2.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private Double price;

    @Column(nullable = false)
    private Integer quantity = 1;

    // Simplified: Store image filenames as comma-separated string
    // Format: "image1.jpg,image2.jpg,image3.jpg"
    @Column(length = 1000)
    private String images = "";

    @Column(nullable = false)
    private String category;

    @ManyToOne
    @JoinColumn(name = "seller_id", nullable = false)
    private User seller;

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false)
    private boolean verified = false;

    @Column(nullable = false)
    private boolean featured = false;

    // New fields for enhanced e-commerce
    @Column(nullable = false)
    private Integer viewCount = 0;

    @Column(nullable = false)
    private Integer soldCount = 0;

    @Column(nullable = false)
    private Double averageRating = 0.0;

    @Column(nullable = false)
    private Integer reviewCount = 0;

    @OneToOne(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private ProductCarbonData carbonData;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Review> reviews = new ArrayList<>();

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // Helper method to get primary image
    @Transient
    public String getPrimaryImage() {
        if (images == null || images.isEmpty()) {
            return "/api/images/default-product.jpg";
        }
        String[] imageArray = images.split(",");
        return "/api/images/" + imageArray[0];
    }

    // Helper method to get all images as array
    @Transient
    public String[] getImageArray() {
        if (images == null || images.isEmpty()) {
            return new String[]{"/api/images/default-product.jpg"};
        }
        String[] imageArray = images.split(",");
        String[] fullPaths = new String[imageArray.length];
        for (int i = 0; i < imageArray.length; i++) {
            fullPaths[i] = "/api/images/" + imageArray[i];
        }
        return fullPaths;
    }

    @Transient
    public Double getTotalCarbonFootprint() {
        return carbonData != null ? carbonData.getTotalCO2e() : 0.0;
    }

    @Transient
    public String getEcoRating() {
        Double total = getTotalCarbonFootprint();
        if (total < 2.0) return "A+";
        else if (total < 5.0) return "B";
        else return "C";
    }

    public void incrementViewCount() {
        this.viewCount++;
    }

    public void incrementSoldCount(int quantity) {
        this.soldCount += quantity;
    }

    public void updateRating(double newRating, int newReviewCount) {
        this.averageRating = newRating;
        this.reviewCount = newReviewCount;
    }
}