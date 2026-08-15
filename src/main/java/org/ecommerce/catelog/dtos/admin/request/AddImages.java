package org.ecommerce.catelog.dtos.admin.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import org.ecommerce.common.constants.AppConstants;
import org.ecommerce.common.validator.ValidImage.ValidImage;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public record AddImages(
        @NotEmpty(message = "At least one image is required")
        @ValidImage
        @Size(max = AppConstants.MAX_FILE_UPLOAD, message = "Maximum " + AppConstants.MAX_FILE_UPLOAD + " images allowed")
        List<MultipartFile> images
) {
}
