package org.ecommerce.catelog.dtos.admin.request;

import jakarta.validation.constraints.*;
import org.ecommerce.common.constants.AppConstants;
import org.ecommerce.common.validator.ValidImage.ValidImage;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

public record AddProductVariants(
        @NotNull
        @DecimalMin(value = "0.0", inclusive = false)
        BigDecimal price,

        @Min(0)
        int stockQuantity,

        @NotBlank
        String attributes,

        @ValidImage
        @Size(max = AppConstants.MAX_FILE_UPLOAD, message = "Maximum " + AppConstants.MAX_FILE_UPLOAD + " images allowed")
        List<MultipartFile> images
) {
}
