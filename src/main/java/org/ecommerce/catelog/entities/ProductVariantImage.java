package org.ecommerce.catelog.entities;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "product_variant_images")
@EntityListeners(AuditingEntityListener.class)
public class ProductVariantImage {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "product_variant_id", nullable = false)
    private UUID productVariantId;

    @Column(nullable = false)
    private String imageUrl;

    @Column(nullable = false)
    private String imagePublicId;

    @Column(nullable = false)
    private int displayOrder;

    @Column(name = "is_primary", nullable = false)
    private boolean primary;

    @CreatedDate
    private Instant createdAt;
    @LastModifiedDate
    private Instant updatedAt;

    @Override
    public String toString() {
        return "ProductVariantImage{" +
                "id=" + id +
                ", productVariantId=" + productVariantId +
                ", imageUrl='" + imageUrl + '\'' +
                ", imagePublicId='" + imagePublicId + '\'' +
                ", displayOrder=" + displayOrder +
                ", primary=" + primary +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
