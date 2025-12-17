package com.example.EcoBazaar_module2.controller;

import com.example.EcoBazaar_module2.model.Cart;
import com.example.EcoBazaar_module2.model.CartItem;
import com.example.EcoBazaar_module2.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @GetMapping("/{userId}")
    public ResponseEntity<Map<String, Object>> getCart(@PathVariable Long userId) {
        Cart cart = cartService.getUserCart(userId);

        Map<String, Object> response = new HashMap<>();
        response.put("id", cart.getId());
        response.put("items", cart.getItems().stream().map(item -> {
            Map<String, Object> itemDTO = new HashMap<>();
            itemDTO.put("id", item.getId());
            itemDTO.put("productId", item.getProduct().getId());
            itemDTO.put("productName", item.getProduct().getName());
            itemDTO.put("price", item.getProduct().getPrice());
            itemDTO.put("carbonFootprint", item.getProduct().getTotalCarbonFootprint());
            itemDTO.put("quantity", item.getQuantity());
            itemDTO.put("imageUrl", item.getProduct().getImageUrl());
            return itemDTO;
        }).collect(Collectors.toList()));

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{userId}/items")
    public ResponseEntity<?> addItem(@PathVariable Long userId, @RequestBody Map<String, Object> request) {
        try {
            Long productId = Long.valueOf(request.get("productId").toString());
            Integer quantity = Integer.valueOf(request.getOrDefault("quantity", 1).toString());

            CartItem item = cartService.addItemToCart(userId, productId, quantity);

            return ResponseEntity.ok(Map.of(
                    "message", "Item added to cart",
                    "itemId", item.getId()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{userId}/items/{itemId}")
    public ResponseEntity<?> removeItem(@PathVariable Long userId, @PathVariable Long itemId) {
        try {
            cartService.removeItemFromCart(userId, itemId);
            return ResponseEntity.ok(Map.of("message", "Item removed"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}