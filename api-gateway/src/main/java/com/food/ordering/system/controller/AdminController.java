package com.food.ordering.system.controller;

import com.food.ordering.system.catalog.service.PricingService;
import com.food.ordering.system.infra.exception.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {

    private final PricingService pricingService;

    @PostMapping("/surge")
    public ApiResponse<Void> setSurgePrice(@RequestParam String itemId, @RequestParam Double multiplier) {
        pricingService.setSurgeMultiplier(itemId, multiplier);
        return ApiResponse.success(null);
    }
}