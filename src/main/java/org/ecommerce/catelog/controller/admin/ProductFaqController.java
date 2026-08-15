package org.ecommerce.catelog.controller.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Admin - Product FAQ Management", description = "APIs for administrators to create, view, update, activate, deactivate, and delete product FAQs")
public class ProductFaqController {
    private final ProductFaqService productFaqService;

    @Operation(summary = "Create product FAQ", description = "Creates a new FAQ for the specified product.")
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

    @Operation(summary = "Get all product FAQs", description = "Retrieves all FAQs associated with the specified product.")
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

    @Operation(summary = "Get product FAQ by ID", description = "Retrieves a specific FAQ belonging to the specified product.")
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

    @Operation(summary = "Update product FAQ", description = "Updates the question and answer of an existing product FAQ.")
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

    @Operation(summary = "Update product FAQ status", description = "Activates or deactivates a product FAQ.")
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

    @Operation(
            summary = "Delete product FAQ",
            description = "Deletes an existing FAQ from the specified product."
    )
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
