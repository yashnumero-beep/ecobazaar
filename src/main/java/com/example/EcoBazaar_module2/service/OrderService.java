package com.example.EcoBazaar_module2.service;

import com.example.EcoBazaar_module2.model.*;
import com.example.EcoBazaar_module2.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private CartService cartService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuditService auditService;

    @Transactional
    public Order createOrderFromCart(Long userId) {
        Cart cart = cartService.getUserCart(userId);

        if (cart.getItems().isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Order order = new Order();
        order.setUser(user);

        double totalAmount = 0.0;
        double totalCarbon = 0.0;

        for (CartItem cartItem : cart.getItems()) {
            Product product = cartItem.getProduct();

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPriceSnapshot(product.getPrice());
            orderItem.setCarbonSnapshot(product.getTotalCarbonFootprint());
            orderItem.setProductNameSnapshot(product.getName());

            order.getItems().add(orderItem);

            totalAmount += product.getPrice() * cartItem.getQuantity();
            totalCarbon += product.getTotalCarbonFootprint() * cartItem.getQuantity();
        }

        order.setTotalAmount(totalAmount);
        order.setTotalCarbonFootprint(totalCarbon);

        Order savedOrder = orderRepository.save(order);

        // Clear cart after order
        cartService.clearCart(userId);

        auditService.log(userId, "CREATE_ORDER", "ORDER", savedOrder.getId(),
                "Total: $" + totalAmount);

        return savedOrder;
    }

    public List<Order> getUserOrders(Long userId) {
        return orderRepository.findByUserId(userId);
    }

    public Order getOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }
}