package org.ecommerce.order.service.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ecommerce.common.dtos.PageResponse;
import org.ecommerce.common.exception.BadRequestException;
import org.ecommerce.common.exception.ResourceNotFoundException;
import org.ecommerce.order.dtos.response.PaymentResponse;
import org.ecommerce.order.entities.Payment;
import org.ecommerce.order.enums.PaymentMethod;
import org.ecommerce.order.enums.PaymentStatus;
import org.ecommerce.order.repository.PaymentRepository;
import org.ecommerce.order.specification.PaymentSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminPaymentService {
    private final PaymentRepository paymentRepository;
    private final ObjectMapper objectMapper;

    public PageResponse<PaymentResponse> getAllPayments(
            String search, String paymentStatus, String paymentMethod, Instant from, Instant to,
            BigDecimal minAmount, BigDecimal maxAmount, Pageable pageable
    ) {
        PaymentStatus status = parsePaymentStatus(paymentStatus);
        PaymentMethod method = parsePaymentMethod(paymentMethod);

        Specification<Payment> specification = PaymentSpecification.search(search)
                .and(PaymentSpecification.hasPaymentStatus(status))
                .and(PaymentSpecification.hasPaymentMethod(method))
                .and(PaymentSpecification.createdAfter(from))
                .and(PaymentSpecification.createdBefore(to))
                .and(PaymentSpecification.amountGreaterThanOrEqual(minAmount))
                .and(PaymentSpecification.amountLessThanOrEqual(maxAmount));

        Page<Payment> payments = paymentRepository.findAll(specification, pageable);

        List<PaymentResponse> content = payments.getContent().stream()
                .map(payment -> objectMapper.convertValue(payment, PaymentResponse.class)).toList();

        log.info("Admin payment list fetched: search={}, status={}, method={}, page={}, size={}",
                search, status, method, pageable.getPageNumber(), pageable.getPageSize());

        return PageResponse.<PaymentResponse>builder()
                .content(content)
                .page(payments.getNumber())
                .size(payments.getSize())
                .totalElements(payments.getTotalElements())
                .totalPages(payments.getTotalPages())
                .first(payments.isFirst())
                .last(payments.isLast())
                .build();
    }

    public PaymentResponse getPayment(UUID paymentId) {
        Payment payment = paymentRepository.findById(paymentId).orElseThrow(() -> {
            log.warn("Admin payment not found: paymentId={}", paymentId);
            return new ResourceNotFoundException("Payment not found");
        });
        log.info("Admin payment details fetched: paymentId={}", paymentId);

        return objectMapper.convertValue(payment, PaymentResponse.class);
    }

    private PaymentStatus parsePaymentStatus(String paymentStatus) {
        if (paymentStatus == null || paymentStatus.isBlank()) {
            return null;
        }

        try {
            return PaymentStatus.valueOf(paymentStatus.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Invalid payment status: {}", paymentStatus);
            throw new BadRequestException("Invalid payment status: " + paymentStatus);
        }
    }

    private PaymentMethod parsePaymentMethod(String paymentMethod) {
        if (paymentMethod == null || paymentMethod.isBlank()) {
            return null;
        }

        try {
            return PaymentMethod.valueOf(paymentMethod.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Invalid payment method: {}", paymentMethod);
            throw new BadRequestException("Invalid payment method: " + paymentMethod);
        }
    }
}
