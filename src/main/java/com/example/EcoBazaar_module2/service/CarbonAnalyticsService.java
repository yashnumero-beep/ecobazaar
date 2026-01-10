// File: src/main/java/com/example/EcoBazaar_module2/service/CarbonAnalyticsService.java

package com.example.EcoBazaar_module2.service;

import com.example.EcoBazaar_module2.model.Order;
import com.example.EcoBazaar_module2.model.OrderItem;
import com.example.EcoBazaar_module2.model.User;
import com.example.EcoBazaar_module2.repository.OrderRepository;
import com.example.EcoBazaar_module2.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CarbonAnalyticsService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public Map<String, Object> getUserCarbonReport(Long userId) {
        Map<String, Object> report = new HashMap<>();

        // Get user and orders
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        List<Order> userOrders = orderRepository.findByUserId(userId);

        // Calculate ACTUAL carbon footprint from orders
        double totalCarbonFootprint = userOrders.stream()
                .mapToDouble(order -> order.getItems().stream()
                        .mapToDouble(item -> item.getCarbonSnapshot() * item.getQuantity())
                        .sum())
                .sum();

        // Calculate BASELINE carbon (industry average - assume 50kg per order)
        double baselineCarbon = userOrders.size() * 50.0;

        // Calculate CARBON SAVED (baseline - actual)
        double carbonSaved = Math.max(0, baselineCarbon - totalCarbonFootprint);

        // Update user's carbon saved and eco score
        user.setTotalCarbonSaved(carbonSaved);

        // Calculate eco score based on carbon efficiency
        int ecoScore = calculateRealEcoScore(totalCarbonFootprint, userOrders.size(), carbonSaved);
        user.setEcoScore(ecoScore);

        userRepository.save(user);

        // Calculate this month's data
        LocalDateTime startOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        double currentMonthCarbon = userOrders.stream()
                .filter(order -> order.getCreatedAt().isAfter(startOfMonth))
                .mapToDouble(order -> order.getItems().stream()
                        .mapToDouble(item -> item.getCarbonSnapshot() * item.getQuantity())
                        .sum())
                .sum();

        // Category breakdown
        Map<String, Double> categoryBreakdown = calculateCategoryBreakdown(userOrders);

        // Eco tips based on user's shopping patterns
        List<String> ecoTips = generateEcoTips(userOrders, categoryBreakdown);

        // Populate report
        report.put("userId", userId);
        report.put("totalCarbonFootprint", Math.round(totalCarbonFootprint * 100.0) / 100.0);
        report.put("monthlyCarbonFootprint", Math.round(currentMonthCarbon * 100.0) / 100.0);
        report.put("carbonSaved", Math.round(carbonSaved * 100.0) / 100.0);
        report.put("ecoScore", ecoScore);
        report.put("categoryBreakdown", categoryBreakdown);
        report.put("ecoTips", ecoTips);
        report.put("reportGenerated", LocalDateTime.now());

        return report;
    }

    public Map<String, Object> getPlatformCarbonSummary() {
        Map<String, Object> summary = new HashMap<>();

        List<Order> allOrders = orderRepository.findAll();

        double totalPlatformCarbon = allOrders.stream()
                .mapToDouble(order -> order.getItems().stream()
                        .mapToDouble(item -> item.getCarbonSnapshot() * item.getQuantity())
                        .sum())
                .sum();

        long totalUsers = userRepository.count();
        long totalOrders = allOrders.size();

        // Average carbon per user
        double avgCarbonPerUser = totalUsers > 0 ? totalPlatformCarbon / totalUsers : 0;

        summary.put("totalPlatformCarbon", Math.round(totalPlatformCarbon * 100.0) / 100.0);
        summary.put("totalUsers", totalUsers);
        summary.put("totalOrders", totalOrders);
        summary.put("averageCarbonPerUser", Math.round(avgCarbonPerUser * 100.0) / 100.0);
        summary.put("generatedAt", LocalDateTime.now());

        return summary;
    }

    private int calculateRealEcoScore(double totalCarbon, int totalOrders, double carbonSaved) {
        if (totalOrders == 0) return 100; // Perfect score for no carbon footprint

        double carbonPerOrder = totalCarbon / totalOrders;
        int score;

        if (carbonPerOrder < 2.0) score = 100;
        else if (carbonPerOrder < 5.0) score = 85;
        else if (carbonPerOrder < 10.0) score = 70;
        else if (carbonPerOrder < 20.0) score = 50;
        else score = 30;

        // Bonus for carbon savings
        if (carbonSaved > 100) score += 20;
        else if (carbonSaved > 50) score += 10;
        else if (carbonSaved > 10) score += 5;

        return Math.min(100, Math.max(0, score));
    }

    private Map<String, Double> calculateCategoryBreakdown(List<Order> orders) {
        Map<String, Double> categoryBreakdown = new HashMap<>();

        orders.forEach(order -> {
            order.getItems().forEach(item -> {
                String category = item.getProduct().getCategory();
                double carbon = item.getCarbonSnapshot() * item.getQuantity();

                categoryBreakdown.merge(category, carbon, Double::sum);
            });
        });

        // Round values
        return categoryBreakdown.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> Math.round(e.getValue() * 100.0) / 100.0
                ));
    }

    private List<String> generateEcoTips(List<Order> orders, Map<String, Double> categoryBreakdown) {
        List<String> tips = new ArrayList<>();

        if (orders.isEmpty()) {
            tips.add("Start making eco-friendly purchases to build your carbon profile!");
            return tips;
        }

        // Tip based on highest carbon category
        if (!categoryBreakdown.isEmpty()) {
            String highestCategory = categoryBreakdown.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse("");

            if ("Electronics".equals(highestCategory)) {
                tips.add("Consider repairing electronics instead of replacing them");
            } else if ("Clothing".equals(highestCategory)) {
                tips.add("Opt for sustainable clothing brands and buy less frequently");
            } else if ("Food".equals(highestCategory)) {
                tips.add("Choose local and seasonal food products to reduce transportation emissions");
            }
        }

        // General tips
        tips.add("Look for products with A+ eco-ratings for better environmental impact");
        tips.add("Consider the full lifecycle carbon footprint when making purchases");
        tips.add("Share your eco-friendly purchases to inspire others!");

        return tips;
    }
}
