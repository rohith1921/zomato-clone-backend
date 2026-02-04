package com.food.ordering.system.controller;

import com.food.ordering.system.infra.exception.ApiResponse;
import com.food.ordering.system.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    // Simulates: POST /webhook/razorpay
    @PostMapping("/webhook")
    public ApiResponse<Void> handlePaymentWebhook(
            @RequestParam UUID orderId,
            @RequestParam BigDecimal amount,
            @RequestParam String txnId) {

        paymentService.processPaymentCallback(orderId, amount, txnId);
        return ApiResponse.success(null);
    }
}