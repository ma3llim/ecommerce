package org.ecommerce.auth.repository;

import org.ecommerce.auth.entities.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
public class UserRepositoryTest {
    @Autowired
    private UserRepository userRepository;
    private User user;

    @Test
    @DisplayName("Should return user when email matches")
    void shouldReturnUserWhenMatchEmail() {
        user = User.builder().firstName("John").lastName("Doe").email("john@example.com").phoneNumber("9876543210")
                .build();

        userRepository.save(user);
        Optional<User> result = userRepository.findByEmail("john@example.com");

        assertTrue(result.isPresent());
        assertEquals(user.getEmail(), result.get().getEmail());
    }

    @Test
    @DisplayName("Should return empty when email does not match")
    void shouldReturnEmptyWhenEmailDoesNotMatch() {
        user = User.builder().firstName("John").lastName("Doe").email("john@example.com").phoneNumber("9876543210")
                .build();

        userRepository.save(user);
        Optional<User> result = userRepository.findByEmail("essam@example.com");

        assertTrue(result.isEmpty());
    }
}
