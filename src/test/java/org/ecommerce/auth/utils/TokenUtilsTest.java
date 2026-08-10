package org.ecommerce.auth.utils;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class TokenUtilsTest {
    @Test
    void generateOtp_whenCalled_returnsSixDigitOtp() {
        String otp = TokenUtils.generateOtp();

        assertNotNull(otp);
        assertEquals(6, otp.length());
        assertTrue(otp.matches("\\d{6}"));
    }


    @Test
    void generateOtp_whenCalledMultipleTimes_generatesDifferentOtps() {

        Set<String> otps = new HashSet<>();

        for (int i = 0; i < 100; i++) {
            otps.add(TokenUtils.generateOtp());
        }

        assertTrue(otps.size() > 1);
    }
}
