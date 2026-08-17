package org.ecommerce.catelog.repository;

import org.ecommerce.catelog.entities.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface ProductVariantRepository extends JpaRepository<ProductVariant, UUID> {
    boolean existsBySku(String sku);

    List<ProductVariant> findAllByProductId(UUID productId);

    List<ProductVariant> findAllByIdInAndActiveTrue(Collection<UUID> ids);
}
