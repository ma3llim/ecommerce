package org.ecommerce.catelog.controller.admin;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.ecommerce.catelog.dtos.admin.request.AddProductRequest;
import org.ecommerce.catelog.dtos.admin.request.AddProductVariants;
import org.ecommerce.catelog.dtos.admin.request.UpdateProduct;
import org.ecommerce.catelog.dtos.admin.response.ProductResponse;
import org.ecommerce.catelog.dtos.admin.response.ProductVariantResponse;
import org.ecommerce.catelog.service.admin.ProductService;
import org.ecommerce.common.response.ApiSuccessResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/api/v1/admin/products")
public class ProductsController {
    private final ProductService productService;

    @PostMapping
    public ResponseEntity<ApiSuccessResponse<ProductResponse>> createProduct(
            @Valid @RequestBody AddProductRequest productRequest,
            HttpServletRequest request) {

        ProductResponse productResponse = productService.createProduct(productRequest);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiSuccessResponse.<ProductResponse>builder()
                        .success(true)
                        .message("Product created successfully")
                        .data(productResponse)
                        .path(request.getRequestURI()).build()
        );
    }

    @PutMapping("/{productId}")
    public ResponseEntity<ApiSuccessResponse<ProductResponse>> updateCategory(
            @PathVariable UUID productId,
            @Valid @RequestBody UpdateProduct productRequest,
            HttpServletRequest request
    ) {
        ProductResponse productResponse = productService.updateProduct(productId, productRequest);

        return ResponseEntity.status(HttpStatus.OK).body(
                ApiSuccessResponse.<ProductResponse>builder()
                        .success(true)
                        .message("Product updated successfully")
                        .data(productResponse)
                        .path(request.getRequestURI()).build()
        );
    }

    @PostMapping("/{productId}/variants")
    public ResponseEntity<ApiSuccessResponse<ProductVariantResponse>> createVariants(
            @PathVariable UUID productId, @Valid @ModelAttribute AddProductVariants addProductVariants,
            HttpServletRequest request) {

        ProductVariantResponse productVariantResponse = productService.addProductVariant(productId, addProductVariants);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiSuccessResponse.<ProductVariantResponse>builder()
                        .success(true)
                        .message("Product variant created successfully")
                        .data(productVariantResponse)
                        .path(request.getRequestURI()).build()
        );
    }
}