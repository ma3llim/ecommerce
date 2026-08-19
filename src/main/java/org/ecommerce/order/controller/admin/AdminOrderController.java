package org.ecommerce.order.controller.admin;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.ecommerce.common.dtos.PageResponse;
import org.ecommerce.common.response.ApiSuccessResponse;
import org.ecommerce.order.dtos.admin.request.UpdateOrderStatusRequest;
import org.ecommerce.order.dtos.response.OrderDetailResponse;
import org.ecommerce.order.dtos.response.OrderResponse;
import org.ecommerce.order.service.admin.AdminOrderService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/orders")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminOrderController {
    private final AdminOrderService adminOrderService;

    @GetMapping
    public ResponseEntity<ApiSuccessResponse<PageResponse<OrderResponse>>> getAllOrders(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String orderStatus,
            @RequestParam(required = false) String paymentStatus,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            HttpServletRequest request
    ) {
        PageResponse<OrderResponse> response = adminOrderService.getAllOrders(search, orderStatus, paymentStatus, pageable);

        return ResponseEntity.ok(
                ApiSuccessResponse.<PageResponse<OrderResponse>>builder()
                        .success(true)
                        .message("Orders retrieved successfully")
                        .data(response)
                        .path(request.getRequestURI())
                        .build()
        );
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<ApiSuccessResponse<OrderDetailResponse>> getOrderDetails(
            @PathVariable UUID orderId,
            HttpServletRequest request
    ) {
        OrderDetailResponse response = adminOrderService.getOrderDetails(orderId);

        return ResponseEntity.ok(
                ApiSuccessResponse.<OrderDetailResponse>builder()
                        .success(true)
                        .message("Order details retrieved successfully")
                        .data(response)
                        .path(request.getRequestURI())
                        .build()
        );
    }


    @PatchMapping("/{orderId}/status")
    public ResponseEntity<ApiSuccessResponse<OrderResponse>> updateOrderStatus(
            @PathVariable UUID orderId,
            @Valid @RequestBody UpdateOrderStatusRequest request, HttpServletRequest httpRequest
    ) {
        OrderResponse response = adminOrderService.updateOrderStatus(orderId, request.status());

        return ResponseEntity.ok(
                ApiSuccessResponse.<OrderResponse>builder()
                        .success(true)
                        .message("Order status updated successfully")
                        .data(response)
                        .path(httpRequest.getRequestURI())
                        .build()
        );
    }

    @PatchMapping("/{orderId}/cancel")
    public ResponseEntity<ApiSuccessResponse<OrderResponse>> cancelOrder(
            @PathVariable UUID orderId,
            HttpServletRequest request
    ) {
        OrderResponse response = adminOrderService.cancelOrder(orderId);

        return ResponseEntity.ok(
                ApiSuccessResponse.<OrderResponse>builder()
                        .success(true)
                        .message("Order cancelled successfully")
                        .data(response)
                        .path(request.getRequestURI())
                        .build()
        );
    }
}