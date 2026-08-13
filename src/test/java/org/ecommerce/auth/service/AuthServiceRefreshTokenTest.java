package org.ecommerce.auth.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import org.ecommerce.auth.Dtos.response.UserAndTokenResponseDto;
import org.ecommerce.auth.Dtos.response.UserResponseDto;
import org.ecommerce.auth.entities.RefreshToken;
import org.ecommerce.auth.entities.User;
import org.ecommerce.auth.enums.AccountStatus;
import org.ecommerce.auth.repository.RefreshTokenRepository;
import org.ecommerce.auth.repository.UserRepository;
import org.ecommerce.common.config.properties.JwtProperties;
import org.ecommerce.common.exception.ResourceNotFoundException;
import org.ecommerce.common.exception.UnauthorizedException;
import org.ecommerce.common.security.JwtService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceRefreshTokenTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private JwtProperties jwtProperties;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private AuthService authService;

    @Test
    @DisplayName("Should throw exception when refresh token is null")
    void shouldThrowExceptionWhenRefreshTokenIsNull() {

        assertThrows(
                UnauthorizedException.class,
                () -> authService.refreshToken(null)
        );

        verifyNoInteractions(jwtService);
        verifyNoInteractions(refreshTokenRepository);
        verifyNoInteractions(userRepository);
    }

    @Test
    @DisplayName("Should throw exception when refresh token is blank")
    void shouldThrowExceptionWhenRefreshTokenIsBlank() {

        assertThrows(
                UnauthorizedException.class,
                () -> authService.refreshToken("   ")
        );

        verifyNoInteractions(jwtService);
        verifyNoInteractions(refreshTokenRepository);
        verifyNoInteractions(userRepository);
    }

    @Test
    @DisplayName("Should throw exception when refresh token is invalid")
    void shouldThrowExceptionWhenRefreshTokenIsInvalid() {

        String refreshToken = "invalid-refresh-token";

        when(jwtService.validateRefreshToken(refreshToken))
                .thenReturn(false);

        assertThrows(
                UnauthorizedException.class,
                () -> authService.refreshToken(refreshToken)
        );

        verify(jwtService).validateRefreshToken(refreshToken);

        verify(jwtService, never())
                .extractClaims(anyString());

        verifyNoInteractions(refreshTokenRepository);
        verifyNoInteractions(userRepository);
    }

    @Test
    @DisplayName("Should throw exception when refresh token does not exist")
    void shouldThrowExceptionWhenRefreshTokenDoesNotExist() {

        String refreshToken = "valid-refresh-token";
        UUID tokenId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Claims claims = mock(Claims.class);

        when(jwtService.validateRefreshToken(refreshToken))
                .thenReturn(true);

        when(jwtService.extractClaims(refreshToken))
                .thenReturn(claims);

        when(jwtService.getJwtId(claims))
                .thenReturn(tokenId.toString());

        when(jwtService.getUserId(claims))
                .thenReturn(userId);

        when(refreshTokenRepository.findById(tokenId))
                .thenReturn(Optional.empty());

        assertThrows(
                UnauthorizedException.class,
                () -> authService.refreshToken(refreshToken)
        );

        verify(refreshTokenRepository).findById(tokenId);

        verify(userRepository, never())
                .findById(any(UUID.class));
    }

    @Test
    @DisplayName("Should throw exception when refresh token is expired")
    void shouldThrowExceptionWhenRefreshTokenIsExpired() {

        String refreshToken = "valid-refresh-token";
        UUID tokenId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Claims claims = mock(Claims.class);

        RefreshToken tokenEntity = RefreshToken.builder()
                .id(tokenId)
                .userId(userId)
                .refreshToken(refreshToken)
                .expiresAt(Instant.now().minusSeconds(100))
                .build();

        when(jwtService.validateRefreshToken(refreshToken))
                .thenReturn(true);

        when(jwtService.extractClaims(refreshToken))
                .thenReturn(claims);

        when(jwtService.getJwtId(claims))
                .thenReturn(tokenId.toString());

        when(jwtService.getUserId(claims))
                .thenReturn(userId);

        when(refreshTokenRepository.findById(tokenId))
                .thenReturn(Optional.of(tokenEntity));

        assertThrows(
                UnauthorizedException.class,
                () -> authService.refreshToken(refreshToken)
        );

        verify(refreshTokenRepository).findById(tokenId);

        verify(refreshTokenRepository, never())
                .save(any(RefreshToken.class));

        verify(userRepository, never())
                .findById(any(UUID.class));
    }

    @Test
    @DisplayName("Should throw exception when refresh token user ID does not match")
    void shouldThrowExceptionWhenRefreshTokenUserIdDoesNotMatch() {

        String refreshToken = "valid-refresh-token";

        UUID tokenId = UUID.randomUUID();
        UUID tokenUserId = UUID.randomUUID();
        UUID jwtUserId = UUID.randomUUID();

        Claims claims = mock(Claims.class);

        RefreshToken tokenEntity = RefreshToken.builder()
                .id(tokenId)
                .userId(tokenUserId)
                .refreshToken(refreshToken)
                .expiresAt(Instant.now().plusSeconds(300))
                .build();

        when(jwtService.validateRefreshToken(refreshToken))
                .thenReturn(true);

        when(jwtService.extractClaims(refreshToken))
                .thenReturn(claims);

        when(jwtService.getJwtId(claims))
                .thenReturn(tokenId.toString());

        when(jwtService.getUserId(claims))
                .thenReturn(jwtUserId);

        when(refreshTokenRepository.findById(tokenId))
                .thenReturn(Optional.of(tokenEntity));

        assertThrows(
                UnauthorizedException.class,
                () -> authService.refreshToken(refreshToken)
        );

        verify(refreshTokenRepository).findById(tokenId);

        verify(userRepository, never())
                .findById(any(UUID.class));
    }

    @Test
    @DisplayName("Should throw exception when refresh token is revoked")
    void shouldThrowExceptionWhenRefreshTokenIsRevoked() {

        String refreshToken = "valid-refresh-token";

        UUID tokenId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Claims claims = mock(Claims.class);

        RefreshToken tokenEntity = RefreshToken.builder()
                .id(tokenId)
                .userId(userId)
                .refreshToken(refreshToken)
                .expiresAt(Instant.now().plusSeconds(300))
                .isRevoked(true)
                .build();

        when(jwtService.validateRefreshToken(refreshToken))
                .thenReturn(true);

        when(jwtService.extractClaims(refreshToken))
                .thenReturn(claims);

        when(jwtService.getJwtId(claims))
                .thenReturn(tokenId.toString());

        when(jwtService.getUserId(claims))
                .thenReturn(userId);

        when(refreshTokenRepository.findById(tokenId))
                .thenReturn(Optional.of(tokenEntity));

        assertThrows(
                UnauthorizedException.class,
                () -> authService.refreshToken(refreshToken)
        );

        verify(userRepository, never())
                .findById(any(UUID.class));

        verify(refreshTokenRepository, never())
                .save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("Should throw exception when user does not exist for valid refresh token")
    void shouldThrowExceptionWhenUserDoesNotExistForValidRefreshToken() {

        String refreshToken = "valid-refresh-token";

        UUID tokenId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Claims claims = mock(Claims.class);

        RefreshToken tokenEntity = RefreshToken.builder()
                .id(tokenId)
                .userId(userId)
                .refreshToken(refreshToken)
                .expiresAt(Instant.now().plusSeconds(300))
                .isRevoked(false)
                .build();

        when(jwtService.validateRefreshToken(refreshToken))
                .thenReturn(true);

        when(jwtService.extractClaims(refreshToken))
                .thenReturn(claims);

        when(jwtService.getJwtId(claims))
                .thenReturn(tokenId.toString());

        when(jwtService.getUserId(claims))
                .thenReturn(userId);

        when(refreshTokenRepository.findById(tokenId))
                .thenReturn(Optional.of(tokenEntity));

        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> authService.refreshToken(refreshToken)
        );

        verify(userRepository).findById(userId);

        verify(jwtService, never())
                .generateAccessToken(any(User.class));

        verify(refreshTokenRepository, never())
                .save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("Should refresh tokens successfully when refresh token is valid")
    void shouldRefreshTokensSuccessfullyWhenRefreshTokenIsValid() {

        String oldRefreshToken = "old-refresh-token";
        String newAccessToken = "new-access-token";
        String newRefreshToken = "new-refresh-token";

        UUID tokenId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Claims claims = mock(Claims.class);

        User user = User.builder()
                .id(userId)
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .emailVerified(true)
                .accountStatus(AccountStatus.ACTIVE)
                .build();

        RefreshToken tokenEntity = RefreshToken.builder()
                .id(tokenId)
                .userId(userId)
                .refreshToken(oldRefreshToken)
                .expiresAt(Instant.now().plusSeconds(300))
                .isRevoked(false)
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

        when(jwtService.validateRefreshToken(oldRefreshToken))
                .thenReturn(true);

        when(jwtService.extractClaims(oldRefreshToken))
                .thenReturn(claims);

        when(jwtService.getJwtId(claims))
                .thenReturn(tokenId.toString());

        when(jwtService.getUserId(claims))
                .thenReturn(userId);

        when(refreshTokenRepository.findById(tokenId))
                .thenReturn(Optional.of(tokenEntity));

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(jwtService.generateAccessToken(user))
                .thenReturn(newAccessToken);

        when(jwtService.generateRefreshToken(
                eq(user),
                anyString()
        )).thenReturn(newRefreshToken);

        when(jwtProperties.getRefreshTokenExpiration())
                .thenReturn(3600L);

        when(objectMapper.convertValue(
                eq(user),
                eq(UserResponseDto.class)
        )).thenReturn(userResponseDto);

        when(refreshTokenRepository.save(any(RefreshToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UserAndTokenResponseDto result =
                authService.refreshToken(oldRefreshToken);

        assertNotNull(result);

        assertEquals(newAccessToken, result.accessToken());
        assertEquals(newRefreshToken, result.refreshToken());

        assertTrue(tokenEntity.isRevoked());
        assertNotNull(tokenEntity.getRevokedByTokenId());

        verify(refreshTokenRepository)
                .save(tokenEntity);

        verify(refreshTokenRepository)
                .save(argThat(token ->
                        token.getUserId().equals(userId)
                                && token.getRefreshToken().equals(newRefreshToken)
                ));

        verify(jwtService)
                .generateAccessToken(user);

        verify(jwtService)
                .generateRefreshToken(eq(user), anyString());

        verify(objectMapper)
                .convertValue(user, UserResponseDto.class);
    }
}
