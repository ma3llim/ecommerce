package org.ecommerce.catelog.controller.admin;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ecommerce.catelog.dtos.admin.request.TagRequest;
import org.ecommerce.catelog.dtos.admin.response.TagResponse;
import org.ecommerce.catelog.service.admin.TagService;
import org.ecommerce.common.dtos.PageResponse;
import org.ecommerce.common.response.ApiSuccessResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/admin/tags")
@RequiredArgsConstructor
public class TagController {
    private final TagService tagService;

    @PostMapping
    public ResponseEntity<ApiSuccessResponse<TagResponse>> createTag(
            @Valid @RequestBody TagRequest requestData, HttpServletRequest request) {
        TagResponse data = tagService.create(requestData);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiSuccessResponse.<TagResponse>builder()
                        .success(true)
                        .message("Tag created successfully.")
                        .data(data)
                        .path(request.getRequestURI()).build()
        );
    }

    @GetMapping
    public ResponseEntity<ApiSuccessResponse<PageResponse<TagResponse>>> getTags(
            @RequestParam(required = false) String search,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            HttpServletRequest request
    ) {
        PageResponse<TagResponse> data = tagService.getAll(search, pageable);

        return ResponseEntity.ok(
                ApiSuccessResponse.<PageResponse<TagResponse>>builder()
                        .success(true)
                        .message("Tags fetched successfully.")
                        .data(data)
                        .path(request.getRequestURI()).build()
        );
    }

    @PutMapping("/{tagId}")
    public ResponseEntity<ApiSuccessResponse<TagResponse>> updateTag(
            @PathVariable UUID tagId, @Valid @RequestBody TagRequest requestData, HttpServletRequest request) {

        TagResponse data = tagService.update(tagId, requestData);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiSuccessResponse.<TagResponse>builder()
                        .success(true)
                        .message("Tag updated successfully.")
                        .data(data)
                        .path(request.getRequestURI()).build()
        );
    }


    @DeleteMapping("/{tagId}")
    public ResponseEntity<ApiSuccessResponse<Void>> deleteTag(
            @PathVariable UUID tagId, HttpServletRequest request) {

        tagService.delete(tagId);

        return ResponseEntity.ok(
                ApiSuccessResponse.<Void>builder()
                        .success(true)
                        .message("Tag deleted successfully.")
                        .data(null).path(request.getRequestURI()).build()
        );
    }
}
