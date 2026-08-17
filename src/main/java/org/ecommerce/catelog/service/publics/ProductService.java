package org.ecommerce.catelog.service.publics;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ecommerce.catelog.dtos.publics.ProductListResponse;
import org.ecommerce.catelog.entities.Product;
import org.ecommerce.catelog.entities.ProductVariant;
import org.ecommerce.catelog.entities.ProductVariantImage;
import org.ecommerce.catelog.repository.CategoryRepository;
import org.ecommerce.catelog.repository.ProductRepository;
import org.ecommerce.catelog.repository.ProductVariantImageRepository;
import org.ecommerce.catelog.repository.ProductVariantRepository;
import org.ecommerce.common.dtos.PageResponse;
import org.ecommerce.common.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProductService {
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final ProductVariantImageRepository productVariantImageRepository;

    public PageResponse<ProductListResponse> allProducts(String category, Pageable pageable) {
        Page<Product> products;

        if (category != null && !category.isBlank()) {
            UUID categoryId = categoryRepository.findBySlugAndActiveTrue(category).orElseThrow(() -> {
                        log.warn("category not found category slug: {}", category);
                        return new ResourceNotFoundException("Category not found");
                    }
            ).getId();

            products = productRepository.findByCategoryIdAndPublishedTrue(categoryId, pageable);
        } else {
            products = productRepository.findByPublishedTrue(pageable);
        }

        List<UUID> defaultVariantIds = products.stream().map(Product::getDefaultVariantId).toList();
        List<ProductVariant> variants = defaultVariantIds.isEmpty() ? List.of() : productVariantRepository.findAllByIdInAndActiveTrue(defaultVariantIds);

        Map<UUID, ProductVariant> variantMap = variants.stream().collect(Collectors.toMap(
                ProductVariant::getId,
                Function.identity()
        ));

        List<UUID> variantIds = variants.stream().map(ProductVariant::getId).toList();
        List<ProductVariantImage> images = variantIds.isEmpty() ? List.of() : productVariantImageRepository.findAllByProductVariantIdInAndPrimaryTrue(variantIds);

        Map<UUID, String> imageMap = images.stream().collect(Collectors
                .toMap(ProductVariantImage::getProductVariantId, ProductVariantImage::getImageUrl));

        List<ProductListResponse> content = products.getContent().stream().map(product -> {
            ProductVariant variant = variantMap.get(product.getDefaultVariantId());
            String imageUrl = imageMap.get(product.getDefaultVariantId());

            return new ProductListResponse(
                    product.getId(),
                    product.getName(),
                    product.getDescription(),
                    product.getSlug(),
                    variant != null ? variant.getPrice() : null,
                    imageUrl
            );
        }).toList();

        return PageResponse.<ProductListResponse>builder()
                .content(content)
                .page(products.getNumber())
                .size(products.getSize())
                .totalElements(products.getTotalElements())
                .totalPages(products.getTotalPages())
                .first(products.isFirst())
                .last(products.isLast()).build();
    }
}
