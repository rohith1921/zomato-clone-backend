package com.food.ordering.system.inventory.repository;

import com.food.ordering.system.inventory.domain.Inventory;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, UUID> {

    /**
     * PESSIMISTIC_WRITE generates: SELECT ... FOR UPDATE
     * This physically prevents any other transaction from reading/writing
     * this specific row until the current transaction finishes.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM Inventory i WHERE i.menuItemId = :menuItemId AND i.restaurantId = :restaurantId")
    Optional<Inventory> findByMenuItemIdAndRestaurantIdLocked(String menuItemId, String restaurantId);

    // Standard read without lock (for menu display)
    Optional<Inventory> findByMenuItemIdAndRestaurantId(String menuItemId, String restaurantId);
}