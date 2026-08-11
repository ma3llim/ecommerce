package org.ecommerce.auth.utils;

import jakarta.servlet.http.Cookie;
import org.ecommerce.common.config.properties.CookieProperties;
import org.ecommerce.common.config.properties.JwtProperties;
import org.ecommerce.common.utils.CookieUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CookieUtilsTest {
    @Mock
    private CookieProperties cookieProperties;
    @Mock
    private JwtProperties jwtProperties;
    @InjectMocks
    private CookieUtils cookieUtils;

    @Test
    @DisplayName("Should add access and refresh cookies")
    void shouldAddAccessAndRefreshCookies() {
        when(cookieProperties.getAccessTokenName()).thenReturn("access-token");
        when(cookieProperties.getRefreshTokenName()).thenReturn("refresh-token");

        MockHttpServletResponse response = new MockHttpServletResponse();


        String accessToken = "access-token";
        String refreshToken = "refresh-token";

        cookieUtils.setAuthCookies(response, accessToken, refreshToken);

        Cookie accessCookie = response.getCookie("access-token");
        Cookie refreshCookie = response.getCookie("refresh-token");

        Assertions.assertNotNull(accessCookie);
        Assertions.assertNotNull(refreshCookie);

        assertEquals(accessToken, accessCookie.getValue());
        assertEquals(refreshToken, refreshCookie.getValue());
    }

    @Test
    @DisplayName("Should clear both authentication cookies")
    void clearAuthCookies_shouldClearBothCookies() {
        when(cookieProperties.getAccessTokenName()).thenReturn("accessToken");
        when(cookieProperties.getRefreshTokenName()).thenReturn("refreshToken");
        MockHttpServletResponse response = new MockHttpServletResponse();

        cookieUtils.clearAuthCookies(response);
        List<String> setCookieHeaders = response.getHeaders(HttpHeaders.SET_COOKIE);
        System.out.println(setCookieHeaders);

        assertEquals(2, setCookieHeaders.size());

        assertTrue(setCookieHeaders.stream().anyMatch(cookie -> cookie.contains("accessToken")
                && cookie.contains("Max-Age=0")));

        assertTrue(setCookieHeaders.stream().anyMatch(cookie -> cookie.contains("refreshToken")
                && cookie.contains("Max-Age=0")));
    }

    @Test
    @DisplayName("Should return access token when cookie exists")
    void getAccessToken_shouldReturnTokenWhenCookieExists() {
        MockHttpServletRequest request = new MockHttpServletRequest();

        Cookie cookie = new Cookie("access-token", "test-access-token");
        request.setCookies(cookie);

        when(cookieProperties.getAccessTokenName()).thenReturn("access-token");

        String result = cookieUtils.getAccessToken(request);

        assertEquals("test-access-token", result);
    }

    @Test
    @DisplayName("Should return null when access token cookie does not exist")
    void getAccessToken_shouldReturnNullWhenCookieDoesNotExist() {
        MockHttpServletRequest request = new MockHttpServletRequest();

        when(cookieProperties.getAccessTokenName()).thenReturn("access-token");

        String result = cookieUtils.getAccessToken(request);

        assertNull(result);
    }
}
