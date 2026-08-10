package org.ecommerce.auth.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ecommerce.auth.Dtos.request.*;
import org.ecommerce.auth.Dtos.response.UserAndTokenResponseDto;
import org.ecommerce.auth.Dtos.response.UserResponseDto;
import org.ecommerce.auth.service.AuthService;
import org.ecommerce.auth.utils.CookieUtils;
import org.ecommerce.common.response.ApiSuccessResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final CookieUtils cookieUtils;

    @PostMapping("/register")
    public ResponseEntity<ApiSuccessResponse<UserResponseDto>> registerUser(
            @Valid @RequestBody RegisterUserRequestDto requestDto, HttpServletRequest request) {
        UserResponseDto userResponseDto = authService.registerUser(requestDto);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiSuccessResponse.<UserResponseDto>builder()
                        .success(true)
                        .message("User registered successfully. We have sent an OTP to your email for verification.")
                        .data(userResponseDto)
                        .path(request.getRequestURI())
                        .build()
                );
    }

    @PostMapping("/verify-email")
    public ResponseEntity<ApiSuccessResponse<UserResponseDto>> verifyEmail(
            @Valid @RequestBody VerifyEmailRequestDto verifyEmailRequest,
            HttpServletResponse response,
            HttpServletRequest request
    ) {
        UserAndTokenResponseDto userAndTokens = authService.verifyEmail(verifyEmailRequest);
        cookieUtils.setAuthCookies(response, userAndTokens.accessToken(), userAndTokens.refreshToken());

        log.info("Email verification successful, authentication cookies set for userId={}", verifyEmailRequest.getUserId());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiSuccessResponse.<UserResponseDto>builder()
                        .success(true)
                        .message("Email verified successfully. You are now logged in.")
                        .data(userAndTokens.userResponseDto())
                        .path(request.getRequestURI())
                        .build()
                );
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<ApiSuccessResponse<Void>> resendVerification(
            @Valid @RequestBody ResendVerificationRequestDto requestDto,
            HttpServletRequest request) {
        authService.resendVerification(requestDto.userId());
        return ResponseEntity.ok(
                ApiSuccessResponse.<Void>builder()
                        .success(true)
                        .message("Verification OTP resent successfully")
                        .data(null)
                        .path(request.getRequestURI())
                        .build()
        );
    }

    @PostMapping("/login")
    public ResponseEntity<ApiSuccessResponse<UserResponseDto>> login(
            @Valid @RequestBody LoginRequestDto loginData,
            HttpServletResponse response,
            HttpServletRequest request
    ) {
        UserAndTokenResponseDto userAndToken = authService.login(loginData);

        cookieUtils.setAuthCookies(response, userAndToken.accessToken(), userAndToken.refreshToken());
        log.info("Login successful, authentication cookies set for userId={}", userAndToken.userResponseDto().getId());

        return ResponseEntity.ok()
                .body(ApiSuccessResponse.<UserResponseDto>builder()
                        .success(true)
                        .message("Login successful")
                        .data(userAndToken.userResponseDto())
                        .path(request.getRequestURI())
                        .build()
                );
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<ApiSuccessResponse<UserResponseDto>> refreshToken(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        String refreshToken = cookieUtils.getRefreshToken(request);
        UserAndTokenResponseDto userAndTokenResponseDto = authService.refreshToken(refreshToken);
        cookieUtils.setAuthCookies(response, userAndTokenResponseDto.accessToken(), userAndTokenResponseDto.refreshToken());

        log.info("Authentication tokens refreshed successfully, cookies updated for userId={}", userAndTokenResponseDto.userResponseDto().getId());

        return ResponseEntity.ok()
                .body(ApiSuccessResponse.<UserResponseDto>builder()
                        .success(true)
                        .message("Refresh Token Successfully")
                        .data(userAndTokenResponseDto.userResponseDto())
                        .path(request.getRequestURI())
                        .build()
                );
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiSuccessResponse<Void>> logout(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        String refreshToken = cookieUtils.getRefreshToken(request);
        authService.logout(refreshToken);

        cookieUtils.clearAuthCookies(response);

        return ResponseEntity.ok(
                ApiSuccessResponse.<Void>builder()
                        .success(true)
                        .message("Logout successful")
                        .path(request.getRequestURI())
                        .build()
        );
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiSuccessResponse<UUID>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequestDto requestDto,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        UUID userId = authService.forgotPassword(requestDto.email());
        return ResponseEntity.ok(
                ApiSuccessResponse.<UUID>builder()
                        .success(true)
                        .message("Password reset OTP sent successfully")
                        .data(userId)
                        .path(request.getRequestURI())
                        .build()
        );
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiSuccessResponse<Void>> resetPassword(
            @Valid @RequestBody ResetPasswordRequestDto resetPasswordDto,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        authService.resetPassword(resetPasswordDto);
        cookieUtils.clearAuthCookies(response);

        return ResponseEntity.ok(
                ApiSuccessResponse.<Void>builder()
                        .success(true)
                        .message("Password reset successfully")
                        .path(request.getRequestURI()).build()
        );
    }
}
