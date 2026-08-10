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
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceLoginTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordUtils passwordUtils;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    @Spy
    private AuthService authService;
    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private JwtProperties jwtProperties;


    @Test
    @DisplayName("Should throw exception when user does not exist")
    void login_whenUserDoesNotExist_throwsResourceNotFoundException() {
        LoginRequestDto loginData = new LoginRequestDto(
                "john@example.com",
                "password123"
        );

        when(userRepository.findByEmail(loginData.email())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> authService.login(loginData));

        verify(userRepository).findByEmail(loginData.email());

        verifyNoInteractions(passwordUtils);
        verifyNoInteractions(jwtService);
        verifyNoInteractions(refreshTokenRepository);
    }

    @Test
    @DisplayName("Should throw exception when email is not verified")
    void login_whenEmailIsNotVerified_throwsBadCredentialsException() {

        LoginRequestDto loginData = new LoginRequestDto("john@example.com", "password123");

        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail(loginData.email());
        user.setEmailVerified(false);
        user.setAccountStatus(AccountStatus.ACTIVE);
        user.setPassword("encoded-password");

        when(userRepository.findByEmail(loginData.email()))
                .thenReturn(Optional.of(user));

        assertThrows(
                BadCredentialsException.class,
                () -> authService.login(loginData)
        );

        verify(userRepository)
                .findByEmail(loginData.email());

        verifyNoInteractions(passwordUtils);
        verifyNoInteractions(jwtService);
        verifyNoInteractions(refreshTokenRepository);
    }

    @Test
    @DisplayName("Should throw exception when account is not active")
    void login_whenAccountIsNotActive_throwsBadCredentialsException() {

        LoginRequestDto loginData = new LoginRequestDto(
                "john@example.com",
                "password123"
        );

        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail(loginData.email());
        user.setEmailVerified(true);
        user.setAccountStatus(AccountStatus.PENDING);
        user.setPassword("encoded-password");

        when(userRepository.findByEmail(loginData.email()))
                .thenReturn(Optional.of(user));

        assertThrows(
                BadCredentialsException.class,
                () -> authService.login(loginData)
        );

        verify(userRepository)
                .findByEmail(loginData.email());

        verifyNoInteractions(passwordUtils);
        verifyNoInteractions(jwtService);
        verifyNoInteractions(refreshTokenRepository);
    }

    @Test
    @DisplayName("Should throw exception when password is incorrect")
    void login_whenPasswordIsIncorrect_throwsBadCredentialsException() {

        LoginRequestDto loginData = new LoginRequestDto(
                "john@example.com",
                "wrong-password"
        );

        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail(loginData.email());
        user.setEmailVerified(true);
        user.setAccountStatus(AccountStatus.ACTIVE);
        user.setPassword("encoded-password");

        when(userRepository.findByEmail(loginData.email()))
                .thenReturn(Optional.of(user));

        when(passwordUtils.passwordMatches(
                loginData.password(),
                user.getPassword()
        )).thenReturn(false);

        assertThrows(
                BadCredentialsException.class,
                () -> authService.login(loginData)
        );

        verify(passwordUtils)
                .passwordMatches(
                        loginData.password(),
                        user.getPassword()
                );

        verifyNoInteractions(jwtService);
        verifyNoInteractions(refreshTokenRepository);
    }

    @Test
    @DisplayName("Should update last login, generate tokens, and return response for valid credentials")
    void login_whenCredentialsAreValid_updatesLastLoginGeneratesTokensAndReturnsResponse() {

        LoginRequestDto loginData = new LoginRequestDto(
                "john@example.com",
                "password123"
        );

        UUID userId = UUID.randomUUID();

        String accessToken = "access-token";
        String refreshToken = "refresh-token";

        User user = new User();
        user.setId(userId);
        user.setEmail(loginData.email());
        user.setPassword("encoded-password");
        user.setEmailVerified(true);
        user.setAccountStatus(AccountStatus.ACTIVE);

        UserResponseDto userResponse =
                mock(UserResponseDto.class);

        when(userRepository.findByEmail(loginData.email()))
                .thenReturn(Optional.of(user));

        when(passwordUtils.passwordMatches(
                loginData.password(),
                user.getPassword()
        )).thenReturn(true);

        when(jwtService.generateAccessToken(user))
                .thenReturn(accessToken);

        when(jwtService.generateRefreshToken(
                eq(user),
                anyString()
        )).thenReturn(refreshToken);

        when(jwtProperties.getRefreshTokenExpiration())
                .thenReturn(604800L);

        when(objectMapper.convertValue(
                user,
                UserResponseDto.class
        )).thenReturn(userResponse);

        UserAndTokenResponseDto result =
                authService.login(loginData);

        assertNotNull(result);

        assertEquals(
                accessToken,
                result.accessToken()
        );

        assertEquals(
                refreshToken,
                result.refreshToken()
        );

        assertSame(
                userResponse,
                result.userResponseDto()
        );

        assertNotNull(user.getLastLoginAt());

        verify(userRepository)
                .findByEmail(loginData.email());

        verify(passwordUtils)
                .passwordMatches(
                        loginData.password(),
                        user.getPassword()
                );

        verify(jwtService)
                .generateAccessToken(user);

        verify(jwtService)
                .generateRefreshToken(
                        eq(user),
                        anyString()
                );

        verify(refreshTokenRepository)
                .save(any(RefreshToken.class));

        verify(objectMapper)
                .convertValue(
                        user,
                        UserResponseDto.class
                );
    }

    @Test
    @DisplayName("Should save refresh token with correct details for valid credentials")
    void login_whenCredentialsAreValid_savesRefreshTokenWithCorrectDetails() {

        LoginRequestDto loginData = new LoginRequestDto(
                "john@example.com",
                "password123"
        );

        UUID userId = UUID.randomUUID();

        String accessToken = "access-token";
        String refreshToken = "refresh-token";

        User user = new User();
        user.setId(userId);
        user.setEmail(loginData.email());
        user.setPassword("encoded-password");
        user.setEmailVerified(true);
        user.setAccountStatus(AccountStatus.ACTIVE);

        when(userRepository.findByEmail(loginData.email()))
                .thenReturn(Optional.of(user));

        when(passwordUtils.passwordMatches(
                loginData.password(),
                user.getPassword()
        )).thenReturn(true);

        when(jwtService.generateAccessToken(user))
                .thenReturn(accessToken);

        when(jwtService.generateRefreshToken(
                eq(user),
                anyString()
        )).thenReturn(refreshToken);

        when(jwtProperties.getRefreshTokenExpiration())
                .thenReturn(604800L);

        when(objectMapper.convertValue(
                user,
                UserResponseDto.class
        )).thenReturn(mock(UserResponseDto.class));

        authService.login(loginData);

        ArgumentCaptor<RefreshToken> refreshTokenCaptor =
                ArgumentCaptor.forClass(RefreshToken.class);

        verify(refreshTokenRepository)
                .save(refreshTokenCaptor.capture());

        RefreshToken savedToken =
                refreshTokenCaptor.getValue();

        assertNotNull(savedToken.getId());

        assertEquals(
                userId,
                savedToken.getUserId()
        );

        assertEquals(
                refreshToken,
                savedToken.getRefreshToken()
        );

        assertNotNull(
                savedToken.getExpiresAt()
        );

        assertTrue(
                savedToken.getExpiresAt()
                        .isAfter(Instant.now())
        );
    }

}
