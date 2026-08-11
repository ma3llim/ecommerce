package org.ecommerce.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.ecommerce.common.response.ApiSuccessResponse;
import org.ecommerce.user.dtos.response.UserInfoResponseDto;
import org.ecommerce.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
@Tag(
        name = "User",
        description = "User profile and account management APIs"
)
public class UserController {
    private final UserService userService;

    @Operation(
            summary = "Get logged-in user's profile",
            description = "Retrieves the profile information of the currently authenticated user."
    )
    @GetMapping("/me")
    public ResponseEntity<ApiSuccessResponse<UserInfoResponseDto>> getUserInfo(Authentication authentication, HttpServletRequest request) {
        UserInfoResponseDto userInfoResponseDto = userService.getUserInfo(authentication);

        return ResponseEntity.ok(ApiSuccessResponse.<UserInfoResponseDto>builder()
                .success(true)
                .message("Successfully retrieved user information")
                .data(userInfoResponseDto)
                .path(request.getRequestURI())
                .build());
    }
}
