package org.ecommerce.order.specification;

import org.ecommerce.order.entities.Shipment;
import org.ecommerce.order.enums.ShipmentStatus;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;

public final class ShipmentSpecification {
    private ShipmentSpecification() {
    }

    public static Specification<Shipment> search(String search) {
        return (root, query, cb) -> {
            if (search == null || search.isBlank()) {
                return cb.conjunction();
            }

            String value = "%" + search.trim().toLowerCase() + "%";

            return cb.or(
                    cb.like(cb.lower(root.get("courierName")), value),
                    cb.like(cb.lower(root.get("trackingNumber")), value),
                    cb.like(cb.lower(root.get("currentLocation")), value)
            );
        };
    }

    public static Specification<Shipment> hasStatus(ShipmentStatus status) {
        return (root, query, cb) -> {
            if (status == null) {
                return cb.conjunction();
            }

            return cb.equal(root.get("shipmentStatus"), status);
        };
    }

    public static Specification<Shipment> hasCourier(String courierName) {
        return (root, query, cb) -> {
            if (courierName == null || courierName.isBlank()) {
                return cb.conjunction();
            }

            return cb.equal(cb.lower(root.get("courierName")), courierName.trim().toLowerCase());
        };
    }

    public static Specification<Shipment> createdAfter(Instant from) {
        return (root, query, cb) -> {
            if (from == null) {
                return cb.conjunction();
            }

            return cb.greaterThanOrEqualTo(root.get("createdAt"), from);
        };
    }

    public static Specification<Shipment> createdBefore(Instant to) {
        return (root, query, cb) -> {
            if (to == null) {
                return cb.conjunction();
            }

            return cb.lessThanOrEqualTo(root.get("createdAt"), to);
        };
    }
}
