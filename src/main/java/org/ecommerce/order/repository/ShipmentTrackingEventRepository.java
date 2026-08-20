package org.ecommerce.order.repository;

import org.ecommerce.order.entities.ShipmentTrackingEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ShipmentTrackingEventRepository extends JpaRepository<ShipmentTrackingEvent, UUID> {
    List<ShipmentTrackingEvent> findByShipmentIdOrderByEventTimeDesc(UUID shipmentId);

    List<ShipmentTrackingEvent> findByShipmentIdOrderByEventTimeAsc(UUID shipmentId);
}
