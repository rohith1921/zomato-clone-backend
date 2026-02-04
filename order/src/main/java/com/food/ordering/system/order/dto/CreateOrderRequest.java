package com.food.ordering.system.order.dto;

import lombok.Data;
import java.util.Map;
import java.util.UUID;

@Data
public class CreateOrderRequest {
    private UUID userId;
    private String restaurantId;
    // Map of MenuItemId -> Quantity
    private Map<String, Integer> items;
}