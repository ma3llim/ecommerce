package org.ecommerce.catelog.dtos.admin.request;

import jakarta.validation.constraints.NotNull;
import org.ecommerce.common.validator.ValidImage.ValidImage;
import org.springframework.web.multipart.MultipartFile;

public record ReplaceImage(
        @NotNull
        @ValidImage
        MultipartFile image
) {
}
