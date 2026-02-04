package com.food.ordering.system.inventory.service;

import com.food.ordering.system.inventory.domain.Inventory;
import com.food.ordering.system.inventory.exception.OutOfStockException;
import com.food.ordering.system.inventory.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService {

    private final InventoryRepository inventoryRepository;

    /**
     * DEDUCT STOCK (Critical Path)
     * 1. Start Transaction
     * 2. Lock the Row (DB Level)
     * 3. Check Quantity
     * 4. Update
     * 5. Commit (Lock Released)
     */
    @Transactional
    public void deductInventory(String restaurantId, String menuItemId, int quantityToDeduct) {
        log.info("Attempting to deduct {} items for Product: {}", quantityToDeduct, menuItemId);

        // 1. Lock and Fetch
        Inventory inventory = inventoryRepository.findByMenuItemIdAndRestaurantIdLocked(menuItemId, restaurantId)
                .orElseThrow(() -> new OutOfStockException("Item not found in inventory"));

        // 2. Business Check
        if (inventory.getQuantity() < quantityToDeduct) {
            log.error("OVERSOLD! Current: {}, Requested: {}", inventory.getQuantity(), quantityToDeduct);
            throw new OutOfStockException("Insufficient stock for item: " + menuItemId);
        }

        // 3. Update State
        inventory.setQuantity(inventory.getQuantity() - quantityToDeduct);
        inventoryRepository.save(inventory); // Explicit save (though Hibernate Dirty Checking handles it too)

        log.info("Stock deducted successfully. Remaining: {}", inventory.getQuantity());
    }

    // Helper to seed data (for testing later)
    @Transactional
    public Inventory initializeStock(String restaurantId, String menuItemId, int initialStock) {
        return inventoryRepository.save(Inventory.builder()
                .restaurantId(restaurantId)
                .menuItemId(menuItemId)
                .quantity(initialStock)
                .reserved(0)
                .build());
    }
}