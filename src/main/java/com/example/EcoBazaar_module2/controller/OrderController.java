package com.example.EcoBazaar_module2.controller;

import com.example.EcoBazaar_module2.model.Order;
import com.example.EcoBazaar_module2.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping("/{userId}")
    public ResponseEntity<?> createOrder(@PathVariable Long userId) {
        try {
            Order order = orderService.createOrderFromCart(userId);

            Map<String, Object> response = new HashMap<>();
            response.put("orderId", order.getId());
            response.put("totalAmount", order.getTotalAmount());
            response.put("totalCarbonFootprint", order.getTotalCarbonFootprint());
            response.put("message", "Order created successfully");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Map<String, Object>>> getUserOrders(@PathVariable Long userId) {
        List<Order> orders = orderService.getUserOrders(userId);

        return ResponseEntity.ok(orders.stream().map(order -> {
            Map<String, Object> dto = new HashMap<>();
            dto.put("id", order.getId());
            dto.put("totalAmount", order.getTotalAmount());
            dto.put("totalCarbonFootprint", order.getTotalCarbonFootprint());
            dto.put("status", order.getStatus());
            dto.put("createdAt", order.getCreatedAt());
            dto.put("itemCount", order.getItems().size());
            return dto;
        }).collect(Collectors.toList()));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<Map<String, Object>> getOrderDetails(@PathVariable Long orderId) {
        Order order = orderService.getOrderById(orderId);

        Map<String, Object> dto = new HashMap<>();
        dto.put("id", order.getId());
        dto.put("totalAmount", order.getTotalAmount());
        dto.put("totalCarbonFootprint", order.getTotalCarbonFootprint());
        dto.put("status", order.getStatus());
        dto.put("createdAt", order.getCreatedAt());
        dto.put("items", order.getItems().stream().map(item -> {
            Map<String, Object> itemDTO = new HashMap<>();
            itemDTO.put("productName", item.getProductNameSnapshot());
            itemDTO.put("quantity", item.getQuantity());
            itemDTO.put("price", item.getPriceSnapshot());
            itemDTO.put("carbon", item.getCarbonSnapshot());
            return itemDTO;
        }).collect(Collectors.toList()));

        return ResponseEntity.ok(dto);
    }

    @GetMapping("/admin/all")
    public ResponseEntity<List<Map<String, Object>>> getAllOrders() {
        List<Order> orders = orderService.getAllOrders();

        return ResponseEntity.ok(orders.stream().map(order -> {
            Map<String, Object> dto = new HashMap<>();
            dto.put("id", order.getId());
            dto.put("userId", order.getUser().getId());
            dto.put("userEmail", order.getUser().getEmail());
            dto.put("totalAmount", order.getTotalAmount());
            dto.put("totalCarbonFootprint", order.getTotalCarbonFootprint());
            dto.put("status", order.getStatus());
            dto.put("createdAt", order.getCreatedAt());
            return dto;
        }).collect(Collectors.toList()));
    }
}