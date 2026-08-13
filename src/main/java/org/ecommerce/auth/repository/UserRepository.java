package org.ecommerce.auth.repository;

import org.ecommerce.auth.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID>, JpaSpecificationExecutor<User> {
    Optional<User> findByEmail(String email);

    boolean existsByPhoneNumberIgnoreCaseAndIdNot(String phoneNumber, UUID userId);
}
