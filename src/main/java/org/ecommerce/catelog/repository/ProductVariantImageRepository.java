package org.ecommerce.catelog.repository;

import org.ecommerce.catelog.entities.ProductVariantImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductVariantImageRepository extends JpaRepository<ProductVariantImage, UUID> {
    int countByProductVariantId(UUID variantId);

    Optional<ProductVariantImage> findByProductVariantIdAndPrimaryTrue(UUID variantId);

    List<ProductVariantImage> findAllByProductVariantId(UUID variantId);

    List<ProductVariantImage> findAllByProductVariantIdOrderByDisplayOrderAsc(UUID variantId);

    List<ProductVariantImage> findAllByProductVariantIdIn(Collection<UUID> variantIds);
}
