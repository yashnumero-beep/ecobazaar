package com.example.EcoBazaar_module2.controller;

import com.example.EcoBazaar_module2.dto.AdminDashboardDTO;
import com.example.EcoBazaar_module2.dto.SellerDashboardDTO;
import com.example.EcoBazaar_module2.dto.UserDashboardDTO;
import com.example.EcoBazaar_module2.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    /**
     * USER DASHBOARD
     * GET /api/dashboard/user/{userId}
     *
     * Returns comprehensive dashboard for USER role including:
     * - Stats (eco score, carbon saved, badges)
     * - Recent orders
     * - Achievements
     * - Personalized carbon tips
     * - Carbon trend over time
     * - Category breakdown
     * - Eco rating distribution
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<UserDashboardDTO> getUserDashboard(@PathVariable Long userId) {
        try {
            UserDashboardDTO dashboard = dashboardService.getUserDashboard(userId);
            return ResponseEntity.ok(dashboard);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * SELLER DASHBOARD
     * GET /api/dashboard/seller/{sellerId}
     *
     * Returns comprehensive dashboard for SELLER role including:
     * - Stats (total products, sales, revenue)
     * - Top performing products
     * - Recent orders
     * - Sales by category
     * - Revenue breakdown (today/week/month/total)
     */
    @GetMapping("/seller/{sellerId}")
    public ResponseEntity<SellerDashboardDTO> getSellerDashboard(@PathVariable Long sellerId) {
        try {
            SellerDashboardDTO dashboard = dashboardService.getSellerDashboard(sellerId);
            return ResponseEntity.ok(dashboard);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * ADMIN DASHBOARD
     * GET /api/dashboard/admin
     *
     * Returns comprehensive dashboard for ADMIN role including:
     * - Platform statistics (users, sellers, products, orders)
     * - Pending verifications
     * - Top sellers
     * - Recent audit activities
     * - Platform carbon impact summary
     * - User role distribution
     */
    @GetMapping("/admin")
    public ResponseEntity<AdminDashboardDTO> getAdminDashboard() {
        try {
            AdminDashboardDTO dashboard = dashboardService.getAdminDashboard();
            return ResponseEntity.ok(dashboard);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}