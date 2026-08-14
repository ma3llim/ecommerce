package org.ecommerce.catelog.repository;

import org.ecommerce.catelog.entities.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {
    boolean existsBySlug(String productSlug);

    boolean existsBySlugAndIdNot(String productSlug, UUID productID);
}
