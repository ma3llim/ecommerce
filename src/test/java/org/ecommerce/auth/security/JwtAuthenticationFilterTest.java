package org.ecommerce.auth.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import org.ecommerce.auth.entities.User;
import org.ecommerce.auth.enums.Role;
import org.ecommerce.auth.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class JwtAuthenticationFilterTest {
    @Mock
    private JwtService jwtService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private FilterChain filterChain;
    @Mock
    private Claims claims;
    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }


    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilter_whenAuthorizationHeaderIsMissing_continuesFilterChain() throws Exception {

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);

        assertNull(SecurityContextHolder.getContext().getAuthentication());

        verifyNoInteractions(jwtService);
        verifyNoInteractions(userRepository);
    }


    @Test
    void doFilter_whenAuthorizationHeaderIsNotBearer_continuesFilterChain() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        request.addHeader("Authorization", "Basic some-token");

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);

        assertNull(SecurityContextHolder.getContext().getAuthentication());

        verifyNoInteractions(jwtService);
        verifyNoInteractions(userRepository);
    }


    @Test
    void doFilter_whenValidAccessTokenAndUserExists_authenticatesUser() throws Exception {
        UUID userId = UUID.randomUUID();
        String accessToken = "valid-access-token";

        User user = mock(User.class);

        when(claims.getSubject())
                .thenReturn(userId.toString());

        when(jwtService.extractClaims(accessToken))
                .thenReturn(claims);

        when(jwtService.isAccessToken(claims))
                .thenReturn(true);

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(user.getRole())
                .thenReturn(Role.USER);

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        request.addHeader(
                "Authorization",
                "Bearer " + accessToken
        );

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        var authentication =
                SecurityContextHolder.getContext().getAuthentication();

        assertNotNull(authentication);

        assertInstanceOf(
                UsernamePasswordAuthenticationToken.class,
                authentication
        );

        assertSame(
                user,
                authentication.getPrincipal()
        );

        assertTrue(
                authentication.getAuthorities()
                        .stream()
                        .anyMatch(authority ->
                                authority.getAuthority().equals("ROLE_USER")
                        )
        );

        verify(userRepository).findById(userId);
        verify(filterChain).doFilter(request, response);
    }


    @Test
    void doFilter_whenTokenIsNotAccessToken_doesNotAuthenticateUser() throws Exception {

        String refreshToken = "refresh-token";

        when(jwtService.extractClaims(refreshToken))
                .thenReturn(claims);

        when(jwtService.isAccessToken(claims))
                .thenReturn(false);

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        request.addHeader(
                "Authorization",
                "Bearer " + refreshToken
        );

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        assertNull(
                SecurityContextHolder.getContext().getAuthentication()
        );

        verify(jwtService).extractClaims(refreshToken);
        verify(jwtService).isAccessToken(claims);

        verifyNoInteractions(userRepository);

        verify(filterChain).doFilter(request, response);
    }


    @Test
    void doFilter_whenUserDoesNotExist_doesNotAuthenticateUser() throws Exception {

        UUID userId = UUID.randomUUID();
        String accessToken = "valid-access-token";

        when(jwtService.extractClaims(accessToken))
                .thenReturn(claims);

        when(jwtService.isAccessToken(claims))
                .thenReturn(true);

        when(claims.getSubject())
                .thenReturn(userId.toString());

        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        request.addHeader(
                "Authorization",
                "Bearer " + accessToken
        );

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        assertNull(
                SecurityContextHolder.getContext().getAuthentication()
        );

        verify(userRepository).findById(userId);
        verify(filterChain).doFilter(request, response);
    }


    @Test
    void doFilter_whenAuthenticationAlreadyExists_doesNotOverwriteAuthentication() throws Exception {

        UUID userId = UUID.randomUUID();
        String accessToken = "valid-access-token";

        User existingUser = mock(User.class);
        User jwtUser = mock(User.class);

        UsernamePasswordAuthenticationToken existingAuthentication =
                new UsernamePasswordAuthenticationToken(
                        existingUser,
                        null,
                        java.util.List.of()
                );

        SecurityContextHolder.getContext()
                .setAuthentication(existingAuthentication);

        when(jwtService.extractClaims(accessToken))
                .thenReturn(claims);

        when(jwtService.isAccessToken(claims))
                .thenReturn(true);

        when(claims.getSubject())
                .thenReturn(userId.toString());

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(jwtUser));

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        request.addHeader(
                "Authorization",
                "Bearer " + accessToken
        );

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        assertSame(
                existingAuthentication,
                SecurityContextHolder.getContext().getAuthentication()
        );

        verify(filterChain).doFilter(request, response);
    }


    @Test
    void doFilter_whenJwtProcessingFails_doesNotAuthenticateUser() throws Exception {

        String invalidToken = "invalid-token";

        when(jwtService.extractClaims(invalidToken))
                .thenThrow(new RuntimeException("Invalid JWT"));

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        request.addHeader(
                "Authorization",
                "Bearer " + invalidToken
        );

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        assertNull(
                SecurityContextHolder.getContext().getAuthentication()
        );

        verify(jwtService).extractClaims(invalidToken);

        verify(filterChain).doFilter(request, response);

        verifyNoInteractions(userRepository);
    }


    @Test
    void doFilter_whenUserHasNoRole_authenticatesUserWithoutAuthorities() throws Exception {

        UUID userId = UUID.randomUUID();
        String accessToken = "valid-access-token";

        User user = mock(User.class);

        when(jwtService.extractClaims(accessToken))
                .thenReturn(claims);

        when(jwtService.isAccessToken(claims))
                .thenReturn(true);

        when(claims.getSubject())
                .thenReturn(userId.toString());

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(user.getRole())
                .thenReturn(null);

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        request.addHeader(
                "Authorization",
                "Bearer " + accessToken
        );

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        var authentication =
                SecurityContextHolder.getContext().getAuthentication();

        assertNotNull(authentication);

        assertTrue(
                authentication.getAuthorities().isEmpty()
        );

        assertSame(
                user,
                authentication.getPrincipal()
        );

        verify(filterChain).doFilter(request, response);
    }
}
