package org.ecommerce.user.repository;

import org.ecommerce.user.entity.UserAddress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserAddressRepository extends JpaRepository<UserAddress, UUID> {
    Optional<UserAddress> findByUserId(UUID userId);

    List<UserAddress> findAllByUserId(UUID userId);
}
