package org.ecommerce.catelog.service.admin;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ecommerce.catelog.dtos.admin.request.AddProductRequest;
import org.ecommerce.catelog.dtos.admin.request.AddProductVariants;
import org.ecommerce.catelog.dtos.admin.response.ProductResponse;
import org.ecommerce.catelog.dtos.admin.response.ProductVariantImageResponse;
import org.ecommerce.catelog.dtos.admin.response.ProductVariantResponse;
import org.ecommerce.catelog.entities.Category;
import org.ecommerce.catelog.entities.Product;
import org.ecommerce.catelog.entities.ProductVariant;
import org.ecommerce.catelog.entities.ProductVariantImage;
import org.ecommerce.catelog.repository.CategoryRepository;
import org.ecommerce.catelog.repository.ProductRepository;
import org.ecommerce.catelog.repository.ProductVariantImageRepository;
import org.ecommerce.catelog.repository.ProductVariantRepository;
import org.ecommerce.common.dtos.CloudinaryUploadResult;
import org.ecommerce.common.enums.CloudinaryFolder;
import org.ecommerce.common.exception.BadRequestException;
import org.ecommerce.common.exception.ResourceAlreadyExistsException;
import org.ecommerce.common.exception.ResourceNotFoundException;
import org.ecommerce.common.service.CloudinaryService;
import org.ecommerce.common.utils.SkuUtils;
import org.ecommerce.common.utils.SlugUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductVariantRepository productVariantRepository;
    private final ProductVariantImageRepository productVariantImageRepository;
    private final CloudinaryService cloudinaryService;
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
                .published(false)
                .build();


        productRepository.save(newProduct);

        return objectMapper.convertValue(newProduct, ProductResponse.class);
    }

    @Transactional
    public ProductVariantResponse addProductVariant(UUID productId, AddProductVariants addProductVariants) {
        Map<String, Object> attributes;
        try {
            attributes = objectMapper.readValue(
                    addProductVariants.attributes(),
                    new TypeReference<Map<String, Object>>() {
                    }
            );
        } catch (JsonProcessingException exception) {
            throw new BadRequestException("Invalid Attributes JSON");
        }
        Product productExisted = productRepository.findById(productId).orElseThrow(() -> {
            log.warn("not found");
            return new ResourceNotFoundException("Product not found");
        });
        String variantValue = attributes.values().stream().map(Objects::toString).collect(Collectors.joining("-"));
        String variantsSku = SkuUtils.generateSku(productExisted.getName(), variantValue);

        if (productVariantRepository.existsBySku(variantsSku)) {
            throw new ResourceAlreadyExistsException("SKU already exists");
        }

        ProductVariant variant = ProductVariant.builder()
                .productId(productExisted.getId())
                .sku(variantsSku)
                .price(addProductVariants.price())
                .stockQuantity(addProductVariants.stockQuantity())
                .attributes(attributes)
                .build();

        ProductVariant savedVariant = productVariantRepository.save(variant);

        List<ProductVariantImage> productVariantImages = uploadAndCreateImageRecords(savedVariant.getId(), addProductVariants.images());
        productVariantImageRepository.saveAll(productVariantImages);
        if (productExisted.getDefaultVariantId() == null) {
            productExisted.setDefaultVariantId(savedVariant.getId());
        }

        List<ProductVariantImageResponse> imageResponses = productVariantImages
                .stream().map(image -> objectMapper
                        .convertValue(image, ProductVariantImageResponse.class)
                ).toList();

        return new ProductVariantResponse(savedVariant.getId(), savedVariant.getSku(),
                savedVariant.getPrice(), savedVariant.getStockQuantity(),
                savedVariant.getAttributes(), savedVariant.isActive(), imageResponses);
    }

    private List<ProductVariantImage> uploadAndCreateImageRecords(UUID productVariantId, List<MultipartFile> images) {
        List<ProductVariantImage> imagesRecords = new ArrayList<>();
        for (int index = 0; index < images.size(); index++) {
            MultipartFile image = images.get(index);
            CloudinaryUploadResult uploadResult = cloudinaryService.uploadImage(image, CloudinaryFolder.PRODUCT_IMAGES);

            ProductVariantImage productVariantImage = ProductVariantImage.builder()
                    .productVariantId(productVariantId)
                    .imageUrl(uploadResult.secureUrl())
                    .imagePublicId(uploadResult.publicId())
                    .displayOrder(index + 1)
                    .primary(index == 0)
                    .build();

            imagesRecords.add(productVariantImage);
        }

        return imagesRecords;
    }

}
