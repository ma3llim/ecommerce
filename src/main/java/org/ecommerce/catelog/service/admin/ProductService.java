package org.ecommerce.catelog.service.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ecommerce.catelog.dtos.admin.request.AddProductRequest;
import org.ecommerce.catelog.dtos.admin.response.ProductResponse;
import org.ecommerce.catelog.entities.Category;
import org.ecommerce.catelog.entities.Product;
import org.ecommerce.catelog.repository.CategoryRepository;
import org.ecommerce.catelog.repository.ProductRepository;
import org.ecommerce.common.exception.ResourceAlreadyExistsException;
import org.ecommerce.common.exception.ResourceNotFoundException;
import org.ecommerce.common.utils.SlugUtils;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ObjectMapper objectMapper;

    public ProductResponse createProduct(AddProductRequest productRequest) {
        Category category = categoryRepository.findById(productRequest.categoryId()).orElseThrow(() -> {
            log.warn("Create product failed request: category not found categoryId:{}", productRequest.categoryId());
            return new ResourceNotFoundException("Category not found");
        });

        String productSlug = SlugUtils.generateSlug(productRequest.name());
        if (productRepository.existsBySlug(productSlug)) {
            log.warn("Create product request rejected: product slug already exists, slug={}", productSlug);
            throw new ResourceAlreadyExistsException("Product Slug is already existed");
        }

        Product newProduct = Product.builder()
                .categoryId(category.getId())
                .name(productRequest.name())
                .slug(productSlug)
                .description(productRequest.description())
                .specifications(productRequest.specifications())
                .active(false)
                .build();


        productRepository.save(newProduct);

        return objectMapper.convertValue(newProduct, ProductResponse.class);
    }
}
