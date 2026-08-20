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
import org.ecommerce.order.dtos.admin.response.ShipmentTimelineResponse;
import org.ecommerce.order.entities.Order;
import org.ecommerce.order.entities.Shipment;
import org.ecommerce.order.entities.ShipmentTrackingEvent;
import org.ecommerce.order.enums.OrderStatus;
import org.ecommerce.order.enums.ShipmentStatus;
import org.ecommerce.order.repository.OrderRepository;
import org.ecommerce.order.repository.ShipmentRepository;
import org.ecommerce.order.repository.ShipmentTrackingEventRepository;
import org.ecommerce.order.specification.ShipmentSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminShipmentService {
    private final ShipmentRepository shipmentRepository;
    private final OrderRepository orderRepository;
    private final ShipmentTrackingEventRepository trackingEventRepository;
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

        List<ShipmentResponse> responses = shipments.getContent().stream()
                .map(shipment -> objectMapper.convertValue(shipment, ShipmentResponse.class)).toList();

        return PageResponse.<ShipmentResponse>builder()
                .content(responses)
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

        List<ShipmentTrackingEvent> shipmentTrackingEvents = trackingEventRepository
                .findByShipmentIdOrderByEventTimeDesc(shipmentId);

        List<ShipmentTimelineResponse> shipmentTimelineResponses = shipmentTrackingEvents.stream()
                .map(shipmentTrackingEvent -> objectMapper.convertValue(shipmentTrackingEvent,
                        ShipmentTimelineResponse.class)).toList();

        return ShipmentResponse.builder()
                .shipmentId(shipment.getId())
                .orderId(shipment.getOrderId())
                .courierName(shipment.getCourierName())
                .trackingNumber(shipment.getTrackingNumber())
                .shipmentStatus(shipment.getShipmentStatus())
                .shippedAt(shipment.getShippedAt())
                .deliveredAt(shipment.getDeliveredAt())
                .createdAt(shipment.getCreatedAt())
                .updatedAt(shipment.getUpdatedAt())
                .timeline(shipmentTimelineResponses)
                .build();
    }

    @Transactional
    public ShipmentResponse createShipment(UUID orderId, CreateShipmentRequest request) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> {
            log.warn("Order not found while creating shipment: orderId={}", orderId);
            return new ResourceNotFoundException("Order not found");
        });

        if (shipmentRepository.existsByOrderId(orderId)) {
            log.warn("Create shipment rejected: shipment already exists. orderId={}", orderId);
            throw new BadRequestException("Shipment already exists for this order");
        }


        if (order.getOrderStatus() != OrderStatus.PACKED) {
            log.warn("Create shipment rejected: order is not packed. orderId={}, orderStatus={}",
                    orderId, order.getOrderStatus());
            throw new BadRequestException("Shipment can only be created for a packed order");
        }

        Instant now = Instant.now();

        String trackingNumber = generateTrackingNumber();

        Shipment shipment = Shipment.builder()
                .orderId(orderId)
                .courierName(request.courierName())
                .trackingNumber(trackingNumber)
                .shipmentStatus(ShipmentStatus.SHIPPED)
                .shippedAt(now)
                .build();

        shipmentRepository.save(shipment);

        ShipmentTrackingEvent trackingEvent = ShipmentTrackingEvent.builder()
                .shipmentId(shipment.getId())
                .status(ShipmentStatus.SHIPPED)
                .location("Warehouse")
                .description("Shipment picked up by courier")
                .eventTime(now)
                .build();

        trackingEventRepository.save(trackingEvent);

        order.setOrderStatus(OrderStatus.SHIPPED);

        orderRepository.save(order);

        log.info("Shipment created: shipmentId={}, orderId={}, trackingNumber={}", shipment.getId(),
                orderId, shipment.getTrackingNumber());

        List<ShipmentTrackingEvent> trackingEvents = trackingEventRepository.findByShipmentIdOrderByEventTimeAsc(
                shipment.getId());

        List<ShipmentTimelineResponse> shipmentTimelineResponses = trackingEvents.stream()
                .map(event -> objectMapper.convertValue(event, ShipmentTimelineResponse.class)
                ).toList();

        return ShipmentResponse.builder()
                .shipmentId(shipment.getId())
                .orderId(shipment.getOrderId())
                .courierName(shipment.getCourierName())
                .trackingNumber(shipment.getTrackingNumber())
                .shipmentStatus(shipment.getShipmentStatus())
                .shippedAt(shipment.getShippedAt())
                .deliveredAt(shipment.getDeliveredAt())
                .createdAt(shipment.getCreatedAt())
                .updatedAt(shipment.getUpdatedAt())
                .timeline(shipmentTimelineResponses)
                .build();
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

        validateStatusTransition(currentStatus, newStatus);

        Instant now = Instant.now();
        shipment.setShipmentStatus(newStatus);

        if (newStatus == ShipmentStatus.DELIVERED) {
            shipment.setDeliveredAt(now);
        }

        shipmentRepository.save(shipment);

        ShipmentTrackingEvent trackingEvent = ShipmentTrackingEvent.builder()
                .shipmentId(shipment.getId())
                .status(newStatus)
                .location(request.currentLocation())
                .description(request.description())
                .eventTime(now)
                .build();

        trackingEventRepository.save(trackingEvent);

        if (newStatus == ShipmentStatus.DELIVERED) {
            Order order = orderRepository.findById(shipment.getOrderId()).orElseThrow(() -> {
                log.warn("Update shipment failed: order not found. shipmentId={}, orderId={}",
                        shipmentId, shipment.getOrderId());
                return new ResourceNotFoundException("Order not found");
            });

            order.setOrderStatus(OrderStatus.DELIVERED);
            orderRepository.save(order);
        }

        log.info("Shipment status updated: shipmentId={}, {} -> {}, location={}",
                shipmentId, currentStatus, newStatus, request.currentLocation()
        );

        List<ShipmentTrackingEvent> trackingEvents = trackingEventRepository.findByShipmentIdOrderByEventTimeAsc(
                shipment.getId());

        List<ShipmentTimelineResponse> shipmentTimelineResponses = trackingEvents.stream()
                .map(event -> objectMapper.convertValue(event, ShipmentTimelineResponse.class)
                ).toList();

        return ShipmentResponse.builder()
                .shipmentId(shipment.getId())
                .orderId(shipment.getOrderId())
                .courierName(shipment.getCourierName())
                .trackingNumber(shipment.getTrackingNumber())
                .shipmentStatus(shipment.getShipmentStatus())
                .shippedAt(shipment.getShippedAt())
                .deliveredAt(shipment.getDeliveredAt())
                .createdAt(shipment.getCreatedAt())
                .updatedAt(shipment.getUpdatedAt())
                .timeline(shipmentTimelineResponses)
                .build();
    }

    private void validateStatusTransition(ShipmentStatus currentStatus, ShipmentStatus newStatus) {
        boolean valid = switch (currentStatus) {
            case PENDING -> newStatus == ShipmentStatus.PENDING || newStatus == ShipmentStatus.SHIPPED;
            case SHIPPED -> newStatus == ShipmentStatus.SHIPPED || newStatus == ShipmentStatus.IN_TRANSIT;
            case IN_TRANSIT -> newStatus == ShipmentStatus.IN_TRANSIT || newStatus == ShipmentStatus.OUT_FOR_DELIVERY;
            case OUT_FOR_DELIVERY ->
                    newStatus == ShipmentStatus.OUT_FOR_DELIVERY || newStatus == ShipmentStatus.DELIVERED;
            case DELIVERED -> false;
        };

        if (!valid) {
            log.warn("Shipment status transition rejected: currentStatus={}, requestedStatus={}",
                    currentStatus, newStatus);
            throw new BadRequestException("Invalid shipment status transition: " + currentStatus + " -> " + newStatus);
        }
    }
}