package org.ecommerce.coupon.repository;

import org.ecommerce.coupon.entities.Coupon;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CouponRepository extends JpaRepository<Coupon, UUID> {
    Optional<Coupon> findByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCase(String code);

    Page<Coupon> findByCodeContainingIgnoreCase(String code, Pageable pageable);

    Page<Coupon> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
