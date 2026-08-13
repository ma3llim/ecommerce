package org.ecommerce.user.specification.admin;

import org.ecommerce.auth.entities.User;
import org.ecommerce.auth.enums.AccountStatus;
import org.springframework.data.jpa.domain.Specification;

public final class UserSpecification {
    private UserSpecification() {
    }

    public static Specification<User> search(String search) {
        return (root, query, criteriaBuilder) -> {
            if (search == null || search.isBlank()) {
                return null;
            }

            String value = "%" + search.toLowerCase() + "%";

            return criteriaBuilder.or(
                    criteriaBuilder.like(
                            criteriaBuilder.lower(root.get("email")),
                            value
                    ),
                    criteriaBuilder.like(
                            criteriaBuilder.lower(
                                    criteriaBuilder.concat(
                                            criteriaBuilder.concat(
                                                    root.get("firstName"),
                                                    " "
                                            ),
                                            root.get("lastName")
                                    )
                            ), value
                    )
            );
        };
    }

    public static Specification<User> hasStatus(AccountStatus accountStatus) {
        return (root, query, criteriaBuilder) -> {
            if (accountStatus == null) return null;

            return criteriaBuilder.equal(root.get("accountStatus"), accountStatus);
        };
    }
}
