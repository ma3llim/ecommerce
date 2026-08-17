package org.ecommerce.catelog.controller.publics;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.ecommerce.catelog.dtos.admin.response.CategoryResponse;
import org.ecommerce.catelog.service.publics.CategoryService;
import org.ecommerce.common.dtos.PageResponse;
import org.ecommerce.common.response.ApiSuccessResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/categories")
public class CategoryController {
    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<ApiSuccessResponse<PageResponse<CategoryResponse>>> getAllCategories(
            Pageable pageable, HttpServletRequest request) {
        PageResponse<CategoryResponse> data = categoryService.getAllCategories(pageable);

        return ResponseEntity.ok(
                ApiSuccessResponse.<PageResponse<CategoryResponse>>builder()
                        .success(true)
                        .message("Categories fetched successfully.")
                        .data(data)
                        .path(request.getRequestURI())
                        .build()
        );
    }

    @GetMapping("/{slug}")
    public ResponseEntity<ApiSuccessResponse<CategoryResponse>> getCategoryBySlug(
            @PathVariable String slug, HttpServletRequest request) {
        CategoryResponse data = categoryService.getCategoryBySlug(slug);

        return ResponseEntity.ok(
                ApiSuccessResponse.<CategoryResponse>builder()
                        .success(true)
                        .message("Categories fetched successfully.")
                        .data(data)
                        .path(request.getRequestURI())
                        .build()
        );
    }
}
