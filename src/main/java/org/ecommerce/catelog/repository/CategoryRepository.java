package org.ecommerce.catelog.repository;

import org.ecommerce.catelog.entities.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID> {
    boolean existsBySlug(String categorySlug);

    boolean existsBySlugAndIdNot(String categorySlug, UUID categoryId);
}
