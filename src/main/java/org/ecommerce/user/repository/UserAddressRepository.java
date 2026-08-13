package org.ecommerce.user.repository;

import org.ecommerce.user.entity.UserAddress;
import org.ecommerce.user.enums.AddressType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserAddressRepository extends JpaRepository<UserAddress, UUID> {
    Optional<UserAddress> findByUserId(UUID userId);

    List<UserAddress> findAllByUserId(UUID userId);

    boolean existsByUserId(UUID userId);

    @Modifying
    @Query(""" 
            UPDATE UserAddress a SET a.defaultShipping = false
            WHERE a.userId = :userId AND a.defaultShipping = true
            """)
    void removeDefaultShipping(@Param("userId") UUID userId);

    @Modifying
    @Query("""
                UPDATE UserAddress a SET a.defaultBilling = false
                WHERE a.userId = :userId AND a.defaultBilling = true
            """)
    void removeDefaultBilling(@Param("userId") UUID userId);

    boolean existsByUserIdAndAddressType(UUID userId, AddressType addressType);
}
