package com.food.ordering.system.inventory.domain;

import com.food.ordering.system.infra.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "inventory", indexes = {
        // Indexing allows fast lookups by restaurant and item
        @Index(name = "idx_inventory_item", columnList = "restaurant_id, menu_item_id")
})
public class Inventory extends BaseEntity {

    @Column(name = "restaurant_id", nullable = false)
    private String restaurantId;

    @Column(name = "menu_item_id", nullable = false)
    private String menuItemId;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false)
    private int reserved; // Useful for "Held in cart" logic later
}