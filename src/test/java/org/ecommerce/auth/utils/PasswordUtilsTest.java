package org.ecommerce.auth.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

public class PasswordUtilsTest {
    private final PasswordUtils passwordUtils = new PasswordUtils(new BCryptPasswordEncoder());

    @Test
    @DisplayName("Should return a hashed password")
    void shouldReturnHashPassword() {
        String password = "test@123";
        String hashPassword = passwordUtils.encode(password);
        assertNotEquals(password, hashPassword);
    }

    @Test
    @DisplayName("Should return true when password matches hash")
    void shouldReturnTrueWhenPasswordMatchHash() {
        String password = "test@123";
        String hashPassword = passwordUtils.encode(password);
        assertTrue(passwordUtils.passwordMatches(password, hashPassword));
    }

    @Test
    @DisplayName("should return false when password doesn't match")
    void shouldReturnFalseWhenPasswordDoestNotMatchHash() {
        String password = "test@123";
        String hashPassword = passwordUtils.encode(password);

        assertFalse(passwordUtils.passwordMatches("123", hashPassword));
    }
}
