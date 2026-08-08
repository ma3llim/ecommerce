package org.ecommerce.auth.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.ecommerce.auth.Dtos.request.RegisterUserRequestDto;
import org.ecommerce.auth.Dtos.response.RegisterUserResponseDto;
import org.ecommerce.auth.service.AuthService;
import org.ecommerce.common.response.ApiSuccessResponse;
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
            @Valid @RequestBody RegisterUserRequestDto requestDto) {
        RegisterUserResponseDto registerUserResponseDto = authService.registerUser(requestDto);

        System.out.println(registerUserResponseDto);

        return ResponseEntity.ok(null);
    }
}
