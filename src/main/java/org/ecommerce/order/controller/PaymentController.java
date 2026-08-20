package org.ecommerce.order.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.ecommerce.common.response.ApiSuccessResponse;
import org.ecommerce.order.dtos.response.PaymentResponse;
import org.ecommerce.order.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

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

    @Operation(
            summary = "Initiate payment",
            description = "Initiates payment for the specified order using the authenticated customer's payment details."
    )
    @PreAuthorize("hasRole('USER')")
    @PostMapping("/{orderId}/payments")
    public ResponseEntity<ApiSuccessResponse<PaymentResponse>> initiatePayment(
            @PathVariable UUID orderId, Authentication authentication, HttpServletRequest request
    ) {
        PaymentResponse response = paymentService.initiatePayment(orderId, authentication);

        return ResponseEntity.ok(
                ApiSuccessResponse.<PaymentResponse>builder()
                        .success(true)
                        .message("Payment initiated successfully")
                        .data(response)
                        .path(request.getRequestURI())
                        .build()
        );
    }
}
