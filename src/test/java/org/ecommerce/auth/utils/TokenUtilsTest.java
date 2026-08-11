package org.ecommerce.auth.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TokenUtilsTest {
    @Test
    @DisplayName("Should generate OTP with exactly 6 digits")
    void shouldGenerateOtpWithExactlySixDigits() {
        String otp = TokenUtils.generateOtp();

        assertEquals(6, otp.length());
    }

    @Test
    @DisplayName("Should generate OTP within valid range")
    void shouldGenerateOtpWithinValidRange() {
        int otp = Integer.parseInt(TokenUtils.generateOtp());
        assertTrue(otp >= 100000);
        assertTrue(otp <= 999999);
    }
}
