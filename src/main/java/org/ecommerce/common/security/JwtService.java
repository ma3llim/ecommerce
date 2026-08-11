package org.ecommerce.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.ecommerce.auth.entities.User;
import org.ecommerce.auth.enums.TokenType;
import org.ecommerce.common.config.properties.CookieProperties;
import org.ecommerce.common.config.properties.JwtProperties;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JwtService {
    private final JwtProperties jwtProperties;
    private final CookieProperties cookieProperties;

    // Secret Key Sign
    public SecretKey secretKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtProperties.getSecret()));
    }

    // generate accessToken
    public String generateAccessToken(User user) {
        Instant now = Instant.now();
        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(user.getId().toString())
                .issuer(jwtProperties.getIssuer())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(jwtProperties.getAccessTokenExpiration())))
                .claim("role", user.getRole().name())
                .claim("type", "access")
                .signWith(secretKey(), Jwts.SIG.HS512)
                .compact();
    }


    // generate refreshToken
    public String generateRefreshToken(User user, String refreshTokenId) {
        Instant now = Instant.now();
        return Jwts.builder()
                .id(refreshTokenId)
                .subject(user.getId().toString())
                .issuer(jwtProperties.getIssuer())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(jwtProperties.getRefreshTokenExpiration())))
                .claim("type", "refresh")
                .signWith(secretKey(), Jwts.SIG.HS512)
                .compact();
    }

    // parse + verify JWT
    public Jws<Claims> parse(String token) {
        return Jwts.parser().verifyWith(secretKey()).build().parseSignedClaims(token);
    }

    // extract claims
    public Claims extractClaims(String token) {
        return parse(token).getPayload();
    }

    // extract UserId
    public UUID getUserId(Claims claims) {
        return UUID.fromString(claims.getSubject());
    }

    // extract Jwt Id
    public String getJwtId(Claims claims) {
        return claims.getId();
    }

    // check token type access
    public boolean isAccessToken(Claims claims) {
        return TokenType.ACCESS.name().equalsIgnoreCase(claims.get("type", String.class));
    }

    // check token type refresh
    public boolean isRefreshToken(Claims claims) {
        return TokenType.REFRESH.name().equalsIgnoreCase(claims.get("type", String.class));
    }

    // validate access token
    public boolean validateAccessToken(String token) {
        try {
            Claims claims = extractClaims(token);
            return isAccessToken(claims) && claims.getId() != null && claims.getSubject() != null;
        } catch (JwtException | IllegalArgumentException exception) {
            return false;
        }
    }

    // validate refresh token
    public boolean validateRefreshToken(String token) {
        try {
            Claims claims = extractClaims(token);
            return isRefreshToken(claims) && claims.getId() != null && claims.getSubject() != null;
        } catch (JwtException | IllegalArgumentException exception) {
            return false;
        }
    }
}
