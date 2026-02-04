package com.food.ordering.system.catalog.service;

import com.food.ordering.system.catalog.domain.MenuItem;
import com.food.ordering.system.catalog.dto.MenuItemResponse;
import com.food.ordering.system.catalog.repository.MenuItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CatalogService {

    private final MenuItemRepository menuItemRepository;
    private final PricingService pricingService;

    public List<MenuItemResponse> getMenuForRestaurant(String restaurantId) {
        List<MenuItem> items = menuItemRepository.findByRestaurantId(restaurantId);

        return items.stream().map(item -> {
            var currentPrice = pricingService.calculateDynamicPrice(item.getId().toString(), item.getBasePrice());

            return MenuItemResponse.builder()
                    .id(item.getId())
                    .name(item.getName())
                    .basePrice(item.getBasePrice())
                    .currentPrice(currentPrice)
                    .isSurgeApplied(currentPrice.compareTo(item.getBasePrice()) > 0)
                    .build();
        }).toList();
    }
}