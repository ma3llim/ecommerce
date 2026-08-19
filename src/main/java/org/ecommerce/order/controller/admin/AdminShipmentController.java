package org.ecommerce.order.controller.admin;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.ecommerce.common.dtos.PageResponse;
import org.ecommerce.common.response.ApiSuccessResponse;
import org.ecommerce.order.dtos.admin.request.CreateShipmentRequest;
import org.ecommerce.order.dtos.admin.request.UpdateShipmentStatusRequest;
import org.ecommerce.order.dtos.admin.response.ShipmentResponse;
import org.ecommerce.order.enums.ShipmentStatus;
import org.ecommerce.order.service.admin.AdminShipmentService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/shipments")
@PreAuthorize("hasRole('ADMIN')")
public class AdminShipmentController {
    private final AdminShipmentService adminShipmentService;

    @GetMapping
    public ResponseEntity<ApiSuccessResponse<PageResponse<ShipmentResponse>>> getAllShipments(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) ShipmentStatus shipmentStatus,
            @RequestParam(required = false) String courierName,
            @RequestParam(required = false) Instant from,
            @RequestParam(required = false) Instant to,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            HttpServletRequest request
    ) {
        PageResponse<ShipmentResponse> response = adminShipmentService.getAllShipments(
                search, shipmentStatus, courierName, from, to, pageable);

        return ResponseEntity.ok(
                ApiSuccessResponse.<PageResponse<ShipmentResponse>>builder()
                        .success(true)
                        .message("Shipments retrieved successfully")
                        .data(response)
                        .path(request.getRequestURI())
                        .build()
        );
    }

    @GetMapping("/{shipmentId}")
    public ResponseEntity<ApiSuccessResponse<ShipmentResponse>> getShipment(
            @PathVariable UUID shipmentId,
            HttpServletRequest request
    ) {
        ShipmentResponse response = adminShipmentService.getShipment(shipmentId);

        return ResponseEntity.ok(
                ApiSuccessResponse.<ShipmentResponse>builder()
                        .success(true)
                        .message("Shipment details retrieved successfully")
                        .data(response)
                        .path(request.getRequestURI())
                        .build()
        );
    }

    @PostMapping("/orders/{orderId}/shipment")
    public ResponseEntity<ApiSuccessResponse<ShipmentResponse>> createShipment(
            @PathVariable UUID orderId,
            @Valid @RequestBody CreateShipmentRequest request,
            HttpServletRequest httpRequest
    ) {
        ShipmentResponse response = adminShipmentService.createShipment(orderId, request);

        return ResponseEntity.ok(ApiSuccessResponse.<ShipmentResponse>builder()
                .success(true)
                .message("Shipment created successfully")
                .data(response)
                .path(httpRequest.getRequestURI())
                .build()
        );
    }

    @PatchMapping("/{shipmentId}/status")
    public ResponseEntity<ApiSuccessResponse<ShipmentResponse>> updateShipmentStatus(
            @PathVariable UUID shipmentId,
            @Valid @RequestBody UpdateShipmentStatusRequest request,
            HttpServletRequest httpRequest
    ) {
        ShipmentResponse response = adminShipmentService.updateShipmentStatus(shipmentId, request);

        return ResponseEntity.ok(
                ApiSuccessResponse.<ShipmentResponse>builder()
                        .success(true)
                        .message("Shipment status updated successfully")
                        .data(response)
                        .path(httpRequest.getRequestURI())
                        .build()
        );
    }
}
