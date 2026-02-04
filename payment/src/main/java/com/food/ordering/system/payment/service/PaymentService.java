package com.food.ordering.system.payment.service;

import com.food.ordering.system.order.service.OrderService;
import com.food.ordering.system.payment.domain.Payment;
import com.food.ordering.system.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderService orderService;
    private final RedissonClient redissonClient; // <--- 1. Inject Redis Client

    @Transactional
    public void processPaymentCallback(UUID orderId, BigDecimal amount, String txnId) {

        // 2. Define a Unique Key for this Transaction
        String redisKey = "payment:processed:" + txnId;
        RBucket<String> bucket = redissonClient.getBucket(redisKey);

        // 3. The Idempotency Check (Atomic Operation)
        // trySet returns TRUE if it set the key, FALSE if it already existed.
        // We keep the key for 24 hours just in case retries happen later.
        boolean isNewTransaction = bucket.trySet("PROCESSED", 24, TimeUnit.HOURS);

        if (!isNewTransaction) {
            log.warn("⚠️ DUPLICATE PAYMENT BLOCKED: Transaction {} was already processed.", txnId);
            return; // <--- EXIT IMMEDIATELY. Save the DB.
        }

        log.info("✅ Processing NEW payment callback for Order: {}", orderId);

        // 4. Record the Transaction in Ledger
        Payment payment = Payment.builder()
                .orderId(orderId)
                .amount(amount)
                .transactionId(txnId)
                .status("SUCCESS")
                .build();
        paymentRepository.save(payment);

        // 5. Update Order Status
        orderService.markOrderPaid(orderId);
    }
}