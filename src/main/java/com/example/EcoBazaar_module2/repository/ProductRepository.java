package com.example.EcoBazaar_module2.repository;


import com.example.EcoBazaar_module2.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // Find active products
    List<Product> findByActiveTrue();

    // Find by seller
    List<Product> findBySellerIdAndActiveTrue(Long sellerId);

    // Find eco-certified products
    List<Product> findByEcoCertifiedTrueAndActiveTrue();

    // Find by category
    List<Product> findByCategoryAndActiveTrue(String category);

    // Search by name or description
    @Query("SELECT p FROM Product p WHERE p.active = true AND " +
            "(LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Product> searchByKeyword(@Param("keyword") String keyword);

    // Find products with carbon impact below threshold
    @Query("SELECT p FROM Product p WHERE p.active = true AND " +
            "p.carbonImpact <= :maxCarbonImpact ORDER BY p.carbonImpact ASC")
    List<Product> findLowCarbonProducts(@Param("maxCarbonImpact") BigDecimal maxCarbonImpact);

    // Find products by carbon impact range
    @Query("SELECT p FROM Product p WHERE p.active = true AND " +
            "p.carbonImpact BETWEEN :minImpact AND :maxImpact ORDER BY p.carbonImpact ASC")
    List<Product> findByCarbonImpactRange(
            @Param("minImpact") BigDecimal minImpact,
            @Param("maxImpact") BigDecimal maxImpact
    );

    // Find best eco-rated products
    @Query("SELECT p FROM Product p WHERE p.active = true AND " +
            "p.ecoRating >= :minRating ORDER BY p.ecoRating DESC, p.carbonImpact ASC")
    List<Product> findBestEcoRatedProducts(@Param("minRating") BigDecimal minRating);

    // Advanced filter query
    @Query("SELECT p FROM Product p WHERE p.active = true " +
            "AND (:category IS NULL OR p.category = :category) " +
            "AND (:ecoCertified IS NULL OR p.ecoCertified = :ecoCertified) " +
            "AND (:maxCarbon IS NULL OR p.carbonImpact <= :maxCarbon) " +
            "AND (:minRating IS NULL OR p.ecoRating >= :minRating) " +
            "AND (:recyclable IS NULL OR p.recyclable = :recyclable) " +
            "ORDER BY p.carbonImpact ASC")
    List<Product> findWithFilters(
            @Param("category") String category,
            @Param("ecoCertified") Boolean ecoCertified,
            @Param("maxCarbon") BigDecimal maxCarbon,
            @Param("minRating") BigDecimal minRating,
            @Param("recyclable") Boolean recyclable
    );

    // Get top eco-friendly alternatives in same category
    @Query("SELECT p FROM Product p WHERE p.active = true " +
            "AND p.category = :category AND p.id != :excludeId " +
            "ORDER BY p.carbonImpact ASC, p.ecoRating DESC")
    List<Product> findEcoAlternativesInCategory(
            @Param("category") String category,
            @Param("excludeId") Long excludeId
    );

    // Statistics queries
    @Query("SELECT AVG(p.carbonImpact) FROM Product p WHERE p.active = true")
    BigDecimal getAverageCarbonImpact();

    @Query("SELECT COUNT(p) FROM Product p WHERE p.active = true AND p.ecoCertified = true")
    Long countEcoCertifiedProducts();

    @Query("SELECT p.category, COUNT(p), AVG(p.carbonImpact) FROM Product p " +
            "WHERE p.active = true GROUP BY p.category")
    List<Object[]> getCategoryStatistics();
}