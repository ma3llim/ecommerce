package org.ecommerce.auth.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PasswordUtilsTest {
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private PasswordUtils passwordUtils;


    @Test
    void encode_whenValidPasswordProvided_returnsEncodedPassword() {
        String rawPassword = "password123";
        String encodedPassword = "$2a$10$encodedPassword";

        when(passwordEncoder.encode(rawPassword)).thenReturn(encodedPassword);

        String result = passwordUtils.encode(rawPassword);

        assertEquals(encodedPassword, result);
    }


    @Test
    void passwordMatches_whenPasswordIsCorrect_returnsTrue() {
        String rawPassword = "password123";
        String encodedPassword = "$2a$10$encodedPassword";

        when(passwordEncoder.matches(rawPassword, encodedPassword)).thenReturn(true);

        boolean result = passwordUtils.passwordMatches(rawPassword, encodedPassword);

        assertTrue(result);
    }


    @Test
    void passwordMatches_whenPasswordIsIncorrect_returnsFalse() {
        String rawPassword = "wrongPassword";
        String encodedPassword = "$2a$10$encodedPassword";

        when(passwordEncoder.matches(rawPassword, encodedPassword)).thenReturn(false);

        boolean result = passwordUtils.passwordMatches(rawPassword, encodedPassword);

        assertFalse(result);
    }
}
