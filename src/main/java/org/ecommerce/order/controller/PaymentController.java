package org.ecommerce.order.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.ecommerce.order.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Tag(name = "Payments", description = "APIs for handling payment operations and Razorpay webhooks")
public class PaymentController {
    private final PaymentService paymentService;

    @Operation(summary = "Handle Razorpay payment webhook", description = "Receives and processes Razorpay webhook events to update payment and order status.")
    @PostMapping("/webhook")
    public ResponseEntity<Void> handleWebhook(
            @RequestBody String payload,
            @RequestHeader("X-Razorpay-Signature") String signature
    ) {
        paymentService.handleWebhook(payload, signature);

        return ResponseEntity.ok().build();
    }
}
