package org.ecommerce.auth.security;

import org.ecommerce.auth.entities.User;
import org.ecommerce.auth.enums.AccountStatus;
import org.ecommerce.auth.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CustomUserDetailsTest {
    private User user;
    private CustomUserDetails userDetails;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setEmail("test@example.com");
        user.setPassword("encoded-password");
        user.setRole(Role.USER);
        user.setAccountStatus(AccountStatus.ACTIVE);
        userDetails = new CustomUserDetails(user);
    }

    @Test
    void getAuthorities_whenUserHasRole_returnsCorrectRoleAuthority() {

        var authorities = userDetails.getAuthorities();

        assertEquals(1, authorities.size());
        assertEquals("ROLE_USER", authorities.iterator().next().getAuthority());
    }


    @Test
    void getPassword_whenUserHasPassword_returnsUserPassword() {
        assertEquals("encoded-password", userDetails.getPassword());
    }


    @Test
    void getUsername_whenUserHasEmail_returnsUserEmail() {
        assertEquals("test@example.com", userDetails.getUsername());
    }

    @Test
    void isAccountNonLocked_whenAccountIsActive_returnsTrue() {
        assertTrue(userDetails.isAccountNonLocked());
    }


    @Test
    void isAccountNonLocked_whenAccountIsNotActive_returnsFalse() {
        user.setAccountStatus(AccountStatus.DISABLED);
        assertFalse(userDetails.isAccountNonLocked());
    }


    @Test
    void isEnabled_whenAccountIsActive_returnsTrue() {
        assertTrue(userDetails.isEnabled());
    }


    @Test
    void isEnabled_whenAccountIsNotActive_returnsFalse() {
        user.setAccountStatus(AccountStatus.PENDING);
        assertFalse(userDetails.isEnabled());
    }
}
