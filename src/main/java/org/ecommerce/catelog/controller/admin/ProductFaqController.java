package org.ecommerce.catelog.controller.admin;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.ecommerce.catelog.dtos.admin.request.ProductFaqCreateRequest;
import org.ecommerce.catelog.dtos.admin.request.ProductFaqStatusRequest;
import org.ecommerce.catelog.dtos.admin.request.ProductFaqUpdateRequest;
import org.ecommerce.catelog.dtos.admin.response.ProductFaqResponse;
import org.ecommerce.catelog.service.admin.ProductFaqService;
import org.ecommerce.common.response.ApiSuccessResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/products/{productId}/faqs")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class ProductFaqController {
    private final ProductFaqService productFaqService;

    @PostMapping
    public ResponseEntity<ApiSuccessResponse<ProductFaqResponse>> createFaq(
            @PathVariable UUID productId,
            @Valid @RequestBody ProductFaqCreateRequest requestData,
            HttpServletRequest request
    ) {
        ProductFaqResponse response = productFaqService.create(productId, requestData);
        return ResponseEntity.ok(
                ApiSuccessResponse.<ProductFaqResponse>builder()
                        .success(true).message("Product FAQ created successfully.")
                        .data(response).path(request.getRequestURI()).build()
        );
    }

    @GetMapping
    public ResponseEntity<ApiSuccessResponse<List<ProductFaqResponse>>> getAllFaqs(
            @PathVariable UUID productId, HttpServletRequest request) {
        List<ProductFaqResponse> response = productFaqService.getAll(productId);

        return ResponseEntity.ok(
                ApiSuccessResponse.<List<ProductFaqResponse>>builder()
                        .success(true).message("Product FAQs fetched successfully.")
                        .data(response).path(request.getRequestURI()).build()
        );
    }

    @GetMapping("/{faqId}")
    public ResponseEntity<ApiSuccessResponse<ProductFaqResponse>> getFaqById(
            @PathVariable UUID productId, @PathVariable UUID faqId,
            HttpServletRequest request
    ) {
        ProductFaqResponse response = productFaqService.getById(productId, faqId);

        return ResponseEntity.ok(
                ApiSuccessResponse.<ProductFaqResponse>builder()
                        .success(true).message("Product FAQ fetched successfully.")
                        .data(response).path(request.getRequestURI()).build()
        );
    }

    @PutMapping("/{faqId}")
    public ResponseEntity<ApiSuccessResponse<ProductFaqResponse>> updateFaq(
            @PathVariable UUID productId,
            @PathVariable UUID faqId,
            @Valid @RequestBody ProductFaqUpdateRequest requestData,
            HttpServletRequest request
    ) {
        ProductFaqResponse response = productFaqService.update(productId, faqId, requestData);

        return ResponseEntity.ok(
                ApiSuccessResponse.<ProductFaqResponse>builder()
                        .success(true).message("Product FAQ updated successfully.")
                        .data(response).path(request.getRequestURI()).build()
        );
    }

    @PatchMapping("/{faqId}/status")
    public ResponseEntity<ApiSuccessResponse<ProductFaqResponse>> updateFaqStatus(
            @PathVariable UUID productId,
            @PathVariable UUID faqId,
            @Valid @RequestBody ProductFaqStatusRequest requestData,
            HttpServletRequest request
    ) {
        ProductFaqResponse response = productFaqService.updateStatus(productId, faqId, requestData);

        String message = requestData.active() ? "Product FAQ activated successfully." : "Product FAQ deactivated successfully.";

        return ResponseEntity.ok(
                ApiSuccessResponse.<ProductFaqResponse>builder().success(true).message(message)
                        .data(response).path(request.getRequestURI()).build()
        );
    }

    @DeleteMapping("/{faqId}")
    public ResponseEntity<ApiSuccessResponse<Void>> deleteFaq(@PathVariable UUID productId, @PathVariable UUID faqId, HttpServletRequest request) {
        productFaqService.delete(productId, faqId);
        return ResponseEntity.ok(
                ApiSuccessResponse.<Void>builder()
                        .success(true).message("Product FAQ deleted successfully")
                        .data(null).path(request.getRequestURI()).build()
        );

    }
}
