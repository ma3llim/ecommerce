package org.ecommerce.catelog.controller.admin;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.ecommerce.catelog.dtos.admin.request.*;
import org.ecommerce.catelog.dtos.admin.response.ProductResponse;
import org.ecommerce.catelog.dtos.admin.response.ProductVariantImageResponse;
import org.ecommerce.catelog.dtos.admin.response.ProductVariantResponse;
import org.ecommerce.catelog.service.admin.ProductService;
import org.ecommerce.common.response.ApiSuccessResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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

    @PutMapping("/{productId}/variants/{variantId}")
    public ResponseEntity<ApiSuccessResponse<ProductVariantResponse>> updateVariants(
            @PathVariable UUID productId, @PathVariable UUID variantId,
            @Valid @RequestBody UpdateProductVariant productVariant,
            HttpServletRequest request
    ) {
        ProductVariantResponse productVariantResponse = productService
                .updateProductVariant(productId, variantId, productVariant);

        return ResponseEntity.status(HttpStatus.OK).body(
                ApiSuccessResponse.<ProductVariantResponse>builder().success(true)
                        .message("Product variant updated successfully")
                        .data(productVariantResponse).path(request.getRequestURI()).build()
        );
    }

    @PostMapping("/{productId}/variants/{variantId}/images")
    public ResponseEntity<ApiSuccessResponse<List<ProductVariantImageResponse>>> uploadsImages(
            @PathVariable UUID productId, @PathVariable UUID variantId,
            @Valid @ModelAttribute AddImages images,
            HttpServletRequest request
    ) {
        List<ProductVariantImageResponse> productVariantImageResponse = productService.uploadsImage(productId, variantId, images);

        return ResponseEntity.status(HttpStatus.OK).body(
                ApiSuccessResponse.<List<ProductVariantImageResponse>>builder().success(true)
                        .message("Product variant image added successfully")
                        .data(productVariantImageResponse).path(request.getRequestURI()).build()
        );
    }

    @PutMapping("/{productId}/variants/{variantId}/images/{variantImageId}")
    public ResponseEntity<ApiSuccessResponse<ProductVariantImageResponse>> uploadsImages(
            @PathVariable UUID productId, @PathVariable UUID variantId, @PathVariable UUID variantImageId,
            @Valid @ModelAttribute ReplaceImage image, HttpServletRequest request
    ) {
        ProductVariantImageResponse productVariantImageResponse = productService.replaceImage(
                productId, variantId, variantImageId, image);

        return ResponseEntity.status(HttpStatus.OK).body(
                ApiSuccessResponse.<ProductVariantImageResponse>builder().success(true)
                        .message("Product variant image replaced successfully")
                        .data(productVariantImageResponse).path(request.getRequestURI()).build()
        );
    }

    @PatchMapping("/{productId}/variants/{variantId}/images/{variantImageId}/primary")
    public ResponseEntity<ApiSuccessResponse<ProductVariantImageResponse>> setVariantImagePrimary(
            @PathVariable UUID productId, @PathVariable UUID variantId, @PathVariable UUID variantImageId,
            HttpServletRequest request
    ) {
        ProductVariantImageResponse productVariantImageResponse = productService.setVariantImagePrimary(
                productId, variantId, variantImageId);

        return ResponseEntity.status(HttpStatus.OK).body(
                ApiSuccessResponse.<ProductVariantImageResponse>builder().success(true)
                        .message("Product variant image replaced successfully")
                        .data(productVariantImageResponse).path(request.getRequestURI()).build()
        );
    }

    @PutMapping("/{productId}/variants/{variantId}/images/reorder")
    public ResponseEntity<ApiSuccessResponse<List<ProductVariantImageResponse>>> reorderImages(
            @PathVariable UUID productId, @PathVariable UUID variantId,
            @Valid @RequestBody ReorderImages reorderImages, HttpServletRequest request
    ) {
        List<ProductVariantImageResponse> productVariantImageResponseList = productService.reorderImages(productId, variantId, reorderImages);

        return ResponseEntity.status(HttpStatus.OK).body(
                ApiSuccessResponse.<List<ProductVariantImageResponse>>builder().success(true)
                        .message("Product variant image order updated successfully")
                        .data(productVariantImageResponseList).path(request.getRequestURI()).build()
        );
    }

    @DeleteMapping("/{productId}/variants/{variantId}/images/{imageVariantId}")
    public ResponseEntity<ApiSuccessResponse<Void>> deleteVariantImage(
            @PathVariable UUID productId, @PathVariable UUID variantId, @PathVariable UUID imageVariantId,
            HttpServletRequest request
    ) {
        productService.deleteVariantImage(productId, variantId, imageVariantId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(
                ApiSuccessResponse.<Void>builder().success(true)
                        .message("Product variant image delete successfully")
                        .data(null).path(request.getRequestURI()).build()
        );
    }
}