package org.ecommerce.order.service.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ecommerce.common.dtos.PageResponse;
import org.ecommerce.common.exception.BadRequestException;
import org.ecommerce.common.exception.ResourceNotFoundException;
import org.ecommerce.order.dtos.admin.request.CreateShipmentRequest;
import org.ecommerce.order.dtos.admin.request.UpdateShipmentStatusRequest;
import org.ecommerce.order.dtos.admin.response.ShipmentResponse;
import org.ecommerce.order.entities.Order;
import org.ecommerce.order.entities.Shipment;
import org.ecommerce.order.enums.OrderStatus;
import org.ecommerce.order.enums.ShipmentStatus;
import org.ecommerce.order.repository.OrderRepository;
import org.ecommerce.order.repository.ShipmentRepository;
import org.ecommerce.order.specification.ShipmentSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminShipmentService {
    private final ShipmentRepository shipmentRepository;
    private final OrderRepository orderRepository;
    private final ObjectMapper objectMapper;

    public PageResponse<ShipmentResponse> getAllShipments(
            String search, ShipmentStatus shipmentStatus, String courierName,
            Instant from, Instant to, Pageable pageable
    ) {
        Specification<Shipment> specification = ShipmentSpecification.search(search)
                .and(ShipmentSpecification.hasStatus(shipmentStatus))
                .and(ShipmentSpecification.hasCourier(courierName))
                .and(ShipmentSpecification.createdAfter(from))
                .and(ShipmentSpecification.createdBefore(to));

        Page<Shipment> shipments = shipmentRepository.findAll(specification, pageable);

        Page<ShipmentResponse> shipmentResponses = shipments.map(shipment ->
                objectMapper.convertValue(shipment, ShipmentResponse.class));

        return PageResponse.<ShipmentResponse>builder()
                .content(shipmentResponses.getContent())
                .page(shipments.getNumber())
                .size(shipments.getSize())
                .totalElements(shipments.getTotalElements())
                .totalPages(shipments.getTotalPages())
                .first(shipments.isFirst())
                .last(shipments.isLast())
                .build();
    }

    public ShipmentResponse getShipment(UUID shipmentId) {
        Shipment shipment = shipmentRepository.findById(shipmentId).orElseThrow(() -> {
            log.warn("Shipment not found: shipmentId={}", shipmentId);
            return new ResourceNotFoundException("Shipment not found");
        });

        return objectMapper.convertValue(shipment, ShipmentResponse.class);
    }

    @Transactional
    public ShipmentResponse createShipment(UUID orderId, CreateShipmentRequest request) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> {
            log.warn("Order not found while creating shipment: orderId={}", orderId);
            return new ResourceNotFoundException("Order not found");
        });

        if (shipmentRepository.existsByOrderId(orderId)) {
            throw new BadRequestException("Shipment already exists for this order");
        }


        if (order.getOrderStatus() != OrderStatus.PACKED) {
            throw new BadRequestException("Shipment can only be created for a packed order");
        }

        Shipment shipment = Shipment.builder().orderId(orderId)
                .courierName(request.courierName())
                .trackingNumber(generateTrackingNumber())
                .shipmentStatus(ShipmentStatus.SHIPPED)
                .shippedAt(Instant.now())
                .build();

        shipmentRepository.save(shipment);

        order.setOrderStatus(OrderStatus.SHIPPED);

        orderRepository.save(order);

        log.info("Shipment created: shipmentId={}, orderId={}, trackingNumber={}",
                shipment.getId(), orderId, shipment.getTrackingNumber());

        return objectMapper.convertValue(shipment, ShipmentResponse.class);
    }

    private String generateTrackingNumber() {
        return "SHP-" + System.currentTimeMillis() + "-" + UUID.randomUUID()
                .toString()
                .substring(0, 6)
                .toUpperCase();
    }

    @Transactional
    public ShipmentResponse updateShipmentStatus(UUID shipmentId, UpdateShipmentStatusRequest request) {
        Shipment shipment = shipmentRepository.findById(shipmentId).orElseThrow(() -> {
            log.warn("Shipment not found while updating status: shipmentId={}", shipmentId);
            return new ResourceNotFoundException("Shipment not found");
        });

        ShipmentStatus currentStatus = shipment.getShipmentStatus();
        ShipmentStatus newStatus = request.status();

        if (currentStatus == newStatus) {
            throw new BadRequestException("Shipment is already in " + newStatus + " status");
        }

        validateStatusTransition(currentStatus, newStatus);

        shipment.setShipmentStatus(newStatus);

        if (request.currentLocation() != null && !request.currentLocation().isBlank()) {
            shipment.setCurrentLocation(request.currentLocation().trim());
        }

        if (newStatus == ShipmentStatus.DELIVERED) {
            shipment.setDeliveredAt(Instant.now());
        }

        shipmentRepository.save(shipment);

        if (newStatus == ShipmentStatus.DELIVERED) {
            Order order = orderRepository.findById(shipment.getOrderId()).orElseThrow(() ->
                    new ResourceNotFoundException("Order not found")
            );

            order.setOrderStatus(OrderStatus.DELIVERED);
            orderRepository.save(order);
        }

        log.info("Shipment status updated: shipmentId={}, {} -> {}", shipmentId, currentStatus, newStatus);

        return objectMapper.convertValue(shipment, ShipmentResponse.class);
    }

    private void validateStatusTransition(ShipmentStatus currentStatus, ShipmentStatus newStatus) {
        boolean valid = switch (currentStatus) {
            case PENDING -> newStatus == ShipmentStatus.SHIPPED;
            case SHIPPED -> newStatus == ShipmentStatus.IN_TRANSIT;
            case IN_TRANSIT -> newStatus == ShipmentStatus.OUT_FOR_DELIVERY;
            case OUT_FOR_DELIVERY -> newStatus == ShipmentStatus.DELIVERED;
            case DELIVERED -> false;
        };

        if (!valid) {
            throw new BadRequestException("Invalid shipment status transition: " + currentStatus + " -> " + newStatus);
        }
    }
}
