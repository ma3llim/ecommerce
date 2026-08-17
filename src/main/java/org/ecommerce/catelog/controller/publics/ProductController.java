package org.ecommerce.catelog.controller.publics;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.ecommerce.catelog.dtos.publics.ProductDetailsResponse;
import org.ecommerce.catelog.dtos.publics.ProductListResponse;
import org.ecommerce.catelog.service.publics.ProductService;
import org.ecommerce.common.dtos.PageResponse;
import org.ecommerce.common.response.ApiSuccessResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/products")
public class ProductController {
    private final ProductService productService;

    @GetMapping
    public ResponseEntity<ApiSuccessResponse<PageResponse<ProductListResponse>>> allProducts(
            @RequestParam(value = "category", required = false) String category,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            HttpServletRequest request
    ) {
        PageResponse<ProductListResponse> responsePageResponse = productService.allProducts(category, pageable);

        return ResponseEntity.ok(
                ApiSuccessResponse.<PageResponse<ProductListResponse>>builder()
                        .success(true)
                        .message("Product fetch successfully")
                        .data(responsePageResponse)
                        .path(request.getRequestURI()).build()
        );
    }

    @GetMapping("/{productSlug}")
    public ResponseEntity<ApiSuccessResponse<ProductDetailsResponse>> getDetailProduct(
            @PathVariable(value = "productSlug", required = true) String productSlug,
            HttpServletRequest request
    ) {
        ProductDetailsResponse productDetailsResponse = productService.productDetails(productSlug);

        return ResponseEntity.ok(
                ApiSuccessResponse.<ProductDetailsResponse>builder()
                        .success(true)
                        .message("Product fetch successfully")
                        .data(productDetailsResponse)
                        .path(request.getRequestURI()).build()
        );
    }
}
