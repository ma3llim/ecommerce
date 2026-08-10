package org.ecommerce.auth.repository;

import org.ecommerce.auth.entities.RefreshToken;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
public class RefreshTokenRepositoryTest {
    @Autowired
    private RefreshTokenRepository refreshTokenRepository;
    private RefreshToken refreshToken;

    @Test
    @DisplayName("Should return refresh tokens when user ID matches")
    void shouldReturnRefreshTokensWhenUserIdMatches() {
        UUID userId = UUID.randomUUID();
        UUID refreshTokenId = UUID.randomUUID();
        String refreshTokenValue = "refreshToken";

        refreshToken = RefreshToken.builder()
                .userId(userId)
                .id(refreshTokenId)
                .refreshToken(refreshTokenValue)
                .expiresAt(Instant.now())
                .build();
        refreshTokenRepository.save(refreshToken);

        List<RefreshToken> refreshTokenList = refreshTokenRepository.findAllByUserId(userId);

        assertEquals(1, refreshTokenList.size());
    }

    @Test
    @DisplayName("Should return empty list when user ID does not match")
    void shouldReturnEmptyListWhenUserIdDoesNotMatch() {
        UUID userId = UUID.randomUUID();
        UUID refreshTokenId = UUID.randomUUID();
        String refreshTokenValue = "refreshToken";

        refreshToken = RefreshToken.builder()
                .userId(userId)
                .id(refreshTokenId)
                .refreshToken(refreshTokenValue)
                .expiresAt(Instant.now())
                .build();
        refreshTokenRepository.save(refreshToken);

        List<RefreshToken> refreshTokenList = refreshTokenRepository.findAllByUserId(UUID.randomUUID());

        assertTrue(refreshTokenList.isEmpty());
    }
}
