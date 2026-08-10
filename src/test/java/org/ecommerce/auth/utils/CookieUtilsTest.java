package org.ecommerce.auth.utils;

import jakarta.servlet.http.Cookie;
import org.ecommerce.common.config.properties.CookieProperties;
import org.ecommerce.common.config.properties.JwtProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CookieUtilsTest {
    @Mock
    private CookieProperties cookieProperties;

    @Mock
    private JwtProperties jwtProperties;

    @InjectMocks
    private CookieUtils cookieUtils;

    // setAuthCookies()
    @Test
    void setAuthCookies_whenValidTokens_addsAccessAndRefreshCookies() {
        String accessToken = "test-access-token";
        String refreshToken = "test-refresh-token";

        when(cookieProperties.getAccessTokenName()).thenReturn("access-token");
        when(cookieProperties.getRefreshTokenName()).thenReturn("refresh-token");
        when(jwtProperties.getAccessTokenExpiration()).thenReturn(900L);
        when(jwtProperties.getRefreshTokenExpiration()).thenReturn(604800L);

        MockHttpServletResponse response = new MockHttpServletResponse();

        cookieUtils.setAuthCookies(response, accessToken, refreshToken);

        assertEquals(2, response.getHeaders("Set-Cookie").size());

        assertTrue(response.getHeaders("Set-Cookie").stream()
                .anyMatch(cookie -> cookie.contains("access-token=" + accessToken)));

        assertTrue(response.getHeaders("Set-Cookie")
                .stream()
                .anyMatch(cookie -> cookie.contains("refresh-token=" + refreshToken))
        );
    }

    @Test
    void setAuthCookies_setsCorrectAccessTokenCookieAttributes() {
        String accessToken = "test-access-token";
        String refreshToken = "test-refresh-token";

        when(cookieProperties.getAccessTokenName()).thenReturn("access-token");
        when(cookieProperties.getRefreshTokenName()).thenReturn("refresh-token");
        when(jwtProperties.getAccessTokenExpiration()).thenReturn(900L);
        when(jwtProperties.getRefreshTokenExpiration()).thenReturn(604800L);
        when(cookieProperties.isSecure()).thenReturn(false);

        when(cookieProperties.isHttpOnly()).thenReturn(true);

        when(cookieProperties.getSameSite()).thenReturn("Lax");

        when(cookieProperties.getPath()).thenReturn("/");

        MockHttpServletResponse response = new MockHttpServletResponse();

        cookieUtils.setAuthCookies(response, accessToken, refreshToken);

        String accessCookie = response.getHeaders("Set-Cookie")
                .stream()
                .filter(cookie -> cookie.startsWith("access-token="))
                .findFirst()
                .orElseThrow();

        assertTrue(accessCookie.contains("access-token=" + accessToken));
        assertTrue(accessCookie.contains("Max-Age=900"));
        assertTrue(accessCookie.contains("HttpOnly"));
        assertTrue(accessCookie.contains("SameSite=Lax"));
        assertTrue(accessCookie.contains("Path=/"));
        assertTrue(accessCookie.contains("Secure"));
    }

    @Test
    void setAuthCookies_setsCorrectRefreshTokenCookieAttributes() {
        String accessToken = "test-access-token";
        String refreshToken = "test-refresh-token";

        when(cookieProperties.getAccessTokenName()).thenReturn("access-token");
        when(cookieProperties.getRefreshTokenName()).thenReturn("refresh-token");
        when(jwtProperties.getAccessTokenExpiration()).thenReturn(900L);
        when(jwtProperties.getRefreshTokenExpiration()).thenReturn(604800L);
        when(cookieProperties.isSecure()).thenReturn(false);
        when(cookieProperties.isHttpOnly()).thenReturn(true);
        when(cookieProperties.getSameSite()).thenReturn("Lax");
        when(cookieProperties.getPath()).thenReturn("/");

        MockHttpServletResponse response = new MockHttpServletResponse();

        cookieUtils.setAuthCookies(response, accessToken, refreshToken);

        String refreshCookie = response.getHeaders("Set-Cookie")
                .stream()
                .filter(cookie -> cookie.startsWith("refresh-token="))
                .findFirst()
                .orElseThrow();

        assertTrue(refreshCookie.contains("refresh-token=" + refreshToken));
        assertTrue(refreshCookie.contains("Max-Age=604800"));
        assertTrue(refreshCookie.contains("HttpOnly"));
        assertTrue(refreshCookie.contains("SameSite=Lax"));
        assertTrue(refreshCookie.contains("Path=/"));
        assertTrue(refreshCookie.contains("Secure"));
    }

    // clearAuthCookies()
    @Test
    void clearAuthCookies_addsExpiredAccessAndRefreshCookies() {
        when(cookieProperties.getAccessTokenName()).thenReturn("access-token");
        when(cookieProperties.getRefreshTokenName()).thenReturn("refresh-token");

        MockHttpServletResponse response = new MockHttpServletResponse();

        cookieUtils.clearAuthCookies(response);

        assertEquals(2, response.getHeaders("Set-Cookie").size());

        String accessCookie = response.getHeaders("Set-Cookie")
                .stream()
                .filter(cookie -> cookie.startsWith("access-token="))
                .findFirst()
                .orElseThrow();

        String refreshCookie = response.getHeaders("Set-Cookie")
                .stream()
                .filter(cookie -> cookie.startsWith("refresh-token="))
                .findFirst()
                .orElseThrow();

        assertTrue(accessCookie.contains("Max-Age=0"));
        assertTrue(refreshCookie.contains("Max-Age=0"));
    }

    // getAccessToken()
    @Test
    void getAccessToken_whenAccessTokenCookieExists_returnsToken() {
        String token = "test-access-token";

        when(cookieProperties.getAccessTokenName()).thenReturn("access-token");

        MockHttpServletRequest request = new MockHttpServletRequest();

        request.setCookies(new Cookie("access-token", token));

        String result = cookieUtils.getAccessToken(request);

        assertEquals(token, result);
    }

    @Test
    void getAccessToken_whenNoCookiesExist_returnsNull() {
        when(cookieProperties.getAccessTokenName()).thenReturn("access-token");
        MockHttpServletRequest request = new MockHttpServletRequest();

        String result = cookieUtils.getAccessToken(request);

        assertNull(result);
    }

    @Test
    void getAccessToken_whenAccessTokenCookieDoesNotExist_returnsNull() {
        when(cookieProperties.getAccessTokenName()).thenReturn("access-token");

        MockHttpServletRequest request = new MockHttpServletRequest();

        request.setCookies(new Cookie("some-other-cookie", "some-value"));

        String result = cookieUtils.getAccessToken(request);

        assertNull(result);
    }

    // getRefreshToken()
    @Test
    void getRefreshToken_whenRefreshTokenCookieExists_returnsToken() {
        String token = "test-refresh-token";

        when(cookieProperties.getRefreshTokenName()).thenReturn("refresh-token");

        MockHttpServletRequest request = new MockHttpServletRequest();

        request.setCookies(new Cookie("refresh-token", token));

        String result = cookieUtils.getRefreshToken(request);

        assertEquals(token, result);
    }

    @Test
    void getRefreshToken_whenNoCookiesExist_returnsNull() {
        when(cookieProperties.getRefreshTokenName()).thenReturn("refresh-token");

        MockHttpServletRequest request = new MockHttpServletRequest();

        String result = cookieUtils.getRefreshToken(request);

        assertNull(result);
    }

    @Test
    void getRefreshToken_whenRefreshTokenCookieDoesNotExist_returnsNull() {
        when(cookieProperties.getRefreshTokenName()).thenReturn("refresh-token");

        MockHttpServletRequest request = new MockHttpServletRequest();

        request.setCookies(new Cookie("some-other-cookie", "some-value"));

        String result = cookieUtils.getRefreshToken(request);

        assertNull(result);
    }
}
