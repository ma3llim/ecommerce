package org.ecommerce.user.controller.admin;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.ecommerce.auth.enums.AccountStatus;
import org.ecommerce.common.dtos.PageResponse;
import org.ecommerce.common.response.ApiSuccessResponse;
import org.ecommerce.user.dtos.admin.AdminUserInfoResponseDto;
import org.ecommerce.user.service.admin.AdminUserService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {
    private final AdminUserService adminUserService;

    @GetMapping("/users")
    public ResponseEntity<ApiSuccessResponse<PageResponse<AdminUserInfoResponseDto>>> getAllUses(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) AccountStatus accountStatus,

            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable,
            HttpServletRequest request
    ) {
        PageResponse<AdminUserInfoResponseDto> userInfoResponseDtos = adminUserService.getAllUsers(search, accountStatus, pageable);
        return ResponseEntity.ok(
                ApiSuccessResponse.<PageResponse<AdminUserInfoResponseDto>>builder()
                        .success(true)
                        .message("Fetch All Users")
                        .data(userInfoResponseDtos)
                        .path(request.getRequestURI()).build()
        );
    }
}
