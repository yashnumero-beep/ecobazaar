// File: src/main/java/com/example/EcoBazaar_module2/controller/CommunityController.java
package com.example.EcoBazaar_module2.controller;

import com.example.EcoBazaar_module2.model.User;
import com.example.EcoBazaar_module2.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/community")
@CrossOrigin(origins = "*")
public class CommunityController {

    @Autowired
    private UserService userService;

    @GetMapping("/carbon-leaderboard")
    public ResponseEntity<List<Map<String, Object>>> getCarbonLeaderboard(
            @RequestParam(defaultValue = "10") int limit) {

        List<User> topEcoUsers = userService.getTopEcoUsers(limit);

        // Create a map of userId to rank for quick lookup
        Map<Long, Integer> rankMap = new HashMap<>();
        AtomicInteger rank = new AtomicInteger(1);
        topEcoUsers.forEach(user -> rankMap.put(user.getId(), rank.getAndIncrement()));

        return ResponseEntity.ok(topEcoUsers.stream().map(user -> {
            Map<String, Object> userMap = new HashMap<>();
            userMap.put("userId", user.getId());
            userMap.put("userName", user.getFullName() != null ? user.getFullName() : "Anonymous User");
            userMap.put("carbonSaved", user.getTotalCarbonSaved() != null ?
                    Math.round(user.getTotalCarbonSaved() * 100.0) / 100.0 : 0.0);
            userMap.put("ecoScore", user.getEcoScore() != null ? user.getEcoScore() : 0);
            userMap.put("rank", rankMap.get(user.getId()));
            return userMap;
        }).collect(Collectors.toList()));
    }

    // Simple rank calculation (in real app, this would be optimized)
    private int calculateRank(Long userId) {
        List<User> allUsers = userService.getAllUsersSortedByCarbonSaved();
        for (int i = 0; i < allUsers.size(); i++) {
            if (allUsers.get(i).getId().equals(userId)) {
                return i + 1;
            }
        }
        return 0;
    }
}
