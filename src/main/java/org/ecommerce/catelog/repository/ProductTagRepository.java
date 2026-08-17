package org.ecommerce.catelog.repository;

import org.ecommerce.catelog.entities.ProductTag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProductTagRepository extends JpaRepository<ProductTag, UUID> {

    void deleteAllByTagId(UUID tagId);
}
