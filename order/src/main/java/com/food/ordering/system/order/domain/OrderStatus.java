package com.food.ordering.system.order.domain;

public enum OrderStatus {
    CREATED,
    PENDING_PAYMENT,
    PAID,
    CANCELLED,
    COMPLETED
}