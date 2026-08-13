package org.ecommerce.user.controller.user;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.ecommerce.common.response.ApiSuccessResponse;
import org.ecommerce.common.utils.CookieUtils;
import org.ecommerce.common.validator.ValidImage.ValidImage;
import org.ecommerce.user.dtos.user.request.PasswordRequestDto;
import org.ecommerce.user.dtos.user.request.UserRequestDto;
import org.ecommerce.user.dtos.user.response.UserInfoResponseDto;
import org.ecommerce.user.service.user.UserService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
sealed
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
                .message("User information retrieved successfully")
                .data(userInfoResponseDto)
                .path(request.getRequestURI())
                .build());
    }

    @Operation(
            summary = "Update logged-in user's profile",
            description = "Updates the profile information of the currently authenticated user."
    )
    @PatchMapping("/me")
    public ResponseEntity<ApiSuccessResponse<UserInfoResponseDto>> updateUserInfo(
            @Valid @RequestBody UserRequestDto updateUserInDto,
            Authentication authentication, HttpServletRequest request
    ) {
        UserInfoResponseDto userInfoResponseDto = userService.updateUserInfo(updateUserInDto, authentication);

        return ResponseEntity.ok(ApiSuccessResponse.<UserInfoResponseDto>builder()
                .success(true)
                .message("User information updated successfully")
                .data(userInfoResponseDto)
                .path(request.getRequestURI())
                .build());
    }

    @Operation(
            summary = "Update user's password",
            description = "Updates the password of the currently authenticated user and clears the authentication cookies."
    )
    @PatchMapping("/me/password")
    public ResponseEntity<ApiSuccessResponse<Void>> updatePassword(
            @Valid @RequestBody PasswordRequestDto passwordRequestDto,
            Authentication authentication, HttpServletRequest request, HttpServletResponse response
    ) {
        userService.updatePassword(passwordRequestDto, authentication);

        cookieUtils.clearAuthCookies(response);
        return ResponseEntity.ok(ApiSuccessResponse.<Void>builder()
                .success(true)
                .message("Password updated successfully. Please log in again to continue.")
                .data(null)
                .path(request.getRequestURI())
                .build());
    }

    @Operation(
            summary = "Update profile image",
            description = "Uploads or replaces the profile image of the currently authenticated user."
    )
    @PatchMapping(value = "/me/profile-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiSuccessResponse<UserInfoResponseDto>> updateProfileImage(
            @ValidImage @RequestPart("profileImage") MultipartFile profileImage, Authentication authentication,
            HttpServletRequest request
    ) {
        UserInfoResponseDto userInfoResponseDto = userService.updateProfileImage(profileImage, authentication);

        return ResponseEntity.ok(ApiSuccessResponse.<UserInfoResponseDto>builder()
                .success(true)
                .message("Profile image updated successfully")
                .data(userInfoResponseDto)
                .path(request.getRequestURI())
                .build());
    }
}
