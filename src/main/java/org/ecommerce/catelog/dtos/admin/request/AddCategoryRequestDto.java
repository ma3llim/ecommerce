package org.ecommerce.catelog.dtos.admin.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.ecommerce.common.validator.ValidImage.ValidImage;
import org.springframework.web.multipart.MultipartFile;

public record AddCategoryRequestDto(
        @NotBlank(message = "Category name is required")
        @Size(max = 100, message = "Category name must not exceed 100 characters")
        String name,

        @NotNull(message = "Category image is required")
        @ValidImage
        MultipartFile categoryImage,

        @NotNull(message = "Active status is required")
        Boolean active
) {
}
