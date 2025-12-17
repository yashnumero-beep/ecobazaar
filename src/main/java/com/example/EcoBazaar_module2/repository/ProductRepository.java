package com.example.EcoBazaar_module2.repository;

import com.example.EcoBazaar_module2.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByVerifiedTrue();

    List<Product> findByVerifiedFalse();

    List<Product> findBySellerId(Long sellerId);

    List<Product> findByCategory(String category);

    List<Product> findByActiveTrue();
}
