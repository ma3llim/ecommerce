package org.ecommerce.cart.repository;

import org.ecommerce.cart.entities.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CartItemRepository extends JpaRepository<CartItem, UUID> {
    List<CartItem> findByCartId(UUID cartId);

    Optional<CartItem> findByIdAndCartId(UUID id, UUID cartId);

    Optional<CartItem> findByCartIdAndProductVariantId(UUID cartId, UUID productVariantId);

    void deleteByCartId(UUID cartId);
}
