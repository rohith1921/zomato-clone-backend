package com.food.ordering.system.order.service;

import com.food.ordering.system.catalog.service.PricingService;
import com.food.ordering.system.inventory.service.InventoryService;
import com.food.ordering.system.order.domain.Order;
import com.food.ordering.system.order.domain.OrderItem;
import com.food.ordering.system.order.domain.OrderStatus;
import com.food.ordering.system.order.dto.CreateOrderRequest;
import com.food.ordering.system.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final InventoryService inventoryService; // Cross-module call
    private final PricingService pricingService;     // Cross-module call

    @Transactional
    public UUID createOrder(CreateOrderRequest request) {
        log.info("Creating order for user: {}", request.getUserId());

        Order order = Order.builder()
                .userId(request.getUserId())
                .restaurantId(request.getRestaurantId())
                .status(OrderStatus.CREATED) // Initial state
                .totalPrice(BigDecimal.ZERO)
                .build();

        // 1. Sort items by ID to prevent Deadlocks during Inventory Locking
        //    (Deadlock happens if Thread A locks Item 1->2, Thread B locks Item 2->1)
        List<String> sortedItemIds = request.getItems().keySet().stream()
                .sorted()
                .toList();

        BigDecimal total = BigDecimal.ZERO;

        for (String itemId : sortedItemIds) {
            int quantity = request.getItems().get(itemId);

            // 2. Lock & Deduct Inventory (This will BLOCK if another transaction is holding the lock)
            inventoryService.deductInventory(request.getRestaurantId(), itemId, quantity);

            // 3. Calculate Price (Snapshotting the dynamic price at this exact moment)
            //    Note: In a real app, we'd fetch base price from DB, here assuming base=10 for simplicity or fetching via Catalog
            //    Let's assume for now we trust the client or (better) fetch from CatalogService.
            //    For this exercise, we will assume a fixed base price to keep code simple,
            //    or you can inject MenuItemRepository if you want perfection.
            BigDecimal basePrice = BigDecimal.valueOf(100.00); // Mocked for simplicity
            BigDecimal finalPrice = pricingService.calculateDynamicPrice(itemId, basePrice);

            order.addItem(OrderItem.builder()
                    .menuItemId(itemId)
                    .quantity(quantity)
                    .priceAtTimeOfOrder(finalPrice)
                    .build());

            total = total.add(finalPrice.multiply(BigDecimal.valueOf(quantity)));
        }

        order.setTotalPrice(total);
        order.setStatus(OrderStatus.PENDING_PAYMENT); // Ready for Razorpay

        Order savedOrder = orderRepository.save(order);
        log.info("Order created successfully: {}", savedOrder.getId());

        return savedOrder.getId();
    }

    @Transactional
    public void markOrderPaid(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        // Idempotency Check: Don't pay twice
        if (order.getStatus() == OrderStatus.PAID) {
            log.warn("Order {} is already paid.", orderId);
            return;
        }

        order.setStatus(OrderStatus.PAID);
        orderRepository.save(order);
        log.info("Order {} marked as PAID.", orderId);
    }
}