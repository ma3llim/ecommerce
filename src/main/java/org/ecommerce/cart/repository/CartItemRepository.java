package org.ecommerce.cart.repository;

import org.ecommerce.cart.entities.CartItem;
import org.ecommerce.cart.repository.projection.CartOrderItemProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CartItemRepository extends JpaRepository<CartItem, UUID> {
    List<CartItem> findByCartId(UUID cartId);

    Optional<CartItem> findByIdAndCartId(UUID id, UUID cartId);

    Optional<CartItem> findByCartIdAndProductVariantId(UUID cartId, UUID productVariantId);

    void deleteByCartId(UUID cartId);

    @Query(value = """
            SELECT
                ci.id AS cartItemId,
                ci.product_variant_id AS productVariantId,
                pv.product_id AS productId,
                p.name AS productName,
                pv.price AS unitPrice,
                pv.stock_quantity AS stockQuantity,
                ci.quantity AS quantity,
                pv.sku AS sku
            FROM cart_items ci
            INNER JOIN product_variants pv
                ON pv.id = ci.product_variant_id
            INNER JOIN products p
                ON p.id = pv.product_id
            WHERE ci.cart_id = :cartId
              AND p.is_published = true
            """, nativeQuery = true)
    List<CartOrderItemProjection> findOrderItemsByCartId(@Param("cartId") UUID cartId);
}
