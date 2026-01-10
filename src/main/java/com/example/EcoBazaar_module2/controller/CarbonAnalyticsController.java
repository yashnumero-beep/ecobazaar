// File: src/main/java/com/example/EcoBazaar_module2/controller/CarbonAnalyticsController.java

package com.example.EcoBazaar_module2.controller;

import com.example.EcoBazaar_module2.service.CarbonAnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/carbon")
@CrossOrigin(origins = "*")
public class CarbonAnalyticsController {

    @Autowired
    private CarbonAnalyticsService carbonAnalyticsService;

    @GetMapping("/report/{userId}")
    public ResponseEntity<Map<String, Object>> getUserCarbonReport(@PathVariable Long userId) {
        try {
            Map<String, Object> report = carbonAnalyticsService.getUserCarbonReport(userId);
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", "Failed to generate report: " + e.getMessage())
            );
        }
    }

    @GetMapping("/admin/summary")
    public ResponseEntity<Map<String, Object>> getPlatformSummary() {
        try {
            Map<String, Object> summary = carbonAnalyticsService.getPlatformCarbonSummary();
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", "Failed to generate platform summary: " + e.getMessage())
            );
        }
    }

    // Enhanced export with real data
    @GetMapping("/report/{userId}/export")
    public ResponseEntity<String> exportCarbonReport(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "txt") String format) {

        try {
            Map<String, Object> report = carbonAnalyticsService.getUserCarbonReport(userId);
            String reportContent = generateReportContent(report, userId);

            String filename = "carbon-report-" + userId + "-" +
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) +
                    ("csv".equalsIgnoreCase(format) ? ".csv" : ".txt");

            String contentType = "csv".equalsIgnoreCase(format) ? "text/csv" : "text/plain";

            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=" + filename)
                    .header("Content-Type", contentType)
                    .body(reportContent);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error generating report: " + e.getMessage());
        }
    }

    private String generateReportContent(Map<String, Object> report, Long userId) {
        StringBuilder sb = new StringBuilder();

        if (report.containsKey("categoryBreakdown") &&
                report.get("categoryBreakdown") instanceof Map) {

            sb.append("=== EcoBazaar Carbon Report ===\n");
            sb.append("User ID: ").append(userId).append("\n");
            sb.append("Generated: ").append(report.get("reportGenerated")).append("\n\n");

            sb.append("Total Carbon Footprint: ")
                    .append(report.get("totalCarbonFootprint")).append(" kg CO2\n");
            sb.append("Monthly Carbon Footprint: ")
                    .append(report.get("monthlyCarbonFootprint")).append(" kg CO2\n");
            sb.append("Carbon Saved: ")
                    .append(report.get("carbonSaved")).append(" kg CO2\n");
            sb.append("Eco Score: ")
                    .append(report.get("ecoScore")).append("/100\n\n");

            sb.append("Category Breakdown:\n");
            Map<String, Double> categoryBreakdown = (Map<String, Double>) report.get("categoryBreakdown");
            categoryBreakdown.forEach((category, carbon) ->
                    sb.append("  ").append(category).append(": ").append(carbon).append(" kg CO2\n"));

            sb.append("\nPersonalized Eco Tips:\n");
            List<String> tips = (List<String>) report.get("ecoTips");
            if (tips != null) {
                tips.forEach(tip -> sb.append("  • ").append(tip).append("\n"));
            }
        } else {
            sb.append("Carbon Report Data Unavailable\n");
        }

        return sb.toString();
    }
}
