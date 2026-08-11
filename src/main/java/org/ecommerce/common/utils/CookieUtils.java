package org.ecommerce.common.utils;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.ecommerce.common.config.properties.CookieProperties;
import org.ecommerce.common.config.properties.JwtProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class CookieUtils {
    private final CookieProperties cookieProperties;
    private final JwtProperties jwtProperties;

    public void setAuthCookies(
            HttpServletResponse response,
            String accessToken,
            String refreshToken
    ) {
        addCookie(
                response,
                cookieProperties.getAccessTokenName(),
                accessToken,
                jwtProperties.getAccessTokenExpiration()
        );
        addCookie(
                response,
                cookieProperties.getRefreshTokenName(),
                refreshToken,
                jwtProperties.getRefreshTokenExpiration()
        );
    }

    public void clearAuthCookies(HttpServletResponse response) {
        addCookie(response, cookieProperties.getAccessTokenName(), "", 0);
        addCookie(response, cookieProperties.getRefreshTokenName(), "", 0);
    }

    private void addCookie(
            HttpServletResponse response,
            String name,
            String value,
            long maxAgeSeconds
    ) {
        ResponseCookie cookie = ResponseCookie.from(name, value)
                .secure(cookieProperties.isSecure())
                .httpOnly(cookieProperties.isHttpOnly())
                .sameSite(cookieProperties.getSameSite())
                .path(cookieProperties.getPath())
                .maxAge(Duration.ofSeconds(maxAgeSeconds))
                .build();
        response.addHeader(
                HttpHeaders.SET_COOKIE,
                cookie.toString()
        );
    }

    public String getAccessToken(HttpServletRequest request) {
        return getCookieValue(request, cookieProperties.getAccessTokenName());
    }

    public String getRefreshToken(HttpServletRequest request) {
        return getCookieValue(request, cookieProperties.getRefreshTokenName());
    }

    private String getCookieValue(HttpServletRequest request, String cookieName) {
        Cookie[] cookies = request.getCookies();

        if (cookies == null) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (cookieName.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
