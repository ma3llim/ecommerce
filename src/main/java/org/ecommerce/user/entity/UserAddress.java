package org.ecommerce.user.entity;

import jakarta.persistence.*;
import lombok.*;
import org.ecommerce.user.enums.AddressType;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "user_addresses")
@EntityListeners(AuditingEntityListener.class)
public class UserAddress {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private UUID userId;
    private String fullName;
    private String phoneNumber;
    private String addressLineOne;
    private String addressLineTwo;
    private String city;
    private String state;
    private String country;
    private String postalCode;

    @Enumerated(EnumType.STRING)
    private AddressType addressType;
    private boolean defaultShipping;
    private boolean defaultBilling;
    @CreatedDate
    private Instant createdAt;
    @LastModifiedDate
    private Instant updatedAt;

    public String getFullAddress() {
        return Stream.of(
                        addressLineOne,
                        addressLineTwo,
                        city,
                        state,
                        country,
                        postalCode
                ).filter(Objects::nonNull)
                .filter(value -> !value.isBlank())
                .collect(Collectors.joining(", "));
    }
}
