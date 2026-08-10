package org.ecommerce.auth.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.ecommerce.auth.Dtos.request.LoginRequestDto;
import org.ecommerce.auth.Dtos.response.UserAndTokenResponseDto;
import org.ecommerce.auth.Dtos.response.UserResponseDto;
import org.ecommerce.auth.entities.RefreshToken;
import org.ecommerce.auth.entities.User;
import org.ecommerce.auth.enums.AccountStatus;
import org.ecommerce.auth.repository.RefreshTokenRepository;
import org.ecommerce.auth.repository.UserRepository;
import org.ecommerce.auth.security.JwtService;
import org.ecommerce.auth.utils.PasswordUtils;
import org.ecommerce.common.config.properties.JwtProperties;
import org.ecommerce.common.exception.ResourceNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.AssertionsKt.assertNotNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AuthServiceLoginTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordUtils passwordUtils;

    @Mock
    private JwtService jwtService;

    @Mock
    private JwtProperties jwtProperties;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private AuthService authService;

    @Test
    @DisplayName("Should throw exception when user does not exist")
    void shouldThrowExceptionWhenUserDoesNotExist() {

        LoginRequestDto loginRequest = new LoginRequestDto("john@example.com", "password123");

        when(userRepository.findByEmail(loginRequest.email())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> authService.login(loginRequest));
    }

    @Test
    @DisplayName("Should throw exception when email is not verified")
    void shouldThrowExceptionWhenEmailIsNotVerified() {

        LoginRequestDto loginRequest = new LoginRequestDto("john@example.com", "password123");

        User user = User.builder()
                .id(UUID.randomUUID())
                .email("john@example.com")
                .password("encoded-password")
                .emailVerified(false)
                .accountStatus(AccountStatus.ACTIVE)
                .build();

        when(userRepository.findByEmail(loginRequest.email())).thenReturn(Optional.of(user));

        assertThrows(BadCredentialsException.class, () -> authService.login(loginRequest));
    }

    @Test
    @DisplayName("Should throw exception when account is not active")
    void shouldThrowExceptionWhenAccountIsNotActive() {

        LoginRequestDto loginRequest = new LoginRequestDto("john@example.com", "password123");

        User user = User.builder()
                .id(UUID.randomUUID())
                .email("john@example.com")
                .password("encoded-password")
                .emailVerified(true)
                .accountStatus(AccountStatus.PENDING)
                .build();

        when(userRepository.findByEmail(loginRequest.email())).thenReturn(Optional.of(user));

        assertThrows(BadCredentialsException.class, () -> authService.login(loginRequest));
    }

    @Test
    @DisplayName("Should throw exception when password is incorrect")
    void shouldThrowExceptionWhenPasswordIsIncorrect() {

        LoginRequestDto loginRequest = new LoginRequestDto("john@example.com", "wrong-password");

        User user = User.builder()
                .id(UUID.randomUUID())
                .email("john@example.com")
                .password("encoded-password")
                .emailVerified(true)
                .accountStatus(AccountStatus.ACTIVE)
                .build();

        when(userRepository.findByEmail(loginRequest.email()))
                .thenReturn(Optional.of(user));

        when(passwordUtils.passwordMatches(
                loginRequest.password(),
                user.getPassword()
        )).thenReturn(false);

        assertThrows(BadCredentialsException.class, () -> authService.login(loginRequest));
    }

    @Test
    @DisplayName("Should login successfully when credentials are valid")
    void shouldLoginSuccessfullyWhenCredentialsAreValid() {
        LoginRequestDto loginRequest = new LoginRequestDto("john@example.com", "password123");

        User user = User.builder()
                .id(UUID.randomUUID())
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .password("encoded-password")
                .emailVerified(true)
                .accountStatus(AccountStatus.ACTIVE)
                .build();

        UserResponseDto userResponseDto = new UserResponseDto(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.isEmailVerified(),
                user.getAccountStatus(),
                user.getRole()
        );

        when(userRepository.findByEmail(loginRequest.email())).thenReturn(Optional.of(user));

        when(passwordUtils.passwordMatches(loginRequest.password(), user.getPassword())).thenReturn(true);

        when(jwtService.generateAccessToken(user)).thenReturn("access-token");

        when(jwtService.generateRefreshToken(eq(user), anyString())).thenReturn("refresh-token");

        when(jwtProperties.getRefreshTokenExpiration()).thenReturn(3600L);

        when(refreshTokenRepository.save(any(RefreshToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(objectMapper.convertValue(eq(user), eq(UserResponseDto.class))).thenReturn(userResponseDto);

        UserAndTokenResponseDto result = authService.login(loginRequest);

        assertNotNull(result);

        assertEquals("access-token", result.accessToken());
        assertEquals("refresh-token", result.refreshToken());

        assertNotNull(user.getLastLoginAt());

        verify(passwordUtils).passwordMatches(loginRequest.password(), user.getPassword());

        verify(jwtService).generateAccessToken(user);

        verify(jwtService).generateRefreshToken(eq(user), anyString());

        verify(refreshTokenRepository).save(any(RefreshToken.class));

        verify(objectMapper).convertValue(user, UserResponseDto.class);
    }
}
