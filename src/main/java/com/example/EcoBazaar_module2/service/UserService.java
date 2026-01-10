package com.example.EcoBazaar_module2.service;

import com.example.EcoBazaar_module2.model.SellerProfile;
import com.example.EcoBazaar_module2.model.User;
import com.example.EcoBazaar_module2.repository.SellerProfileRepository;
import com.example.EcoBazaar_module2.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SellerProfileRepository sellerProfileRepository;

    @Autowired
    private AuditService auditService;

    public User getUserProfile(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Transactional
    public User updateUserProfile(Long userId, String fullName) {
        User user = getUserProfile(userId);
        user.setFullName(fullName);
        return userRepository.save(user);
    }

    @Transactional
    public SellerProfile createSellerProfile(Long userId, String businessName, String description, String contactPhone) {
        User user = getUserProfile(userId);

        if (sellerProfileRepository.findByUserId(userId).isPresent()) {
            throw new RuntimeException("Seller profile already exists");
        }

        SellerProfile profile = new SellerProfile();
        profile.setUser(user);
        profile.setBusinessName(businessName);
        profile.setDescription(description);
        profile.setContactPhone(contactPhone);
        profile.setActive(true);

        return sellerProfileRepository.save(profile);
    }

    public List<User> getAllUsers(Long adminId) {
        auditService.log(adminId, "VIEW_ALL_USERS", "USER", 0L, null);
        return userRepository.findAll();
    }

    @Transactional
    public void deactivateUser(Long adminId, Long userId, String reason) {
        User user = getUserProfile(userId);
        user.setActive(false);
        userRepository.save(user);

        auditService.log(adminId, "DEACTIVATE_USER", "USER", userId, reason);
    }

    @Transactional
    public void activateUser(Long adminId, Long userId) {
        User user = getUserProfile(userId);
        user.setActive(true);
        userRepository.save(user);

        auditService.log(adminId, "ACTIVATE_USER", "USER", userId, null);
    }

    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
    }

    // FIXED: Enhanced leaderboard with real carbon calculations
    public List<User> getTopEcoUsers(int limit) {
        List<User> allUsers = userRepository.findAll();

        return allUsers.stream()
                .filter(user -> user.getTotalCarbonSaved() != null)
                .sorted((u1, u2) -> Double.compare(u2.getTotalCarbonSaved(), u1.getTotalCarbonSaved()))
                .limit(limit)
                .collect(Collectors.toList());
    }

    // FIXED: Proper sorting for all users
    public List<User> getAllUsersSortedByCarbonSaved() {
        List<User> allUsers = userRepository.findAll();

        return allUsers.stream()
                .filter(user -> user.getTotalCarbonSaved() != null)
                .sorted((u1, u2) -> Double.compare(u2.getTotalCarbonSaved(), u1.getTotalCarbonSaved()))
                .collect(Collectors.toList());
    }

    public User findById(Long userId) {
        return userRepository.findById(userId).orElse(null);
    }
}
