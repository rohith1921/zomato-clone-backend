package com.food.ordering.system.config;

import com.food.ordering.system.catalog.domain.MenuItem;
import com.food.ordering.system.catalog.repository.MenuItemRepository;
import com.food.ordering.system.inventory.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final MenuItemRepository menuItemRepository;
    private final InventoryService inventoryService;

    @Override
    public void run(String... args) throws Exception {
        if (menuItemRepository.count() > 0) return; // Don't run if data exists

        String restaurantId = "rest-1";

        // 1. Create a Burger
        MenuItem burger = MenuItem.builder()
                .name("Spicy Chicken Burger")
                .description("Very spicy")
                .basePrice(BigDecimal.valueOf(150)) // ₹150
                .restaurantId(restaurantId)
                .available(true)
                .build();
        menuItemRepository.save(burger);

        // 2. Add Stock (100 items)
        inventoryService.initializeStock(restaurantId, burger.getId().toString(), 100);

        System.out.println("✅ DATA SEEDED: Restaurant ID: " + restaurantId + " | Item ID: " + burger.getId());
    }
}