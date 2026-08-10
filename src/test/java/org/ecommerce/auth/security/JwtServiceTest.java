package org.ecommerce.auth.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import org.ecommerce.auth.entities.User;
import org.ecommerce.auth.enums.Role;
import org.ecommerce.auth.enums.TokenType;
import org.ecommerce.common.config.properties.CookieProperties;
import org.ecommerce.common.config.properties.JwtProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class JwtServiceTest {
    @Mock
    private JwtProperties jwtProperties;

    @Mock
    private CookieProperties cookieProperties;

    @InjectMocks
    private JwtService jwtService;

    private String secret;
    private UUID userId;
    private User user;

    @BeforeEach
    void setUp() {
        byte[] secretBytes = new byte[64];

        for (int i = 0; i < secretBytes.length; i++) {
            secretBytes[i] = (byte) i;
        }

        secret = Base64.getEncoder().encodeToString(secretBytes);

        userId = UUID.randomUUID();

        user = new User();
        user.setId(userId);
    }

    // secretKey()
    @Test
    void secretKey_whenValidSecretConfigured_returnsSecretKey() {
        when(jwtProperties.getSecret()).thenReturn(secret);

        SecretKey result = jwtService.secretKey();
        assertNotNull(result);
    }


    // generateAccessToken()

    @Test
    void generateAccessToken_whenValidUserProvided_returnsValidAccessToken() {

        when(jwtProperties.getSecret())
                .thenReturn(secret);

        when(jwtProperties.getIssuer())
                .thenReturn("ecommerce");

        when(jwtProperties.getAccessTokenExpiration())
                .thenReturn(900L);

        // Set your actual Role enum value here
        user.setRole(Role.USER);

        String token = jwtService.generateAccessToken(user);

        assertNotNull(token);
        assertFalse(token.isBlank());

        Claims claims = jwtService.extractClaims(token);

        assertEquals(userId.toString(), claims.getSubject());
        assertEquals("ecommerce", claims.getIssuer());
        assertEquals(
                TokenType.ACCESS.name().toLowerCase(),
                claims.get("type", String.class)
        );

        assertNotNull(claims.getId());
        assertNotNull(claims.getIssuedAt());
        assertNotNull(claims.getExpiration());
    }


    // generateRefreshToken()
    @Test
    void generateRefreshToken_whenValidUserAndTokenIdProvided_returnsValidRefreshToken() {

        String refreshTokenId = UUID.randomUUID().toString();

        when(jwtProperties.getSecret())
                .thenReturn(secret);

        when(jwtProperties.getIssuer())
                .thenReturn("ecommerce");

        when(jwtProperties.getRefreshTokenExpiration())
                .thenReturn(604800L);

        String token = jwtService.generateRefreshToken(
                user,
                refreshTokenId
        );

        assertNotNull(token);
        assertFalse(token.isBlank());

        Claims claims = jwtService.extractClaims(token);

        assertEquals(userId.toString(), claims.getSubject());
        assertEquals("ecommerce", claims.getIssuer());
        assertEquals(refreshTokenId, claims.getId());

        assertEquals(
                TokenType.REFRESH.name().toLowerCase(),
                claims.get("type", String.class)
        );

        assertNotNull(claims.getIssuedAt());
        assertNotNull(claims.getExpiration());
    }


    // parse()
    @Test
    void parse_whenValidTokenProvided_returnsParsedClaims() {

        when(jwtProperties.getSecret())
                .thenReturn(secret);

        when(jwtProperties.getIssuer())
                .thenReturn("ecommerce");

        when(jwtProperties.getRefreshTokenExpiration())
                .thenReturn(604800L);

        String token = jwtService.generateRefreshToken(
                user,
                "refresh-id"
        );

        Jws<Claims> result = jwtService.parse(token);

        assertNotNull(result);
        assertNotNull(result.getPayload());
    }


    // extractClaims()
    @Test
    void extractClaims_whenValidTokenProvided_returnsClaims() {

        when(jwtProperties.getSecret())
                .thenReturn(secret);

        when(jwtProperties.getIssuer())
                .thenReturn("ecommerce");

        when(jwtProperties.getRefreshTokenExpiration())
                .thenReturn(604800L);

        String token = jwtService.generateRefreshToken(
                user,
                "refresh-id"
        );

        Claims claims = jwtService.extractClaims(token);

        assertEquals(userId.toString(), claims.getSubject());
    }


    // getUserId()
    @Test
    void getUserId_whenClaimsContainValidSubject_returnsUserId() {

        Claims claims = org.mockito.Mockito.mock(Claims.class);

        when(claims.getSubject())
                .thenReturn(userId.toString());

        UUID result = jwtService.getUserId(claims);

        assertEquals(userId, result);
    }


    // getJwtId()
    @Test
    void getJwtId_whenClaimsContainJwtId_returnsJwtId() {

        String jwtId = UUID.randomUUID().toString();

        Claims claims = org.mockito.Mockito.mock(Claims.class);

        when(claims.getId())
                .thenReturn(jwtId);

        String result = jwtService.getJwtId(claims);

        assertEquals(jwtId, result);
    }


    // isAccessToken()

    @Test
    void isAccessToken_whenTokenTypeIsAccess_returnsTrue() {

        Claims claims = org.mockito.Mockito.mock(Claims.class);

        when(claims.get("type", String.class))
                .thenReturn("access");

        assertTrue(jwtService.isAccessToken(claims));
    }


    @Test
    void isAccessToken_whenTokenTypeIsRefresh_returnsFalse() {

        Claims claims = org.mockito.Mockito.mock(Claims.class);

        when(claims.get("type", String.class))
                .thenReturn("refresh");

        assertFalse(jwtService.isAccessToken(claims));
    }


    // isRefreshToken()

    @Test
    void isRefreshToken_whenTokenTypeIsRefresh_returnsTrue() {

        Claims claims = org.mockito.Mockito.mock(Claims.class);

        when(claims.get("type", String.class))
                .thenReturn("refresh");

        assertTrue(jwtService.isRefreshToken(claims));
    }


    @Test
    void isRefreshToken_whenTokenTypeIsAccess_returnsFalse() {

        Claims claims = org.mockito.Mockito.mock(Claims.class);

        when(claims.get("type", String.class))
                .thenReturn("access");

        assertFalse(jwtService.isRefreshToken(claims));
    }


    // validateAccessToken()

    @Test
    void validateAccessToken_whenValidAccessTokenProvided_returnsTrue() {

        when(jwtProperties.getSecret())
                .thenReturn(secret);

        when(jwtProperties.getIssuer())
                .thenReturn("ecommerce");

        when(jwtProperties.getAccessTokenExpiration())
                .thenReturn(900L);

        user.setRole(Role.USER);

        String token = jwtService.generateAccessToken(user);

        assertTrue(jwtService.validateAccessToken(token));
    }


    @Test
    void validateAccessToken_whenRefreshTokenProvided_returnsFalse() {

        when(jwtProperties.getSecret())
                .thenReturn(secret);

        when(jwtProperties.getIssuer())
                .thenReturn("ecommerce");

        when(jwtProperties.getRefreshTokenExpiration())
                .thenReturn(604800L);

        String token = jwtService.generateRefreshToken(
                user,
                "refresh-id"
        );

        assertFalse(jwtService.validateAccessToken(token));
    }


    @Test
    void validateAccessToken_whenInvalidTokenProvided_returnsFalse() {

        when(jwtProperties.getSecret())
                .thenReturn(secret);

        assertFalse(
                jwtService.validateAccessToken("invalid-token")
        );
    }


    // validateRefreshToken()
    @Test
    void validateRefreshToken_whenValidRefreshTokenProvided_returnsTrue() {

        when(jwtProperties.getSecret())
                .thenReturn(secret);

        when(jwtProperties.getIssuer())
                .thenReturn("ecommerce");

        when(jwtProperties.getRefreshTokenExpiration())
                .thenReturn(604800L);

        String token = jwtService.generateRefreshToken(
                user,
                "refresh-id"
        );

        assertTrue(jwtService.validateRefreshToken(token));
    }


    @Test
    void validateRefreshToken_whenAccessTokenProvided_returnsFalse() {

        when(jwtProperties.getSecret())
                .thenReturn(secret);

        when(jwtProperties.getIssuer())
                .thenReturn("ecommerce");

        when(jwtProperties.getAccessTokenExpiration())
                .thenReturn(900L);

        user.setRole(Role.USER);

        String token = jwtService.generateAccessToken(user);

        assertFalse(jwtService.validateRefreshToken(token));
    }


    @Test
    void validateRefreshToken_whenInvalidTokenProvided_returnsFalse() {
        when(jwtProperties.getSecret()).thenReturn(secret);
        assertFalse(jwtService.validateRefreshToken("invalid-token"));
    }
}
