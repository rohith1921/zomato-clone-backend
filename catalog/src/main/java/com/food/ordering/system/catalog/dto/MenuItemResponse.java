package com.food.ordering.system.catalog.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class MenuItemResponse {
    private UUID id;
    private String name;
    private BigDecimal basePrice;
    private BigDecimal currentPrice; // The dynamic price
    private boolean isSurgeApplied;
}