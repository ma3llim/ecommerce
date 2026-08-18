package org.ecommerce.order.repository;

import org.ecommerce.order.entities.Shipment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ShipmentRepository extends JpaRepository<Shipment, UUID> {

    Optional<Shipment> findByOrderId(UUID orderId);

    Optional<Shipment> findByOrderIdAndUserId(UUID orderId, UUID userId);
}
