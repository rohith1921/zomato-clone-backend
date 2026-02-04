package com.food.ordering.system.controller;

import com.food.ordering.system.catalog.dto.MenuItemResponse;
import com.food.ordering.system.catalog.service.CatalogService;
import com.food.ordering.system.infra.exception.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/menu")
@RequiredArgsConstructor
public class MenuController {

    private final CatalogService catalogService;

    @GetMapping("/{restaurantId}")
    public ApiResponse<List<MenuItemResponse>> getMenu(@PathVariable String restaurantId) {
        return ApiResponse.success(catalogService.getMenuForRestaurant(restaurantId));
    }
}