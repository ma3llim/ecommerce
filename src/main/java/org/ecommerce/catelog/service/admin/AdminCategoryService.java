package org.ecommerce.catelog.service.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.ecommerce.catelog.dtos.admin.request.AddCategoryRequestDto;
import org.ecommerce.catelog.dtos.admin.response.CategoryResponse;
import org.ecommerce.catelog.entities.Category;
import org.ecommerce.catelog.repository.CategoryRepository;
import org.ecommerce.common.dtos.CloudinaryUploadResult;
import org.ecommerce.common.dtos.PageResponse;
import org.ecommerce.common.enums.CloudinaryFolder;
import org.ecommerce.common.exception.ResourceAlreadyExistsException;
import org.ecommerce.common.service.CloudinaryService;
import org.ecommerce.common.utils.SlugUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminCategoryService {
    private final CategoryRepository categoryRepository;
    private final CloudinaryService cloudinaryService;
    private final ObjectMapper objectMapper;

    public CategoryResponse createCategory(AddCategoryRequestDto newCategory) {
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
                .active(true)
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
}
