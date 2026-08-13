package org.ecommerce.user.controller.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.ecommerce.auth.enums.AccountStatus;
import org.ecommerce.common.dtos.PageResponse;
import org.ecommerce.common.response.ApiSuccessResponse;
import org.ecommerce.user.dtos.admin.request.adminStatusRequestDto;
import org.ecommerce.user.dtos.admin.response.AdminUserDetailsResponseDto;
import org.ecommerce.user.dtos.admin.response.AdminUserInfoResponseDto;
import org.ecommerce.user.service.admin.AdminUserService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Admin - User Management", description = "Admin APIs for managing and viewing users")
public class AdminUserController {
    private final AdminUserService adminUserService;

    @Operation(summary = "Get all users", description = "Retrieves a paginated list of users with optional search and account status filtering.")
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
                        .message("Users retrieved successfully")
                        .data(userInfoResponseDtos)
                        .path(request.getRequestURI()).build()
        );
    }

    @Operation(summary = "Get user details", description = "Retrieves detailed information about a specific user, including their addresses.")
    @GetMapping("/users/{userId}")
    public ResponseEntity<ApiSuccessResponse<AdminUserDetailsResponseDto>> getUserDetails(
            @PathVariable UUID userId, HttpServletRequest request
    ) {
        AdminUserDetailsResponseDto userDetailsResponseDto = adminUserService.getUserDetails(userId);
        return ResponseEntity.ok(
                ApiSuccessResponse.<AdminUserDetailsResponseDto>builder()
                        .success(true)
                        .message("User details retrieved successfully")
                        .data(userDetailsResponseDto)
                        .path(request.getRequestURI()).build()
        );
    }

    @Operation(summary = "Update user account status", description = "Updates the account status of a user. Admin account status cannot be changed.")
    @PatchMapping("/users/{userId}/status")
    public ResponseEntity<ApiSuccessResponse<AccountStatus>> updateUserStatus(
            @PathVariable UUID userId,
            @RequestBody adminStatusRequestDto statusRequestDto,
            HttpServletRequest request
    ) {
        AccountStatus accountStatus = adminUserService.updateAccountStatus(userId, statusRequestDto);
        return ResponseEntity.ok(
                ApiSuccessResponse.<AccountStatus>builder()
                        .success(true)
                        .message("User account status updated successfully")
                        .data(accountStatus)
                        .path(request.getRequestURI()).build()
        );
    }
}
