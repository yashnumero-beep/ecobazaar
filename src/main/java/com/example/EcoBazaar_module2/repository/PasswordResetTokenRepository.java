package com.example.EcoBazaar_module2.repository;

import com.example.EcoBazaar_module2.model.PasswordResetToken;
import com.example.EcoBazaar_module2.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByToken(String token);

    @Modifying
    @Query("DELETE FROM PasswordResetToken t WHERE t.user = :user")
    void deleteByUser(@Param("user") User user);

    Optional<PasswordResetToken> findByUser(User user);

    @Query("SELECT t FROM PasswordResetToken t WHERE t.expiryDate < CURRENT_TIMESTAMP")
    List<PasswordResetToken> findAllExpiredTokens();
}
