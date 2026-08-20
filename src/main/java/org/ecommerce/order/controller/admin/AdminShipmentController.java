package org.ecommerce.order.controller.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Admin - Shipments", description = "Admin APIs for managing shipments and shipment status")
public class AdminShipmentController {
    private final AdminShipmentService adminShipmentService;

    @Operation(
            summary = "Get all shipments",
            description = "Retrieves a paginated list of shipments with optional search, status, courier, and date-range filters."
    )
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

    @Operation(
            summary = "Get shipment details",
            description = "Retrieves complete shipment details including tracking information for the specified shipment."
    )
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

    @Operation(summary = "Create shipment", description = "Creates a shipment for the specified order.")
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

    @Operation(
            summary = "Update shipment status",
            description = "Updates the status of an existing shipment and records the corresponding tracking event."
    )
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
