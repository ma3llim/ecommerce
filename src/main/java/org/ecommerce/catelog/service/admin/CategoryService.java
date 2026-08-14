package org.ecommerce.catelog.service.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.ecommerce.catelog.dtos.admin.request.CategoryRequestDto;
import org.ecommerce.catelog.dtos.admin.response.CategoryResponse;
import org.ecommerce.catelog.entities.Category;
import org.ecommerce.catelog.repository.CategoryRepository;
import org.ecommerce.common.dtos.CloudinaryUploadResult;
import org.ecommerce.common.dtos.PageResponse;
import org.ecommerce.common.enums.CloudinaryFolder;
import org.ecommerce.common.exception.ResourceAlreadyExistsException;
import org.ecommerce.common.exception.ResourceNotFoundException;
import org.ecommerce.common.service.CloudinaryService;
import org.ecommerce.common.utils.SlugUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final CloudinaryService cloudinaryService;
    private final ObjectMapper objectMapper;

    public CategoryResponse createCategory(CategoryRequestDto newCategory) {
        String categorySlug = SlugUtils.generateSlug(newCategory.name());

        boolean categoryExisted = categoryRepository.existsBySlug(categorySlug);
        if (categoryExisted) {
            throw new ResourceAlreadyExistsException("Category Slug is already existed");
        }
        CloudinaryUploadResult uploadResult = cloudinaryService.uploadImage(newCategory.categoryImage(), CloudinaryFolder.CATEGORY_IMAGES);

        Category newCategoryEntity = Category.builder()
                .name(newCategory.name())
                .slug(categorySlug)
                .imageUrl(uploadResult.secureUrl())
                .imagePublicId(uploadResult.publicId())
                .active(newCategory.active())
                .build();

        Category savedCategory = categoryRepository.save(newCategoryEntity);
        return objectMapper.convertValue(savedCategory, CategoryResponse.class);
    }

    public PageResponse<CategoryResponse> getAllCategories(Pageable pageable) {
        Page<Category> categories = categoryRepository.findAll(pageable);

        Page<CategoryResponse> categoriesInfoResponse = categories.map(
                category -> objectMapper.convertValue(category, CategoryResponse.class));

        return new PageResponse<>(
                categoriesInfoResponse.getContent(),
                categoriesInfoResponse.getNumber(),
                categoriesInfoResponse.getSize(),
                categoriesInfoResponse.getTotalElements(),
                categoriesInfoResponse.getTotalPages(),
                categoriesInfoResponse.isFirst(),
                categoriesInfoResponse.isLast()
        );
    }

    public CategoryResponse updateCategory(UUID categoryId, CategoryRequestDto categoryRequest) {
        String oldPublicId = "";
        Category categoryExisted = categoryRepository.findById(categoryId).orElseThrow(() -> {
            return new ResourceNotFoundException("Category is not found");
        });

        String categorySlug = SlugUtils.generateSlug(categoryRequest.name());
        boolean newSlugExisted = categoryRepository.existsBySlug(categorySlug);
        if (newSlugExisted) {
            throw new ResourceAlreadyExistsException("Category Slug is already existed");
        }

        if (categoryRequest.name() != null) {
            categoryExisted.setName(categoryRequest.name().trim());
            categoryExisted.setSlug(categorySlug);
        }
        if (categoryRequest.active() != null) categoryExisted.setActive(categoryRequest.active());
        if (categoryRequest.categoryImage() != null) {
            oldPublicId = categoryExisted.getImagePublicId();

            CloudinaryUploadResult cloudinaryUploadResult = cloudinaryService.uploadImage(categoryRequest.categoryImage(),
                    CloudinaryFolder.PROFILE_IMAGES);

            categoryExisted.setImageUrl(cloudinaryUploadResult.secureUrl());
            categoryExisted.setImagePublicId(cloudinaryUploadResult.publicId());
        }

        categoryRepository.save(categoryExisted);

        if (oldPublicId != null && oldPublicId.isEmpty()) {
            cloudinaryService.removeImage(oldPublicId);
        }

        return objectMapper.convertValue(categoryExisted, CategoryResponse.class);
    }

    public void deleteCategory(UUID categoryId) {
        Category categoryExisted = categoryRepository.findById(categoryId).orElseThrow(() -> {
            return new ResourceNotFoundException("Category is not found");
        });
        String categoryImagePublicId = categoryExisted.getImagePublicId();

        categoryRepository.deleteById(categoryId);
        cloudinaryService.removeImage(categoryImagePublicId);
    }
}
