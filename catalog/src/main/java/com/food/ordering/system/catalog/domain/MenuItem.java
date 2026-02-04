package com.food.ordering.system.catalog.domain;

import com.food.ordering.system.infra.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "menu_items")
public class MenuItem extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private BigDecimal basePrice; // e.g., $10.00

    @Column(nullable = false)
    private String restaurantId;

    @Column(nullable = false)
    private boolean available;
}