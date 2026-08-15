package org.ecommerce.catelog.repository;

import org.ecommerce.catelog.entities.ProductFaq;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductFaqRepository extends JpaRepository<ProductFaq, UUID> {
    List<ProductFaq> findAllByProductIdOrderByCreatedAtAsc(UUID productId);

    Optional<ProductFaq> findByIdAndProductId(UUID faqId, UUID productId);

    boolean existsByIdAndProductId(UUID faqId, UUID productId);
}
