package org.ecommerce.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.ecommerce.common.response.ApiSuccessResponse;
import org.ecommerce.common.utils.CookieUtils;
import org.ecommerce.user.dtos.request.PasswordRequestDto;
import org.ecommerce.user.dtos.request.UserRequestDto;
import org.ecommerce.user.dtos.response.UserInfoResponseDto;
import org.ecommerce.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

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
    private final CookieUtils cookieUtils;

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

    @PatchMapping("/me")
    public ResponseEntity<ApiSuccessResponse<UserInfoResponseDto>> updateUserInfo(
            @Valid @RequestBody UserRequestDto updateUserInDto,
            Authentication authentication, HttpServletRequest request
    ) {
        UserInfoResponseDto userInfoResponseDto = userService.updateUserInfo(updateUserInDto, authentication);

        return ResponseEntity.ok(ApiSuccessResponse.<UserInfoResponseDto>builder()
                .success(true)
                .message("User info update successfully")
                .data(userInfoResponseDto)
                .path(request.getRequestURI())
                .build());
    }

    @PatchMapping("/me/password")
    public ResponseEntity<ApiSuccessResponse<Void>> updatePassword(
            @Valid @RequestBody PasswordRequestDto passwordRequestDto,
            Authentication authentication, HttpServletRequest request, HttpServletResponse response
    ) {
        userService.updatePassword(passwordRequestDto, authentication);

        cookieUtils.clearAuthCookies(response);
        return ResponseEntity.ok(ApiSuccessResponse.<Void>builder()
                .success(true)
                .message("Password Updated successfully, login to continue")
                .data(null)
                .path(request.getRequestURI())
                .build());
    }
}
