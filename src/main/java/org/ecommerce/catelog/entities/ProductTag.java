package org.ecommerce.catelog.entities;


import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "product_tags")
@Builder
@EntityListeners(AuditingEntityListener.class)
public class ProductTag {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "tag_id", nullable = false)
    private UUID tagId;

    @CreatedDate
    private Instant createdAt;
}
