package org.ecommerce.order.repository;

import org.ecommerce.order.entities.Shipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface ShipmentRepository extends JpaRepository<Shipment, UUID>, JpaSpecificationExecutor<Shipment> {

    Optional<Shipment> findByOrderId(UUID orderId);

    boolean existsByOrderId(UUID orderId);

    boolean existsByTrackingNumber(String trackingNumber);

    Optional<Shipment> findByTrackingNumber(String trackingNumber);
}
