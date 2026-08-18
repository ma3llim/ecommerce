package org.ecommerce.coupon.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.ecommerce.common.response.ApiSuccessResponse;
import org.ecommerce.coupon.dtos.request.CouponRequest;
import org.ecommerce.coupon.dtos.response.ApplyCouponResponse;
import org.ecommerce.coupon.service.CouponService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/coupons/apply")
@PreAuthorize("hasRole('USER')")
@SecurityRequirement(name = "bearerAuth")
public class CouponController {
    private final CouponService couponService;

    @PostMapping
    public ResponseEntity<ApiSuccessResponse<ApplyCouponResponse>> applyCouponCode(
            @Valid @RequestBody CouponRequest couponRequest,
            Authentication authentication, HttpServletRequest httpRequest
    ) {
        ApplyCouponResponse response = couponService.applyCouponCode(couponRequest.code(), authentication);

        return ResponseEntity.ok(
                ApiSuccessResponse.<ApplyCouponResponse>builder()
                        .success(true)
                        .message("Coupon applied successfully")
                        .data(response)
                        .path(httpRequest.getRequestURI()).build()
        );
    }
}
