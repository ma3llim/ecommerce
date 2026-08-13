package org.ecommerce.catelog.controller.admin;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.ecommerce.catelog.dtos.admin.request.AddCategoryRequestDto;
import org.ecommerce.catelog.dtos.admin.response.CategoryResponse;
import org.ecommerce.catelog.service.admin.AdminCategoryService;
import org.ecommerce.common.dtos.PageResponse;
import org.ecommerce.common.response.ApiSuccessResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/api/v1/admin/categories")
public class AdminCategoryController {
    private final AdminCategoryService adminCategoryService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiSuccessResponse<CategoryResponse>> createCategory(
            @Valid @ModelAttribute AddCategoryRequestDto newCategory,
            HttpServletRequest request
    ) {
        CategoryResponse categoryResponse = adminCategoryService.createCategory(newCategory);

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
        PageResponse<CategoryResponse> allCategories = adminCategoryService.getAllCategories(pageable);

        return ResponseEntity.ok(
                ApiSuccessResponse.<PageResponse<CategoryResponse>>builder()
                        .success(true).message("Get all category")
                        .data(allCategories)
                        .path(request.getRequestURI()).build()
        );
    }
//    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
//    public ResponseEntity<?> updateCategory(@PathVariable UUID id) {
//    }
//
//    @DeleteMapping("/{id}")
//    public ResponseEntity<?> deleteCategory(@PathVariable UUID id) {
//    }
}
