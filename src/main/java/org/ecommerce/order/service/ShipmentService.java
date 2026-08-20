package org.ecommerce.order.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ecommerce.auth.entities.User;
import org.ecommerce.common.exception.ResourceNotFoundException;
import org.ecommerce.order.dtos.response.ShipmentTimelineResponse;
import org.ecommerce.order.dtos.response.UserShipmentResponse;
import org.ecommerce.order.entities.Order;
import org.ecommerce.order.entities.Shipment;
import org.ecommerce.order.entities.ShipmentTrackingEvent;
import org.ecommerce.order.repository.OrderRepository;
import org.ecommerce.order.repository.ShipmentRepository;
import org.ecommerce.order.repository.ShipmentTrackingEventRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShipmentService {
    private final OrderRepository orderRepository;
    private final ShipmentRepository shipmentRepository;
    private final ShipmentTrackingEventRepository trackingEventRepository;
    private final ObjectMapper objectMapper;

    public UserShipmentResponse getShipment(
            String orderNumber, Authentication authentication
    ) {
        UUID userId = ((User) authentication.getPrincipal()).getId();

        Order order = orderRepository.findByOrderNumberAndUserId(orderNumber, userId).orElseThrow(() -> {
            log.warn("Get shipment failed: order not found. orderNumber={}, userId={}", orderNumber, userId);
            return new ResourceNotFoundException("Order not found");
        });

        Shipment shipment = shipmentRepository.findByOrderId(order.getId()).orElseThrow(() -> {
            log.warn("Get shipment failed: shipment not found. orderNumber={}, orderId={}, userId={}",
                    orderNumber, order.getId(), userId);
            return new ResourceNotFoundException("Shipment not found");
        });

        List<ShipmentTrackingEvent> trackingEvents = trackingEventRepository
                .findByShipmentIdOrderByEventTimeAsc(shipment.getId());

        List<ShipmentTimelineResponse> timeline = trackingEvents.stream().map(event ->
                objectMapper.convertValue(event, ShipmentTimelineResponse.class)).toList();

        return UserShipmentResponse.builder()
                .shipmentId(shipment.getId())
                .courierName(shipment.getCourierName())
                .trackingNumber(shipment.getTrackingNumber())
                .shipmentStatus(shipment.getShipmentStatus())
                .shippedAt(shipment.getShippedAt())
                .deliveredAt(shipment.getDeliveredAt())
                .timeline(timeline)
                .build();
    }
}
