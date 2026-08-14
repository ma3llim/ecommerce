package org.ecommerce.catelog.controller.admin;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.ecommerce.catelog.dtos.admin.request.CategoryRequestDto;
import org.ecommerce.catelog.dtos.admin.response.CategoryResponse;
import org.ecommerce.catelog.service.admin.CategoryService;
import org.ecommerce.common.dtos.PageResponse;
import org.ecommerce.common.response.ApiSuccessResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/api/v1/admin/categories")
public class CategoryController {
    private final CategoryService categoryService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiSuccessResponse<CategoryResponse>> createCategory(
            @Valid @ModelAttribute CategoryRequestDto newCategory,
            HttpServletRequest request
    ) {
        CategoryResponse categoryResponse = categoryService.createCategory(newCategory);

        return ResponseEntity.ok(
                ApiSuccessResponse.<CategoryResponse>builder()
                        .success(true).message("Created new category successfully")
                        .data(categoryResponse).path(request.getRequestURI()).build()
        );
    }


    @GetMapping
    public ResponseEntity<ApiSuccessResponse<PageResponse<CategoryResponse>>> getAllCategories(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            HttpServletRequest request) {
        PageResponse<CategoryResponse> allCategories = categoryService.getAllCategories(pageable);

        return ResponseEntity.ok(
                ApiSuccessResponse.<PageResponse<CategoryResponse>>builder()
                        .success(true).message("Get all category")
                        .data(allCategories)
                        .path(request.getRequestURI()).build()
        );
    }

    @PutMapping(value = "/{categoryId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiSuccessResponse<CategoryResponse>> updateCategory(@PathVariable UUID categoryId,
                                                                               @Valid @ModelAttribute CategoryRequestDto categoryRequest, HttpServletRequest request) {
        CategoryResponse categoryResponse = categoryService.updateCategory(categoryId, categoryRequest);
        return ResponseEntity.ok(
                ApiSuccessResponse.<CategoryResponse>builder()
                        .success(true).message("updated category successfully")
                        .data(categoryResponse).path(request.getRequestURI()).build()
        );
    }

    @DeleteMapping("/{categoryId}")
    public ResponseEntity<ApiSuccessResponse<Void>> deleteCategory(@PathVariable UUID categoryId, HttpServletRequest request) {
        categoryService.deleteCategory(categoryId);
        return ResponseEntity.ok(ApiSuccessResponse.<Void>builder()
                .success(true)
                .message("category delete successfully")
                .data(null)
                .path(request.getRequestURI()).build()
        );
    }
}
