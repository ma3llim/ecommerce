package org.ecommerce.order.repository;

import org.ecommerce.order.entities.Order;
import org.ecommerce.order.enums.OrderStatus;
import org.ecommerce.order.enums.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {

    Page<Order> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    Optional<Order> findByIdAndUserId(UUID orderId, UUID userId);

    @Query("""
            SELECT o FROM Order o
            WHERE
                (
                    :search IS NULL
                    OR :search = ''
                    OR LOWER(o.orderNumber) LIKE LOWER(CONCAT('%', :search, '%'))
                )
                AND (
                    :orderStatus IS NULL
                    OR o.orderStatus = :orderStatus
                )
                AND (
                    :paymentStatus IS NULL
                    OR o.paymentStatus = :paymentStatus
                )
            """)
    Page<Order> findOrders(
            @Param("search") String search,
            @Param("orderStatus") OrderStatus orderStatus,
            @Param("paymentStatus") PaymentStatus paymentStatus,
            Pageable pageable
    );
}
