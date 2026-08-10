package org.ecommerce.auth.service;

import io.jsonwebtoken.Claims;
import org.ecommerce.auth.entities.RefreshToken;
import org.ecommerce.auth.repository.RefreshTokenRepository;
import org.ecommerce.auth.security.JwtService;
import org.ecommerce.common.exception.UnauthorizedException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceLogoutTest {
    @InjectMocks
    @Spy
    private AuthService authService;
    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private JwtService jwtService;

    @Test
    @DisplayName("Should throw exception when refresh token is null")
    void logout_whenRefreshTokenIsNull_throwsUnauthorizedException() {

        assertThrows(
                UnauthorizedException.class,
                () -> authService.logout(null)
        );

        verifyNoInteractions(jwtService);
        verifyNoInteractions(refreshTokenRepository);
    }


    @Test
    @DisplayName("Should throw exception when refresh token is blank")
    void logout_whenRefreshTokenIsBlank_throwsUnauthorizedException() {

        assertThrows(
                UnauthorizedException.class,
                () -> authService.logout("   ")
        );

        verifyNoInteractions(jwtService);
        verifyNoInteractions(refreshTokenRepository);
    }


    @Test
    @DisplayName("Should throw exception when refresh token is invalid")
    void logout_whenRefreshTokenIsInvalid_throwsUnauthorizedException() {

        String refreshToken = "invalid-refresh-token";

        when(jwtService.validateRefreshToken(refreshToken))
                .thenReturn(false);

        assertThrows(
                UnauthorizedException.class,
                () -> authService.logout(refreshToken)
        );

        verify(jwtService)
                .validateRefreshToken(refreshToken);

        verifyNoInteractions(refreshTokenRepository);
    }


    @Test
    @DisplayName("Should throw exception when refresh token does not exist")
    void logout_whenRefreshTokenDoesNotExist_throwsUnauthorizedException() {

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
                () -> authService.logout(refreshToken)
        );

        verify(refreshTokenRepository)
                .findById(tokenId);

        verify(refreshTokenRepository, never())
                .save(any(RefreshToken.class));
    }


    @Test
    @DisplayName("Should revoke and save refresh token when refresh token is valid")
    void logout_whenRefreshTokenIsValid_revokesAndSavesToken() {

        String refreshToken = "valid-refresh-token";

        UUID tokenId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Claims claims = mock(Claims.class);

        RefreshToken tokenEntity = new RefreshToken();

        tokenEntity.setId(tokenId);
        tokenEntity.setUserId(userId);
        tokenEntity.setRefreshToken(refreshToken);
        tokenEntity.setRevoked(false);

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

        authService.logout(refreshToken);

        assertTrue(tokenEntity.isRevoked());

        verify(jwtService)
                .validateRefreshToken(refreshToken);

        verify(jwtService)
                .extractClaims(refreshToken);

        verify(jwtService)
                .getJwtId(claims);

        verify(jwtService)
                .getUserId(claims);

        verify(refreshTokenRepository)
                .findById(tokenId);

        verify(refreshTokenRepository)
                .save(tokenEntity);
    }
}
