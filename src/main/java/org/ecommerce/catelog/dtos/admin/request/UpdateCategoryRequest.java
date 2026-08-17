package org.ecommerce.catelog.dtos.admin.request;

import jakarta.validation.constraints.Size;
import org.ecommerce.common.validator.OptionalNotBlank.OptionalNotBlank;
import org.ecommerce.common.validator.ValidImage.ValidImage;
import org.springframework.web.multipart.MultipartFile;

public record UpdateCategoryRequest(
        @OptionalNotBlank(message = "Category name cannot be blank")
        @Size(max = 100, message = "Category name must not exceed 100 characters")
        String name,

        @ValidImage
        MultipartFile categoryImage,

        Boolean active
) {
}
