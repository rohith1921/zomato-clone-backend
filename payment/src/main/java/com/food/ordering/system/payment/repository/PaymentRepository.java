package com.food.ordering.system.payment.repository;

import com.food.ordering.system.payment.domain.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {
}