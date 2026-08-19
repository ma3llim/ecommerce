package org.ecommerce.order.controller;

import com.razorpay.RazorpayException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.ecommerce.common.response.ApiSuccessResponse;
import org.ecommerce.order.dtos.request.CreateOrderRequest;
import org.ecommerce.order.dtos.response.OrderResponse;
import org.ecommerce.order.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
