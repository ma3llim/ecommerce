package org.ecommerce.common.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.ecommerce.common.dtos.CloudinaryUploadResult;
import org.ecommerce.common.enums.CloudinaryFolder;
import org.ecommerce.common.exception.FileStorageException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CloudinaryService {
    private final Cloudinary cloudinary;

    public CloudinaryUploadResult uploadImage(MultipartFile file, CloudinaryFolder folder) {
        try {

            Map<?, ?> result = cloudinary.uploader().upload(file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", folder.getPath(),
                            "resource_type", "image",
                            "format", "webp"
                    ));

            String secureUrl = result.get("secure_url").toString();
            String publicId = result.get("public_id").toString();

            return new CloudinaryUploadResult(secureUrl, publicId);
        } catch (IOException e) {
            throw new FileStorageException("Failed to upload image", e);
        }
    }

    public boolean removeImage(String publicId) {
        if (publicId == null || publicId.isBlank()) {
            return false;
        }

        try {
            Map<?, ?> result = cloudinary.uploader().destroy(publicId,
                    ObjectUtils.asMap("resource_type", "image")
            );
            return "ok".equals(result.get("result"));
        } catch (IOException e) {
            throw new FileStorageException("Failed to delete image", e);
        }
    }
}
