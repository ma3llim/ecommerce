package org.ecommerce.auth.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import org.ecommerce.auth.Dtos.request.RegisterUserRequestDto;
import org.ecommerce.auth.Dtos.response.UserAndTokenResponseDto;
import org.ecommerce.auth.Dtos.response.UserResponseDto;
import org.ecommerce.auth.entities.RefreshToken;
import org.ecommerce.auth.entities.User;
import org.ecommerce.auth.repository.OtpVerificationRepository;
import org.ecommerce.auth.repository.RefreshTokenRepository;
import org.ecommerce.auth.repository.UserRepository;
import org.ecommerce.auth.security.JwtService;
import org.ecommerce.auth.utils.PasswordUtils;
import org.ecommerce.common.config.properties.JwtProperties;
import org.ecommerce.common.exception.ResourceNotFoundException;
import org.ecommerce.common.exception.UnauthorizedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceRefreshTokenTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private OtpVerificationRepository otpVerificationRepository;

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

    private RegisterUserRequestDto requestDto;

    @BeforeEach
    void setUp() {
        requestDto = new RegisterUserRequestDto(
                "John",
                "Doe",
                "john@example.com",
                "password123"
        );
    }

    @Test
    @DisplayName("Should throw exception when refresh token is null")
    void refreshToken_whenRefreshTokenIsNull_throwsUnauthorizedException() {

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
    void refreshToken_whenRefreshTokenIsBlank_throwsUnauthorizedException() {

        assertThrows(
                UnauthorizedException.class,
                () -> authService.refreshToken("   ")
        );

        verifyNoInteractions(jwtService);
        verifyNoInteractions(refreshTokenRepository);
        verifyNoInteractions(userRepository);
    }

    @Test
    @DisplayName("Should throw exception when JWT refresh token is invalid")
    void refreshToken_whenJwtIsInvalid_throwsUnauthorizedException() {

        String refreshToken = "invalid-refresh-token";

        when(jwtService.validateRefreshToken(refreshToken))
                .thenReturn(false);

        assertThrows(
                UnauthorizedException.class,
                () -> authService.refreshToken(refreshToken)
        );

        verify(jwtService)
                .validateRefreshToken(refreshToken);

        verify(refreshTokenRepository, never())
                .findById(any());

        verifyNoInteractions(userRepository);
    }

    @Test
    @DisplayName("Should throw exception when refresh token does not exist in database")
    void refreshToken_whenTokenDoesNotExistInDatabase_throwsUnauthorizedException() {

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

        verify(refreshTokenRepository)
                .findById(tokenId);

        verifyNoInteractions(userRepository);
    }

    @Test
    @DisplayName("Should throw exception when database refresh token is expired")
    void refreshToken_whenDatabaseTokenIsExpired_throwsUnauthorizedException() {

        String refreshToken = "valid-refresh-token";

        UUID tokenId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Claims claims = mock(Claims.class);

        RefreshToken tokenEntity = new RefreshToken();
        tokenEntity.setId(tokenId);
        tokenEntity.setUserId(userId);
        tokenEntity.setExpiresAt(
                Instant.now().minusSeconds(60)
        );
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

        assertThrows(
                UnauthorizedException.class,
                () -> authService.refreshToken(refreshToken)
        );

        verify(refreshTokenRepository)
                .findById(tokenId);

        verify(refreshTokenRepository, never())
                .save(any(RefreshToken.class));

        verifyNoInteractions(userRepository);
    }

    @Test
    @DisplayName("Should throw exception when refresh token user does not match token user")
    void refreshToken_whenTokenUserDoesNotMatchClaimUser_throwsUnauthorizedException() {

        String refreshToken = "valid-refresh-token";

        UUID tokenId = UUID.randomUUID();
        UUID claimUserId = UUID.randomUUID();
        UUID tokenUserId = UUID.randomUUID();

        Claims claims = mock(Claims.class);

        RefreshToken tokenEntity = new RefreshToken();
        tokenEntity.setId(tokenId);
        tokenEntity.setUserId(tokenUserId);
        tokenEntity.setExpiresAt(
                Instant.now().plusSeconds(300)
        );
        tokenEntity.setRevoked(false);

        when(jwtService.validateRefreshToken(refreshToken))
                .thenReturn(true);

        when(jwtService.extractClaims(refreshToken))
                .thenReturn(claims);

        when(jwtService.getJwtId(claims))
                .thenReturn(tokenId.toString());

        when(jwtService.getUserId(claims))
                .thenReturn(claimUserId);

        when(refreshTokenRepository.findById(tokenId))
                .thenReturn(Optional.of(tokenEntity));

        assertThrows(
                UnauthorizedException.class,
                () -> authService.refreshToken(refreshToken)
        );

        verify(refreshTokenRepository)
                .findById(tokenId);

        verify(refreshTokenRepository, never())
                .save(any(RefreshToken.class));

        verifyNoInteractions(userRepository);
    }

    @Test
    @DisplayName("Should throw exception when refresh token is already revoked")
    void refreshToken_whenTokenIsAlreadyRevoked_throwsUnauthorizedException() {

        String refreshToken = "valid-refresh-token";

        UUID tokenId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Claims claims = mock(Claims.class);

        RefreshToken tokenEntity = new RefreshToken();
        tokenEntity.setId(tokenId);
        tokenEntity.setUserId(userId);
        tokenEntity.setExpiresAt(
                Instant.now().plusSeconds(300)
        );
        tokenEntity.setRevoked(true);

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

        verify(refreshTokenRepository)
                .findById(tokenId);

        verify(refreshTokenRepository, never())
                .save(any(RefreshToken.class));

        verifyNoInteractions(userRepository);
    }

    @Test
    @DisplayName("Should throw exception when user does not exist")
    void refreshToken_whenUserDoesNotExist_throwsResourceNotFoundException() {

        String refreshToken = "valid-refresh-token";

        UUID tokenId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Claims claims = mock(Claims.class);

        RefreshToken tokenEntity = new RefreshToken();
        tokenEntity.setId(tokenId);
        tokenEntity.setUserId(userId);
        tokenEntity.setExpiresAt(
                Instant.now().plusSeconds(300)
        );
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

        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> authService.refreshToken(refreshToken)
        );

        verify(userRepository)
                .findById(userId);

        verify(refreshTokenRepository, never())
                .save(any(RefreshToken.class));

        verify(jwtService, never())
                .generateAccessToken(any());

        verify(jwtService, never())
                .generateRefreshToken(any(), anyString());
    }

    @Test
    @DisplayName("Should rotate tokens and return new tokens when refresh token is valid")
    void refreshToken_whenValidToken_rotatesTokensAndReturnsNewTokens() {

        String oldRefreshToken = "old-refresh-token";

        String newAccessToken = "new-access-token";
        String newRefreshToken = "new-refresh-token";

        UUID oldTokenId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Claims claims = mock(Claims.class);

        User user = new User();
        user.setId(userId);
        user.setEmail("john@example.com");

        RefreshToken oldTokenEntity = new RefreshToken();
        oldTokenEntity.setId(oldTokenId);
        oldTokenEntity.setUserId(userId);
        oldTokenEntity.setRefreshToken(oldRefreshToken);
        oldTokenEntity.setExpiresAt(
                Instant.now().plusSeconds(300)
        );
        oldTokenEntity.setRevoked(false);

        UserResponseDto userResponse =
                mock(UserResponseDto.class);

        when(jwtService.validateRefreshToken(oldRefreshToken))
                .thenReturn(true);

        when(jwtService.extractClaims(oldRefreshToken))
                .thenReturn(claims);

        when(jwtService.getJwtId(claims))
                .thenReturn(oldTokenId.toString());

        when(jwtService.getUserId(claims))
                .thenReturn(userId);

        when(refreshTokenRepository.findById(oldTokenId))
                .thenReturn(Optional.of(oldTokenEntity));

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(jwtService.generateAccessToken(user))
                .thenReturn(newAccessToken);

        when(jwtService.generateRefreshToken(
                eq(user),
                anyString()
        )).thenReturn(newRefreshToken);

        when(jwtProperties.getRefreshTokenExpiration())
                .thenReturn(604800L);

        when(objectMapper.convertValue(
                user,
                UserResponseDto.class
        )).thenReturn(userResponse);

        UserAndTokenResponseDto result =
                authService.refreshToken(oldRefreshToken);

        assertNotNull(result);

        assertEquals(
                newAccessToken,
                result.accessToken()
        );

        assertEquals(
                newRefreshToken,
                result.refreshToken()
        );

        assertSame(
                userResponse,
                result.userResponseDto()
        );

        // Old token must be revoked
        assertTrue(oldTokenEntity.isRevoked());

        assertNotNull(
                oldTokenEntity.getRevokedByTokenId()
        );

        verify(refreshTokenRepository, times(2))
                .save(any(RefreshToken.class));

        verify(jwtService)
                .generateAccessToken(user);

        verify(jwtService)
                .generateRefreshToken(
                        eq(user),
                        anyString()
                );
    }

    @Test
    @DisplayName("Should save new refresh token with correct details when refresh token is valid")
    void refreshToken_whenValidToken_savesNewRefreshTokenWithCorrectDetails() {

        String oldRefreshToken = "old-refresh-token";

        String newAccessToken = "new-access-token";
        String newRefreshToken = "new-refresh-token";

        UUID oldTokenId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Claims claims = mock(Claims.class);

        User user = new User();
        user.setId(userId);
        user.setEmail("john@example.com");

        RefreshToken oldTokenEntity = new RefreshToken();
        oldTokenEntity.setId(oldTokenId);
        oldTokenEntity.setUserId(userId);
        oldTokenEntity.setExpiresAt(
                Instant.now().plusSeconds(300)
        );
        oldTokenEntity.setRevoked(false);

        when(jwtService.validateRefreshToken(oldRefreshToken))
                .thenReturn(true);

        when(jwtService.extractClaims(oldRefreshToken))
                .thenReturn(claims);

        when(jwtService.getJwtId(claims))
                .thenReturn(oldTokenId.toString());

        when(jwtService.getUserId(claims))
                .thenReturn(userId);

        when(refreshTokenRepository.findById(oldTokenId))
                .thenReturn(Optional.of(oldTokenEntity));

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(jwtService.generateAccessToken(user))
                .thenReturn(newAccessToken);

        when(jwtService.generateRefreshToken(
                eq(user),
                anyString()
        )).thenReturn(newRefreshToken);

        when(jwtProperties.getRefreshTokenExpiration())
                .thenReturn(604800L);

        when(objectMapper.convertValue(
                user,
                UserResponseDto.class
        )).thenReturn(mock(UserResponseDto.class));

        authService.refreshToken(oldRefreshToken);

        ArgumentCaptor<RefreshToken> tokenCaptor =
                ArgumentCaptor.forClass(RefreshToken.class);

        verify(refreshTokenRepository, times(2))
                .save(tokenCaptor.capture());

        RefreshToken newToken =
                tokenCaptor.getAllValues().get(1);

        assertNotNull(newToken.getId());

        assertNotEquals(
                oldTokenId,
                newToken.getId()
        );

        assertEquals(
                userId,
                newToken.getUserId()
        );

        assertEquals(
                newRefreshToken,
                newToken.getRefreshToken()
        );

        assertNotNull(
                newToken.getExpiresAt()
        );

        assertTrue(
                newToken.getExpiresAt()
                        .isAfter(Instant.now())
        );
    }

}
