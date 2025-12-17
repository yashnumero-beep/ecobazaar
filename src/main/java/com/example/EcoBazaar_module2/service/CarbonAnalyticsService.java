package com.example.EcoBazaar_module2.service;

import com.example.EcoBazaar_module2.model.Order;
import com.example.EcoBazaar_module2.model.OrderItem;
import com.example.EcoBazaar_module2.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CarbonAnalyticsService {

    @Autowired
    private OrderRepository orderRepository;

    public Map<String, Object> getUserCarbonReport(Long userId) {
        List<Order> orders = orderRepository.findByUserId(userId);

        double totalCarbonFootprint = orders.stream()
                .mapToDouble(Order::getTotalCarbonFootprint)
                .sum();

        int greenPurchases = 0;
        for (Order order : orders) {
            for (OrderItem item : order.getItems()) {
                if (item.getCarbonSnapshot() < 2.0) {
                    greenPurchases++;
                }
            }
        }

        // Mock baseline comparison (in real system, use category averages)
        double baselineCarbon = orders.size() * 10.0; // Assume 10kg per order baseline
        double carbonSaved = Math.max(0, baselineCarbon - totalCarbonFootprint);

        int ecoScore = (int) (carbonSaved * 10);
        String badge = getBadge(ecoScore);

        Map<String, Object> report = new HashMap<>();
        report.put("totalCarbonFootprint", totalCarbonFootprint);
        report.put("carbonSaved", carbonSaved);
        report.put("greenPurchases", greenPurchases);
        report.put("ecoScore", ecoScore);
        report.put("badge", badge);
        report.put("totalOrders", orders.size());

        return report;
    }

    public Map<String, Object> getPlatformCarbonSummary() {
        List<Order> allOrders = orderRepository.findAll();

        double totalCarbon = allOrders.stream()
                .mapToDouble(Order::getTotalCarbonFootprint)
                .sum();

        double totalRevenue = allOrders.stream()
                .mapToDouble(Order::getTotalAmount)
                .sum();

        Map<String, Object> summary = new HashMap<>();
        summary.put("totalOrders", allOrders.size());
        summary.put("totalCarbonFootprint", totalCarbon);
        summary.put("totalRevenue", totalRevenue);
        summary.put("averageCarbonPerOrder", allOrders.isEmpty() ? 0 : totalCarbon / allOrders.size());

        return summary;
    }

    private String getBadge(int ecoScore) {
        if (ecoScore > 1000) return "Planet Protector";
        else if (ecoScore > 500) return "Low Carbon Leader";
        else if (ecoScore > 100) return "Eco Enthusiast";
        else return "Eco Starter";
    }
}