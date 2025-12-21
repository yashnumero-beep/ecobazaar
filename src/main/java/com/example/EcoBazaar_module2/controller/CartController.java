package com.example.EcoBazaar_module2.controller;

import com.example.EcoBazaar_module2.model.Cart;
import com.example.EcoBazaar_module2.model.CartItem;
import com.example.EcoBazaar_module2.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
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

        response.put(
                "items",
                cart.getItems()
                        .stream()
                        .map(this::mapToCartItemDTO)
                        .collect(Collectors.toList())
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{userId}/items")
    public ResponseEntity<?> addItem(
            @PathVariable Long userId,
            @RequestBody Map<String, Object> request
    ) {
        try {
            Long productId = Long.parseLong(request.get("productId").toString());
            Integer quantity = Integer.parseInt(
                    request.getOrDefault("quantity", 1).toString()
            );

            CartItem item = cartService.addItemToCart(userId, productId, quantity);

            return ResponseEntity.ok(Map.of(
                    "message", "Item added to cart",
                    "itemId", item.getId()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", e.getMessage()
            ));
        }
    }

    @DeleteMapping("/{userId}/items/{itemId}")
    public ResponseEntity<?> removeItem(
            @PathVariable Long userId,
            @PathVariable Long itemId
    ) {
        try {
            cartService.removeItemFromCart(userId, itemId);

            return ResponseEntity.ok(Map.of(
                    "message", "Item removed successfully"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", e.getMessage()
            ));
        }
    }

    private Map<String, Object> mapToCartItemDTO(CartItem item) {
        Map<String, Object> dto = new HashMap<>();

        var product = item.getProduct();

        dto.put("id", item.getId());
        dto.put("productId", product.getId());
        dto.put("productName", product.getName());
        dto.put("price", product.getPrice());
        dto.put("quantity", item.getQuantity());
        dto.put("imageUrl", product.getImageUrl());
        dto.put("carbonFootprint", product.getTotalCarbonFootprint());
        dto.put("ecoRating", product.getEcoRating());

        return dto;
    }
}