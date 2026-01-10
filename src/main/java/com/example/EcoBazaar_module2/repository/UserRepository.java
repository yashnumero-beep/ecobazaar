package com.example.EcoBazaar_module2.repository;

import com.example.EcoBazaar_module2.model.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    List<User> findByRole(Role role);

    // In UserRepository.java
    @Query("SELECT u FROM User u WHERE u.totalCarbonSaved IS NOT NULL ORDER BY u.totalCarbonSaved DESC")
    Page<User> findTopUsersByCarbonSaved(Pageable pageable);

    @Query("SELECT u FROM User u ORDER BY u.totalCarbonSaved DESC")
    Page<User> findAllByOrderByTotalCarbonSavedDesc(Pageable pageable);

}

