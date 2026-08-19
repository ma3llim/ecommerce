package org.ecommerce.order.controller;

import com.razorpay.RazorpayException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.ecommerce.common.dtos.PageResponse;
import org.ecommerce.common.response.ApiSuccessResponse;
import org.ecommerce.order.dtos.request.CreateOrderRequest;
import org.ecommerce.order.dtos.response.OrderDetailResponse;
import org.ecommerce.order.dtos.response.OrderListResponse;
import org.ecommerce.order.dtos.response.OrderResponse;
import org.ecommerce.order.service.OrderService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@PreAuthorize("hasRole('USER')")
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "APIs for creating and managing customer orders")
public class OrderController {
    private final OrderService orderService;

    @Operation(summary = "Create order", description = "Creates a new order from the authenticated user's cart and initiates the selected payment method.")
    @PostMapping
    public ResponseEntity<ApiSuccessResponse<OrderResponse>> createOrder(
            @Valid @RequestBody CreateOrderRequest request, Authentication authentication,
            HttpServletRequest httpRequest
    ) throws RazorpayException {

        OrderResponse response = orderService.createOrder(request, authentication);

        return ResponseEntity.ok(
                ApiSuccessResponse.<OrderResponse>builder()
                        .success(true)
                        .message("Order created successfully.")
                        .data(response)
                        .path(httpRequest.getRequestURI())
                        .build()
        );
    }

    @GetMapping
    public ResponseEntity<ApiSuccessResponse<PageResponse<OrderListResponse>>> getOrders(
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            Authentication authentication,
            HttpServletRequest httpRequest) {
        PageResponse<OrderListResponse> pageResponse = orderService.getOrders(pageable, authentication);

        return ResponseEntity.ok(
                ApiSuccessResponse.<PageResponse<OrderListResponse>>builder()
                        .success(true)
                        .message("Orders retried successfully.")
                        .data(pageResponse)
                        .path(httpRequest.getRequestURI())
                        .build()
        );
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<ApiSuccessResponse<OrderDetailResponse>> getOrderDetail(
            @PathVariable UUID orderId, Authentication authentication,
            HttpServletRequest httpRequest
    ) {
        OrderDetailResponse orderDetail = orderService.getOrderDetail(orderId, authentication);

        return ResponseEntity.ok(
                ApiSuccessResponse.<OrderDetailResponse>builder()
                        .success(true)
                        .message("Order details retried successfully.")
                        .data(orderDetail)
                        .path(httpRequest.getRequestURI())
                        .build()
        );
    }
//
//    @PatchMapping("/{orderId}")
//    public ResponseEntity<ApiSuccessResponse<OrderResponse>> cancelOrder(
//            @PathVariable UUID orderId, Authentication authentication,
//            HttpServletRequest httpRequest
//    ) {
//        OrderResponse pageResponse = orderService.getOrderDetail(orderId, authentication);
//
//        return ResponseEntity.ok(
//                ApiSuccessResponse.<OrderResponse>builder()
//                        .success(true)
//                        .message("Order retried successfully.")
//                        .data(pageResponse)
//                        .path(httpRequest.getRequestURI())
//                        .build()
//        );
//    }
}
