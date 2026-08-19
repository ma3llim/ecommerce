package org.ecommerce.order.specification;

import org.ecommerce.order.entities.Payment;
import org.ecommerce.order.enums.PaymentMethod;
import org.ecommerce.order.enums.PaymentStatus;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.Instant;

public final class PaymentSpecification {
    private PaymentSpecification() {
    }

    public static Specification<Payment> hasPaymentStatus(PaymentStatus status) {
        return (root, query, cb) -> {

            if (status == null) {
                return cb.conjunction();
            }

            return cb.equal(root.get("paymentStatus"), status);
        };
    }

    public static Specification<Payment> hasPaymentMethod(PaymentMethod paymentMethod) {
        return (root, query, cb) -> {
            if (paymentMethod == null) {
                return cb.conjunction();
            }

            return cb.equal(root.get("paymentMethod"), paymentMethod);
        };
    }

    public static Specification<Payment> search(String search) {
        return (root, query, cb) -> {
            if (search == null || search.isBlank()) {
                return cb.conjunction();
            }

            String value = "%" + search.toLowerCase() + "%";

            return cb.or(
                    cb.like(root.get("id").as(String.class), "%" + search + "%"),
                    cb.like(root.get("orderId").as(String.class), "%" + search + "%"),
                    cb.like(cb.lower(root.get("transactionId")), value),
                    cb.like(cb.lower(root.get("razorpayOrderId")), value)
            );
        };
    }

    public static Specification<Payment> createdAfter(Instant from) {
        return (root, query, cb) -> {
            if (from == null) {
                return cb.conjunction();
            }

            return cb.greaterThanOrEqualTo(root.get("createdAt"), from);
        };
    }

    public static Specification<Payment> createdBefore(Instant to) {
        return (root, query, cb) -> {

            if (to == null) {
                return cb.conjunction();
            }

            return cb.lessThanOrEqualTo(root.get("createdAt"), to);
        };
    }

    public static Specification<Payment> amountGreaterThanOrEqual(BigDecimal amount) {
        return (root, query, cb) -> {

            if (amount == null) {
                return cb.conjunction();
            }

            return cb.greaterThanOrEqualTo(root.get("amount"), amount);
        };
    }

    public static Specification<Payment> amountLessThanOrEqual(BigDecimal amount) {
        return (root, query, cb) -> {
            if (amount == null) {
                return cb.conjunction();
            }

            return cb.lessThanOrEqualTo(root.get("amount"), amount);
        };
    }
}
