package org.ecommerce.common.dtos;

public record CloudinaryUploadResult(
        String secureUrl,
        String publicId
) {
}
