package org.ecommerce.catelog.controller.admin;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.ecommerce.catelog.dtos.admin.request.AddProductRequest;
import org.ecommerce.catelog.dtos.admin.response.ProductResponse;
import org.ecommerce.catelog.service.admin.ProductService;
import org.ecommerce.common.response.ApiSuccessResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
