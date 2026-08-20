package org.ecommerce.order.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.ecommerce.common.response.ApiSuccessResponse;
import org.ecommerce.order.dtos.response.UserShipmentResponse;
import org.ecommerce.order.service.ShipmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/shipment")
@PreAuthorize("hasRole('USER')")
@Tag(name = "Shipments", description = "APIs for tracking customer shipments")
public class ShipmentController {
    private final ShipmentService shipmentService;

    @Operation(
            summary = "Get shipment details",
            description = "Retrieves shipment and tracking details for an order belonging to the authenticated customer."
    )
    @GetMapping("/{orderNumber}/shipment")
    public ResponseEntity<ApiSuccessResponse<UserShipmentResponse>> getShipment(
            @PathVariable String orderNumber, Authentication authentication,
            HttpServletRequest request
    ) {
        UserShipmentResponse response = shipmentService.getShipment(orderNumber, authentication);

        return ResponseEntity.ok(ApiSuccessResponse.<UserShipmentResponse>builder()
                .success(true)
                .message("Shipment details retrieved successfully")
                .data(response)
                .path(request.getRequestURI())
                .build()
        );
    }
}
