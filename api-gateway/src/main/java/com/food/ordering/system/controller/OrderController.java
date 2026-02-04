package com.food.ordering.system.controller;

import com.food.ordering.system.infra.exception.ApiResponse;
import com.food.ordering.system.order.dto.CreateOrderRequest;
import com.food.ordering.system.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ApiResponse<UUID> createOrder(@RequestBody CreateOrderRequest request) {
        UUID orderId = orderService.createOrder(request);
        return ApiResponse.success(orderId);
    }
}