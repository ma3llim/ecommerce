package org.ecommerce.auth.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ecommerce.auth.Dtos.request.RegisterUserRequestDto;
import org.ecommerce.auth.Dtos.request.VerifyEmailRequestDto;
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
    public ResponseEntity<?> verifyEmail(@Valid @RequestBody VerifyEmailRequestDto verifyEmailRequest,
                                         HttpServletResponse response,
                                         HttpServletRequest request) {
        UserAndTokenResponseDto userAndTokens = authService.verifyEmail(verifyEmailRequest);

        cookieUtils.setAuthCookies(response, userAndTokens.accessToken(), userAndTokens.refreshToken());

        log.info("Email verification successful, authentication cookies set for userId={}",
                verifyEmailRequest.getUserId());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiSuccessResponse.builder()
                        .success(true)
                        .message("Email verified successfully. You are now logged in.")
                        .data(userAndTokens.userResponseDto())
                        .path(request.getRequestURI())
                        .build()
                );
    }
}
