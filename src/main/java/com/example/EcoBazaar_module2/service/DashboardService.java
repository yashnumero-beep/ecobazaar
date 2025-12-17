package com.example.EcoBazaar_module2.service;

import com.example.EcoBazaar_module2.dto.*;
import com.example.EcoBazaar_module2.model.*;
import com.example.EcoBazaar_module2.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    // USER DASHBOARD
    public UserDashboardDTO getUserDashboard(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserDashboardDTO dashboard = new UserDashboardDTO();

        List<Order> orders = orderRepository.findByUserId(userId);

        // Stats
        dashboard.setStats(calculateUserStats(orders));

        // Recent Orders
        dashboard.setRecentOrders(getRecentOrders(orders, 5));

        // Achievements
        dashboard.setAchievements(calculateAchievements(dashboard.getStats()));

        // Personalized Tips
        dashboard.setPersonalizedTips(generatePersonalizedTips(orders, dashboard.getStats()));

        // Carbon Trend
        dashboard.setCarbonTrend(calculateCarbonTrend(orders));

        // Category Breakdown
        dashboard.setCategoryBreakdown(calculateCategoryBreakdown(orders));

        // Eco Rating Distribution
        dashboard.setEcoRatingDistribution(calculateEcoRatingDistribution(orders));

        return dashboard;
    }

    private UserStatsDTO calculateUserStats(List<Order> orders) {
        UserStatsDTO stats = new UserStatsDTO();

        double totalCarbon = orders.stream()
                .mapToDouble(Order::getTotalCarbonFootprint)
                .sum();

        double totalSpent = orders.stream()
                .mapToDouble(Order::getTotalAmount)
                .sum();

        int totalItems = orders.stream()
                .mapToInt(o -> o.getItems().size())
                .sum();

        // Count green purchases (products with carbon < 2kg)
        int greenPurchases = 0;
        for (Order order : orders) {
            for (OrderItem item : order.getItems()) {
                if (item.getCarbonSnapshot() < 2.0) {
                    greenPurchases++;
                }
            }
        }

        // Calculate carbon saved (compared to baseline of 10kg per order)
        double baselineCarbon = orders.size() * 10.0;
        double carbonSaved = Math.max(0, baselineCarbon - totalCarbon);

        int ecoScore = (int) (carbonSaved * 10);

        stats.setTotalOrders(orders.size());
        stats.setTotalCarbonFootprint(totalCarbon);
        stats.setTotalSpent(totalSpent);
        stats.setTotalItemsPurchased(totalItems);
        stats.setGreenPurchases(greenPurchases);
        stats.setCarbonSaved(carbonSaved);
        stats.setEcoScore(ecoScore);
        stats.setBadge(determineBadge(ecoScore));

        return stats;
    }

    private String determineBadge(int ecoScore) {
        if (ecoScore >= 1000) return "Planet Protector";
        else if (ecoScore >= 500) return "Low Carbon Leader";
        else if (ecoScore >= 100) return "Eco Enthusiast";
        else return "Eco Starter";
    }

    private List<RecentOrderDTO> getRecentOrders(List<Order> orders, int limit) {
        return orders.stream()
                .sorted((o1, o2) -> o2.getCreatedAt().compareTo(o1.getCreatedAt()))
                .limit(limit)
                .map(order -> new RecentOrderDTO(
                        order.getId(),
                        order.getCreatedAt(),
                        order.getItems().size(),
                        order.getTotalAmount(),
                        order.getTotalCarbonFootprint(),
                        order.getStatus().name()
                ))
                .collect(Collectors.toList());
    }

    private List<AchievementDTO> calculateAchievements(UserStatsDTO stats) {
        List<AchievementDTO> achievements = new ArrayList<>();

        achievements.add(new AchievementDTO("first_order", "First Step",
                "Place your first order", "🎯", stats.getTotalOrders() >= 1, 1, stats.getTotalOrders()));

        achievements.add(new AchievementDTO("eco_starter", "Eco Starter",
                "Earn 100 eco points", "🌱", stats.getEcoScore() >= 100, 100, stats.getEcoScore()));

        achievements.add(new AchievementDTO("green_shopper", "Green Shopper",
                "5 green purchases", "🛍️", stats.getGreenPurchases() >= 5, 5, stats.getGreenPurchases()));

        achievements.add(new AchievementDTO("carbon_saver", "Carbon Saver",
                "Save 10kg CO2e", "♻️", stats.getCarbonSaved() >= 10, 10, stats.getCarbonSaved().intValue()));

        achievements.add(new AchievementDTO("eco_warrior", "Eco Warrior",
                "Earn 500 eco points", "🦸", stats.getEcoScore() >= 500, 500, stats.getEcoScore()));

        achievements.add(new AchievementDTO("planet_hero", "Planet Hero",
                "Save 50kg CO2e", "🌍", stats.getCarbonSaved() >= 50, 50, stats.getCarbonSaved().intValue()));

        achievements.add(new AchievementDTO("legend", "Eco Legend",
                "Earn 1000 eco points", "👑", stats.getEcoScore() >= 1000, 1000, stats.getEcoScore()));

        achievements.add(new AchievementDTO("master", "Sustainability Master",
                "Save 100kg CO2e", "🏆", stats.getCarbonSaved() >= 100, 100, stats.getCarbonSaved().intValue()));

        return achievements;
    }

    private List<CarbonTipDTO> generatePersonalizedTips(List<Order> orders, UserStatsDTO stats) {
        List<CarbonTipDTO> tips = new ArrayList<>();

        if (stats.getGreenPurchases() < 3) {
            tips.add(new CarbonTipDTO("🌿",
                    "Try choosing products with A+ eco rating to maximize your carbon savings!",
                    "SHOPPING", "HIGH"));
        }

        if (stats.getTotalOrders() > 0 && stats.getCarbonSaved() < 5) {
            tips.add(new CarbonTipDTO("🔍",
                    "Look for products with lower carbon footprints in the same category to increase savings.",
                    "SHOPPING", "MEDIUM"));
        }

        if (orders.size() > 0) {
            double avgCarbon = stats.getTotalCarbonFootprint() / orders.size();
            if (avgCarbon > 5) {
                tips.add(new CarbonTipDTO("📊",
                        String.format("Your average carbon per order is %.1fkg. Try bulk buying to reduce packaging impact!", avgCarbon),
                        "OPTIMIZATION", "HIGH"));
            }
        }

        if (stats.getEcoScore() < 100) {
            tips.add(new CarbonTipDTO("⭐",
                    "Keep making eco-friendly choices to unlock the 'Eco Starter' achievement!",
                    "MOTIVATION", "LOW"));
        }

        tips.add(new CarbonTipDTO("💚",
                "Every green purchase you make helps reduce global carbon emissions!",
                "MOTIVATION", "LOW"));

        return tips;
    }

    private CarbonTrendDTO calculateCarbonTrend(List<Order> orders) {
        Map<String, Double> monthlyCarbon = new TreeMap<>();
        Map<String, Integer> monthlyCounts = new TreeMap<>();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");

        for (Order order : orders) {
            String monthKey = order.getCreatedAt().format(formatter);
            monthlyCarbon.merge(monthKey, order.getTotalCarbonFootprint(), Double::sum);
            monthlyCounts.merge(monthKey, 1, Integer::sum);
        }

        CarbonTrendDTO trend = new CarbonTrendDTO();
        trend.setLabels(new ArrayList<>(monthlyCarbon.keySet()));
        trend.setCarbonData(new ArrayList<>(monthlyCarbon.values()));
        trend.setOrderCounts(new ArrayList<>(monthlyCounts.values()));

        return trend;
    }

    private Map<String, Double> calculateCategoryBreakdown(List<Order> orders) {
        Map<String, Double> categoryCarbon = new HashMap<>();

        for (Order order : orders) {
            for (OrderItem item : order.getItems()) {
                String category = item.getProduct().getCategory();
                categoryCarbon.merge(category,
                        item.getCarbonSnapshot() * item.getQuantity(),
                        Double::sum);
            }
        }

        return categoryCarbon;
    }

    private Map<String, Integer> calculateEcoRatingDistribution(List<Order> orders) {
        Map<String, Integer> distribution = new HashMap<>();
        distribution.put("A+", 0);
        distribution.put("B", 0);
        distribution.put("C", 0);

        for (Order order : orders) {
            for (OrderItem item : order.getItems()) {
                double carbon = item.getCarbonSnapshot();
                String rating = carbon < 2.0 ? "A+" : (carbon < 5.0 ? "B" : "C");
                distribution.merge(rating, 1, Integer::sum);
            }
        }

        return distribution;
    }

    // SELLER DASHBOARD
    public SellerDashboardDTO getSellerDashboard(Long sellerId) {
        User seller = userRepository.findById(sellerId)
                .orElseThrow(() -> new RuntimeException("Seller not found"));

        SellerDashboardDTO dashboard = new SellerDashboardDTO();

        List<Product> products = productRepository.findBySellerId(sellerId);
        List<Order> allOrders = orderRepository.findAll();

        // Filter orders containing seller's products
        List<OrderItem> sellerOrderItems = new ArrayList<>();
        for (Order order : allOrders) {
            for (OrderItem item : order.getItems()) {
                if (item.getProduct().getSeller().getId().equals(sellerId)) {
                    sellerOrderItems.add(item);
                }
            }
        }

        dashboard.setStats(calculateSellerStats(products, sellerOrderItems));
        dashboard.setTopProducts(getTopProducts(sellerOrderItems, 5));
        dashboard.setRecentOrders(getSellerRecentOrders(sellerOrderItems, 5));
        dashboard.setSalesByCategory(calculateSalesByCategory(sellerOrderItems));
        dashboard.setRevenueBreakdown(calculateRevenueBreakdown(sellerOrderItems));

        return dashboard;
    }

    private SellerStatsDTO calculateSellerStats(List<Product> products, List<OrderItem> orderItems) {
        SellerStatsDTO stats = new SellerStatsDTO();

        stats.setTotalProducts(products.size());
        stats.setActiveProducts((int) products.stream().filter(Product::isActive).count());
        stats.setPendingProducts((int) products.stream().filter(p -> !p.isVerified()).count());
        stats.setVerifiedProducts((int) products.stream().filter(Product::isVerified).count());

        stats.setTotalSales(orderItems.size());

        double totalRevenue = orderItems.stream()
                .mapToDouble(item -> item.getPriceSnapshot() * item.getQuantity())
                .sum();
        stats.setTotalRevenue(totalRevenue);

        double avgCarbon = products.stream()
                .mapToDouble(Product::getTotalCarbonFootprint)
                .average()
                .orElse(0.0);
        stats.setAverageCarbonPerProduct(avgCarbon);

        return stats;
    }

    private List<ProductPerformanceDTO> getTopProducts(List<OrderItem> orderItems, int limit) {
        Map<Long, ProductPerformanceDTO> productMap = new HashMap<>();

        for (OrderItem item : orderItems) {
            Product product = item.getProduct();
            ProductPerformanceDTO perf = productMap.getOrDefault(product.getId(),
                    new ProductPerformanceDTO(product.getId(), product.getName(), 0, 0.0,
                            product.getTotalCarbonFootprint(), product.getEcoRating()));

            perf.setUnitsSold(perf.getUnitsSold() + item.getQuantity());
            perf.setRevenue(perf.getRevenue() + (item.getPriceSnapshot() * item.getQuantity()));

            productMap.put(product.getId(), perf);
        }

        return productMap.values().stream()
                .sorted((p1, p2) -> Double.compare(p2.getRevenue(), p1.getRevenue()))
                .limit(limit)
                .collect(Collectors.toList());
    }

    private List<RecentOrderDTO> getSellerRecentOrders(List<OrderItem> orderItems, int limit) {
        return orderItems.stream()
                .map(OrderItem::getOrder)
                .distinct()
                .sorted((o1, o2) -> o2.getCreatedAt().compareTo(o1.getCreatedAt()))
                .limit(limit)
                .map(order -> new RecentOrderDTO(
                        order.getId(),
                        order.getCreatedAt(),
                        order.getItems().size(),
                        order.getTotalAmount(),
                        order.getTotalCarbonFootprint(),
                        order.getStatus().toString()
                ))
                .collect(Collectors.toList());
    }

    private Map<String, Double> calculateSalesByCategory(List<OrderItem> orderItems) {
        Map<String, Double> salesByCategory = new HashMap<>();

        for (OrderItem item : orderItems) {
            String category = item.getProduct().getCategory();
            salesByCategory.merge(category,
                    item.getPriceSnapshot() * item.getQuantity(),
                    Double::sum);
        }

        return salesByCategory;
    }

    private RevenueBreakdownDTO calculateRevenueBreakdown(List<OrderItem> orderItems) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime todayStart = now.toLocalDate().atStartOfDay();
        LocalDateTime weekStart = now.minusWeeks(1);
        LocalDateTime monthStart = now.minusMonths(1);

        double todayRevenue = 0, weekRevenue = 0, monthRevenue = 0, totalRevenue = 0;

        for (OrderItem item : orderItems) {
            double revenue = item.getPriceSnapshot() * item.getQuantity();
            LocalDateTime orderDate = item.getOrder().getCreatedAt();

            totalRevenue += revenue;

            if (orderDate.isAfter(todayStart)) todayRevenue += revenue;
            if (orderDate.isAfter(weekStart)) weekRevenue += revenue;
            if (orderDate.isAfter(monthStart)) monthRevenue += revenue;
        }

        return new RevenueBreakdownDTO(todayRevenue, weekRevenue, monthRevenue, totalRevenue);
    }

    // ADMIN DASHBOARD
    public AdminDashboardDTO getAdminDashboard() {
        AdminDashboardDTO dashboard = new AdminDashboardDTO();

        List<User> allUsers = userRepository.findAll();
        List<Product> allProducts = productRepository.findAll();
        List<Order> allOrders = orderRepository.findAll();
        List<AuditLog> recentAudits = auditLogRepository.findAll();

        dashboard.setPlatformStats(calculatePlatformStats(allUsers, allProducts, allOrders));
        dashboard.setPendingVerifications(getPendingVerifications(allProducts));
        dashboard.setTopSellers(calculateTopSellers(allOrders));
        dashboard.setRecentActivities(getRecentActivities(recentAudits, 10));
        dashboard.setCarbonImpact(calculatePlatformCarbonImpact(allOrders));
        dashboard.setUserRoleDistribution(calculateUserRoleDistribution(allUsers));

        return dashboard;
    }

    private PlatformStatsDTO calculatePlatformStats(List<User> users, List<Product> products, List<Order> orders) {
        PlatformStatsDTO stats = new PlatformStatsDTO();

        stats.setTotalUsers(users.size());
        stats.setTotalSellers((int) users.stream().filter(u -> u.getRole() == Role.SELLER).count());
        stats.setTotalProducts(products.size());
        stats.setTotalOrders(orders.size());
        stats.setActiveUsers((int) users.stream().filter(User::isActive).count());

        double totalRevenue = orders.stream()
                .mapToDouble(Order::getTotalAmount)
                .sum();
        stats.setTotalRevenue(totalRevenue);

        double totalCarbon = orders.stream()
                .mapToDouble(Order::getTotalCarbonFootprint)
                .sum();
        stats.setPlatformCarbonFootprint(totalCarbon);

        double baselineCarbon = orders.size() * 10.0;
        stats.setPlatformCarbonSaved(Math.max(0, baselineCarbon - totalCarbon));

        return stats;
    }

    private List<PendingVerificationDTO> getPendingVerifications(List<Product> products) {
        return products.stream()
                .filter(p -> !p.isVerified())
                .map(p -> new PendingVerificationDTO(
                        p.getId(),
                        p.getName(),
                        p.getSeller().getId(),
                        p.getSeller().getFullName(),
                        p.getTotalCarbonFootprint(),
                        p.getCreatedAt()
                ))
                .collect(Collectors.toList());
    }

    private List<TopSellerDTO> calculateTopSellers(List<Order> orders) {
        Map<Long, TopSellerDTO> sellerMap = new HashMap<>();

        for (Order order : orders) {
            for (OrderItem item : order.getItems()) {
                User seller = item.getProduct().getSeller();
                TopSellerDTO sellerDTO = sellerMap.getOrDefault(seller.getId(),
                        new TopSellerDTO(seller.getId(), seller.getFullName(), 0, 0.0, 0.0));

                sellerDTO.setProductsSold(sellerDTO.getProductsSold() + item.getQuantity());
                sellerDTO.setRevenue(sellerDTO.getRevenue() + (item.getPriceSnapshot() * item.getQuantity()));

                sellerMap.put(seller.getId(), sellerDTO);
            }
        }

        // Calculate average carbon impact per seller
        for (TopSellerDTO seller : sellerMap.values()) {
            List<Product> sellerProducts = productRepository.findBySellerId(seller.getSellerId());
            double avgCarbon = sellerProducts.stream()
                    .mapToDouble(Product::getTotalCarbonFootprint)
                    .average()
                    .orElse(0.0);
            seller.setAverageCarbonImpact(avgCarbon);
        }

        return sellerMap.values().stream()
                .sorted((s1, s2) -> Double.compare(s2.getRevenue(), s1.getRevenue()))
                .limit(10)
                .collect(Collectors.toList());
    }

    private List<RecentActivityDTO> getRecentActivities(List<AuditLog> logs, int limit) {
        return logs.stream()
                .sorted((l1, l2) -> l2.getTimestamp().compareTo(l1.getTimestamp()))
                .limit(limit)
                .map(log -> new RecentActivityDTO(
                        log.getActor().getFullName(),
                        log.getAction(),
                        log.getEntityType(),
                        log.getEntityId(),
                        log.getTimestamp(),
                        log.getMetadata()
                ))
                .collect(Collectors.toList());
    }

    private CarbonImpactSummaryDTO calculatePlatformCarbonImpact(List<Order> orders) {
        CarbonImpactSummaryDTO impact = new CarbonImpactSummaryDTO();

        double totalCarbon = orders.stream()
                .mapToDouble(Order::getTotalCarbonFootprint)
                .sum();
        impact.setTotalCarbonFootprint(totalCarbon);

        double baselineCarbon = orders.size() * 10.0;
        impact.setTotalCarbonSaved(Math.max(0, baselineCarbon - totalCarbon));

        double avgCarbon = orders.isEmpty() ? 0 : totalCarbon / orders.size();
        impact.setAverageCarbonPerOrder(avgCarbon);

        int highImpact = 0, lowImpact = 0;
        Map<String, Double> carbonByCategory = new HashMap<>();

        for (Order order : orders) {
            for (OrderItem item : order.getItems()) {
                if (item.getCarbonSnapshot() > 5.0) highImpact++;
                if (item.getCarbonSnapshot() < 2.0) lowImpact++;

                String category = item.getProduct().getCategory();
                carbonByCategory.merge(category,
                        item.getCarbonSnapshot() * item.getQuantity(),
                        Double::sum);
            }
        }

        impact.setHighImpactProducts(highImpact);
        impact.setLowImpactProducts(lowImpact);
        impact.setCarbonByCategory(carbonByCategory);

        return impact;
    }

    private Map<String, Integer> calculateUserRoleDistribution(List<User> users) {
        Map<String, Integer> distribution = new HashMap<>();
        distribution.put("USER", 0);
        distribution.put("SELLER", 0);
        distribution.put("ADMIN", 0);

        for (User user : users) {
            distribution.merge(user.getRole().toString(), 1, Integer::sum);
        }

        return distribution;
    }
}