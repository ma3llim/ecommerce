package org.ecommerce.order.controller.admin;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ecommerce.common.dtos.PageResponse;
import org.ecommerce.common.response.ApiSuccessResponse;
import org.ecommerce.order.dtos.response.PaymentResponse;
import org.ecommerce.order.service.admin.AdminPaymentService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/payments")
@Slf4j
@RequiredArgsConstructor
public class AdminPaymentController {
    private final AdminPaymentService adminPaymentService;

    @GetMapping
    public ResponseEntity<ApiSuccessResponse<PageResponse<PaymentResponse>>> getAllPayments(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String paymentStatus,
            @RequestParam(required = false) String paymentMethod,
            @RequestParam(required = false) Instant from,
            @RequestParam(required = false) Instant to,
            @RequestParam(required = false) BigDecimal minAmount,
            @RequestParam(required = false) BigDecimal maxAmount,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            HttpServletRequest request
    ) {
        PageResponse<PaymentResponse> response = adminPaymentService.getAllPayments(
                search, paymentStatus, paymentMethod, from, to, minAmount, maxAmount, pageable);

        return ResponseEntity.ok(ApiSuccessResponse
                .<PageResponse<PaymentResponse>>builder()
                .success(true)
                .message("Payments retrieved successfully")
                .data(response)
                .path(request.getRequestURI())
                .build()
        );
    }

    @GetMapping("/{paymentId}")
    public ResponseEntity<ApiSuccessResponse<PaymentResponse>> getPayment(
            @PathVariable UUID paymentId,
            HttpServletRequest request
    ) {
        PaymentResponse response = adminPaymentService.getPayment(paymentId);

        return ResponseEntity.ok(ApiSuccessResponse
                .<PaymentResponse>builder()
                .success(true)
                .message("Payment details retrieved successfully")
                .data(response)
                .path(request.getRequestURI())
                .build()
        );
    }
}
