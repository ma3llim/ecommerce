package org.ecommerce.coupon.controller.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.ecommerce.common.dtos.PageResponse;
import org.ecommerce.common.response.ApiSuccessResponse;
import org.ecommerce.coupon.dtos.admin.request.CreateCouponRequest;
import org.ecommerce.coupon.dtos.admin.request.UpdateCouponRequest;
import org.ecommerce.coupon.dtos.admin.request.UpdateCouponStatusRequest;
import org.ecommerce.coupon.dtos.admin.response.CouponResponse;
import org.ecommerce.coupon.service.admin.AdminCouponService;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/coupons")
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Admin - Coupon Management", description = "APIs for administrators to create, view, update, activate, deactivate, and delete coupons")
public class AdminCouponController {
    private final AdminCouponService couponService;

    @Operation(summary = "Create coupon", description = "Creates a new coupon with discount, usage, and validity configuration.")
    @PostMapping
    public ResponseEntity<ApiSuccessResponse<CouponResponse>> createCoupon(
            @Valid @RequestBody CreateCouponRequest request,
            HttpServletRequest httpRequest
    ) {

        CouponResponse response = couponService.createCoupon(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiSuccessResponse.<CouponResponse>builder()
                        .success(true)
                        .message("Coupon created successfully")
                        .data(response)
                        .path(httpRequest.getRequestURI())
                        .build()
                );
    }

    @Operation(summary = "Get coupons", description = "Retrieves a paginated list of coupons with optional code-based search.")
    @GetMapping
    public ResponseEntity<ApiSuccessResponse<PageResponse<CouponResponse>>> getCoupons(
            @RequestParam(required = false) String search, Pageable pageable,
            HttpServletRequest httpRequest
    ) {
        PageResponse<CouponResponse> response = couponService.getCoupons(search, pageable);

        return ResponseEntity.ok(ApiSuccessResponse.<PageResponse<CouponResponse>>builder()
                .success(true)
                .message("Coupons fetched successfully")
                .data(response)
                .path(httpRequest.getRequestURI())
                .build()
        );
    }

    @Operation(summary = "Get coupon", description = "Retrieves a coupon by its unique identifier.")
    @GetMapping("/{codeId}")
    public ResponseEntity<ApiSuccessResponse<CouponResponse>> getCoupon(
            @PathVariable UUID codeId, HttpServletRequest httpRequest
    ) {
        CouponResponse response = couponService.getCoupon(codeId);

        return ResponseEntity.ok(ApiSuccessResponse.<CouponResponse>builder()
                .success(true)
                .message("Coupon fetched successfully")
                .data(response)
                .path(httpRequest.getRequestURI())
                .build()
        );
    }

    @Operation(summary = "Update coupon", description = "Updates the configuration of an existing coupon.")
    @PutMapping("/{codeId}")
    public ResponseEntity<ApiSuccessResponse<CouponResponse>> updateCoupon(
            @PathVariable UUID codeId, @Valid @RequestBody UpdateCouponRequest request,
            HttpServletRequest httpRequest
    ) {
        CouponResponse response = couponService.updateCoupon(codeId, request);

        return ResponseEntity.ok(ApiSuccessResponse.<CouponResponse>builder()
                .success(true)
                .message("Coupon updated successfully")
                .data(response)
                .path(httpRequest.getRequestURI())
                .build()
        );
    }

    @Operation(summary = "Update coupon status", description = "Activates or deactivates an existing coupon.")
    @PatchMapping("/{codeId}/status")
    public ResponseEntity<ApiSuccessResponse<CouponResponse>> updateStatus(
            @PathVariable UUID codeId, @Valid @RequestBody UpdateCouponStatusRequest request,
            HttpServletRequest httpRequest
    ) {

        CouponResponse response = couponService.updateCouponStatus(codeId, request);

        return ResponseEntity.ok(ApiSuccessResponse.<CouponResponse>builder()
                .success(true)
                .message("Coupon status updated successfully")
                .data(response)
                .path(httpRequest.getRequestURI())
                .build()
        );
    }

    @Operation(summary = "Delete coupon", description = "Permanently deletes an existing coupon.")
    @DeleteMapping("/{codeId}")
    public ResponseEntity<ApiSuccessResponse<Void>> deleteCoupon(
            @PathVariable UUID codeId,
            HttpServletRequest httpRequest
    ) {

        couponService.deleteCoupon(codeId);

        return ResponseEntity.ok(ApiSuccessResponse.<Void>builder()
                .success(true)
                .message("Coupon deleted successfully")
                .data(null)
                .path(httpRequest.getRequestURI())
                .build()
        );
    }
}
