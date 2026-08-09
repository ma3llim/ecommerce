package org.ecommerce.auth.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.ecommerce.auth.Dtos.request.RegisterUserRequestDto;
import org.ecommerce.auth.Dtos.request.VerifyEmailRequestDto;
import org.ecommerce.auth.Dtos.response.RegisterUserResponseDto;
import org.ecommerce.auth.service.AuthService;
import org.ecommerce.common.response.ApiSuccessResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiSuccessResponse<RegisterUserResponseDto>> registerUser(
            @Valid @RequestBody RegisterUserRequestDto requestDto, HttpServletRequest request) {
        RegisterUserResponseDto registerUserResponseDto = authService.registerUser(requestDto);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiSuccessResponse.<RegisterUserResponseDto>builder()
                        .success(true)
                        .message("User registered successfully. We have sent an OTP to your email for verification.")
                        .data(registerUserResponseDto)
                        .path(request.getRequestURI())
                        .build()
                );
    }

    @PostMapping("/verify-email")
    public void verifyEmail(@Valid @RequestBody VerifyEmailRequestDto verifyEmailRequest) {
        authService.verifyEmail(verifyEmailRequest);
    }
}
