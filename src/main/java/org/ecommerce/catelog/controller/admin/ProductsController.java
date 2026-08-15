package org.ecommerce.catelog.controller.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.ecommerce.catelog.dtos.admin.request.*;
import org.ecommerce.catelog.dtos.admin.response.ProductDetailsResponse;
import org.ecommerce.catelog.dtos.admin.response.ProductResponse;
import org.ecommerce.catelog.dtos.admin.response.ProductVariantImageResponse;
import org.ecommerce.catelog.dtos.admin.response.ProductVariantResponse;
import org.ecommerce.catelog.service.admin.ProductService;
import org.ecommerce.common.dtos.PageResponse;
import org.ecommerce.common.response.ApiSuccessResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/api/v1/admin/products")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Admin - Product Management", description = "APIs for administrators to manage products, variants, and variant images")
public class ProductsController {
    private final ProductService productService;

    @Operation(summary = "Get all products", description = "Retrieves a paginated list of products sorted by creation date in descending order.")
    @GetMapping
    public ResponseEntity<ApiSuccessResponse<PageResponse<ProductResponse>>> getProducts(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable, HttpServletRequest request) {
        PageResponse<ProductResponse> products = productService.getProducts(pageable);

        return ResponseEntity.ok(
                ApiSuccessResponse.<PageResponse<ProductResponse>>builder()
                        .success(true)
                        .message("Products retrieved successfully")
                        .data(products)
                        .path(request.getRequestURI())
                        .build()
        );
    }

    @Operation(summary = "Get product details", description = "Retrieves detailed information about a product, including its category, variants, variant images, specifications, and current status.")
    @GetMapping("/{productId}")
    public ResponseEntity<ApiSuccessResponse<ProductDetailsResponse>> getProduct(
            @PathVariable UUID productId, HttpServletRequest request) {
        ProductDetailsResponse productDetailsResponse = productService.getProduct(productId);

        return ResponseEntity.ok(
                ApiSuccessResponse.<ProductDetailsResponse>builder()
                        .success(true)
                        .message("Product details retrieved successfully")
                        .data(productDetailsResponse)
                        .path(request.getRequestURI())
                        .build()
        );
    }

    @Operation(summary = "Create product", description = "Creates a new product under the specified category. The product is created with an inactive/unpublished status until it is ready to be published.")
    @PostMapping
    public ResponseEntity<ApiSuccessResponse<ProductResponse>> createProduct(
            @Valid @RequestBody AddProductRequest productRequest,
            HttpServletRequest request) {

        ProductResponse productResponse = productService.createProduct(productRequest);

        return ResponseEntity.ok(
                ApiSuccessResponse.<ProductResponse>builder()
                        .success(true)
                        .message("Product created successfully")
                        .data(productResponse)
                        .path(request.getRequestURI()).build()
        );
    }

    @Operation(summary = "Update product", description = "Updates the details of an existing product, including its category, name, description, and specifications.")
    @PutMapping("/{productId}")
    public ResponseEntity<ApiSuccessResponse<ProductResponse>> updateProduct(
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

    @Operation(summary = "Update product status", description = "Updates the visibility status of an existing product. The product is marked as published when the ACTIVE status is provided.")
    @PatchMapping("/{productId}/status")
    public ResponseEntity<ApiSuccessResponse<ProductResponse>> updateProductStatus(
            @PathVariable UUID productId, @Valid @RequestBody UpdateProductStatus productStatus,
            HttpServletRequest request
    ) {
        ProductResponse productResponse = productService.updateProductStatus(productId, productStatus.status());
        return ResponseEntity.status(HttpStatus.OK).body(
                ApiSuccessResponse.<ProductResponse>builder().success(true)
                        .message("Product status updated successfully")
                        .data(productResponse).path(request.getRequestURI()).build()
        );
    }

    @Operation(summary = "Create product variant", description = "Creates a new variant for the specified product, including its price, stock quantity, attributes, and optional images.")
    @PostMapping(value = "/{productId}/variants", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiSuccessResponse<ProductVariantResponse>> createVariants(
            @PathVariable UUID productId, @Valid @ModelAttribute AddProductVariants addProductVariants,
            HttpServletRequest request) {

        ProductVariantResponse productVariantResponse = productService.addProductVariant(productId, addProductVariants);

        return ResponseEntity.ok(
                ApiSuccessResponse.<ProductVariantResponse>builder()
                        .success(true)
                        .message("Product variant created successfully")
                        .data(productVariantResponse)
                        .path(request.getRequestURI()).build()
        );
    }

    @Operation(summary = "Update product variant", description = "Updates an existing product variant, including its price, stock quantity, and attributes.")
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

    @Operation(summary = "Delete product variant", description = "Deletes an existing product variant and its associated images.")
    @DeleteMapping("/{productId}/variants/{variantId}")
    public ResponseEntity<ApiSuccessResponse<Void>> deleteVariants(
            @PathVariable UUID productId, @PathVariable UUID variantId,
            HttpServletRequest request
    ) {
        productService.deleteVariant(productId, variantId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(
                ApiSuccessResponse.<Void>builder().success(true)
                        .message("Product variant deleted successfully")
                        .data(null).path(request.getRequestURI()).build()
        );
    }

    @Operation(summary = "Update product variant status", description = "Updates the active status of an existing product variant.")
    @PutMapping("/{productId}/variants/{variantId}/status")
    public ResponseEntity<ApiSuccessResponse<ProductVariantResponse>> updateVariantStatus(
            @PathVariable UUID productId, @PathVariable UUID variantId,
            @Valid @RequestBody UpdateVariantStatus requestData,
            HttpServletRequest request
    ) {
        ProductVariantResponse productVariantResponse = productService.updateVariantStatus(productId, variantId, requestData.status());

        return ResponseEntity.status(HttpStatus.OK).body(
                ApiSuccessResponse.<ProductVariantResponse>builder().success(true)
                        .message("Product variant status updated successfully")
                        .data(productVariantResponse).path(request.getRequestURI()).build()
        );
    }

    @Operation(summary = "Upload product variant images", description = "Uploads one or more images for the specified product variant and returns the uploaded image details.")
    @PostMapping(value = "/{productId}/variants/{variantId}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiSuccessResponse<List<ProductVariantImageResponse>>> uploadsImages(
            @PathVariable UUID productId, @PathVariable UUID variantId,
            @Valid @ModelAttribute AddImages images,
            HttpServletRequest request
    ) {
        List<ProductVariantImageResponse> productVariantImageResponse = productService.uploadsImage(productId, variantId, images);

        return ResponseEntity.status(HttpStatus.OK).body(
                ApiSuccessResponse.<List<ProductVariantImageResponse>>builder().success(true)
                        .message("Product variant images uploaded successfully")
                        .data(productVariantImageResponse).path(request.getRequestURI()).build()
        );
    }

    @Operation(summary = "Replace product variant image", description = "Replaces an existing product variant image with a new image while retaining the image resource.")
    @PutMapping(value = "/{productId}/variants/{variantId}/images/{variantImageId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
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

    @Operation(summary = "Set product variant image as primary", description = "Sets the specified product variant image as the primary image. Any existing primary image for the variant will no longer be primary.")
    @PatchMapping("/{productId}/variants/{variantId}/images/{variantImageId}/primary")
    public ResponseEntity<ApiSuccessResponse<ProductVariantImageResponse>> setVariantImagePrimary(
            @PathVariable UUID productId, @PathVariable UUID variantId, @PathVariable UUID variantImageId,
            HttpServletRequest request
    ) {
        ProductVariantImageResponse productVariantImageResponse = productService.setVariantImagePrimary(
                productId, variantId, variantImageId);

        return ResponseEntity.status(HttpStatus.OK).body(
                ApiSuccessResponse.<ProductVariantImageResponse>builder().success(true)
                        .message("Product variant image set as primary successfully")
                        .data(productVariantImageResponse).path(request.getRequestURI()).build()
        );
    }

    @Operation(summary = "Reorder product variant images", description = "Updates the display order of all images associated with a product variant. All existing image IDs must be included in the requested order.")
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

    @Operation(summary = "Delete product variant image", description = "Deletes an image associated with a product variant. If the deleted image is the primary image, the next available image is promoted to primary.")
    @DeleteMapping("/{productId}/variants/{variantId}/images/{imageVariantId}")
    public ResponseEntity<ApiSuccessResponse<Void>> deleteVariantImage(
            @PathVariable UUID productId, @PathVariable UUID variantId, @PathVariable UUID imageVariantId,
            HttpServletRequest request
    ) {
        productService.deleteVariantImage(productId, variantId, imageVariantId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(
                ApiSuccessResponse.<Void>builder().success(true)
                        .message("Product variant image deleted successfully")
                        .data(null).path(request.getRequestURI()).build()
        );
    }
}