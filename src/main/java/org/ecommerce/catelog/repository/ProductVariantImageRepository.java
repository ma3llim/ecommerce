package org.ecommerce.catelog.repository;

import org.ecommerce.catelog.entities.ProductVariantImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProductVariantImageRepository extends JpaRepository<ProductVariantImage, UUID> {
}
