package com.food.ordering.system.catalog.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class PricingService {

    private final RedissonClient redissonClient;

    // e.g., "surge:item:uuid-1234" -> "1.2"
    private static final String SURGE_KEY_PREFIX = "surge:item:";

    public BigDecimal calculateDynamicPrice(String menuItemId, BigDecimal basePrice) {
        String key = SURGE_KEY_PREFIX + menuItemId;
        RBucket<Double> bucket = redissonClient.getBucket(key);

        Double multiplier = 1.0;

        // Resilience: If Redis is down or key missing, fallback to 1.0
        try {
            if (bucket.isExists()) {
                multiplier = bucket.get();
                log.info("Surge applied for item {}: {}x", menuItemId, multiplier);
            }
        } catch (Exception e) {
            log.error("Redis error fetching surge price for {}. Defaulting to base price.", menuItemId, e);
        }

        return basePrice.multiply(BigDecimal.valueOf(multiplier));
    }

    // Admin/System tool to set surge manually (for testing/simulation)
    public void setSurgeMultiplier(String menuItemId, double multiplier) {
        String key = SURGE_KEY_PREFIX + menuItemId;
        redissonClient.getBucket(key).set(multiplier);
        log.info("Set surge multiplier for {} to {}", menuItemId, multiplier);
    }
}