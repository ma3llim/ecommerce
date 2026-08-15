package org.ecommerce.catelog.service.admin;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ecommerce.catelog.dtos.admin.request.*;
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
import org.ecommerce.common.constants.AppConstants;
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
        // remove this after frontend send valid data
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

        List<ProductVariantImage> productVariantImages = List.of();
        if (addProductVariants.images() != null && !addProductVariants.images().isEmpty()) {
            productVariantImages = uploadAndCreateImageRecords(savedVariant.getId(), addProductVariants.images());
            productVariantImageRepository.saveAll(productVariantImages);
            if (productExisted.getDefaultVariantId() == null) {
                productExisted.setDefaultVariantId(savedVariant.getId());
            }

        }
        List<ProductVariantImageResponse> imageResponses = productVariantImages.stream()
                .map(image -> objectMapper.convertValue(image, ProductVariantImageResponse.class))
                .toList();

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

    public ProductResponse updateProduct(UUID productId, @Valid UpdateProduct productRequest) {
        Product productExisted = productRepository.findById(productId).orElseThrow(() -> {
            log.warn("Product not found: {}", productId);
            return new ResourceNotFoundException("Product not found");
        });

        if (productRequest.categoryId() != null) productExisted.setCategoryId(productRequest.categoryId());
        if (productRequest.name() != null) {
            String productSlug = SlugUtils.generateSlug(productRequest.name());

            if (productRepository.existsBySlugAndIdNot(productSlug, productId)) {
                throw new ResourceAlreadyExistsException("Product with this name already exists");
            }

            productExisted.setName(productRequest.name());
            productExisted.setSlug(productSlug);
        }
        if (productRequest.description() != null) productExisted.setDescription(productRequest.description());
        if (productRequest.specifications() != null) productExisted.setSpecifications(productRequest.specifications());

        productRepository.save(productExisted);

        return objectMapper.convertValue(productExisted, ProductResponse.class);
    }

    public ProductVariantResponse updateProductVariant(UUID productId, UUID variantId, @Valid UpdateProductVariant productVariant) {
        Product productExisted = productRepository.findById(productId).orElseThrow(() -> {
            log.warn("Product not found: {}", productId);
            return new ResourceNotFoundException("Product not found");
        });
        ProductVariant productVariantExisted = productVariantRepository.findById(variantId).orElseThrow(() -> {
            log.warn("Variant not found: {}", variantId);
            return new ResourceNotFoundException("Variant not found");
        });

        if (productVariant.price() != null) productVariantExisted.setPrice(productVariant.price());
        if (productVariant.stockQuantity() != null)
            productVariantExisted.setStockQuantity(productVariant.stockQuantity());
        if (productVariant.attributes() != null) productVariantExisted.setAttributes(productVariant.attributes());

        productVariantRepository.save(productVariantExisted);

        return objectMapper.convertValue(productVariantExisted, ProductVariantResponse.class);
    }

    public List<ProductVariantImageResponse> uploadsImage(UUID productId, UUID variantId, @Valid AddImages images) {
        Product productExisted = productRepository.findById(productId).orElseThrow(() -> {
            log.warn("Product not found. productId={}", productId);
            return new ResourceNotFoundException("Product not found");
        });
        ProductVariant productVariantExisted = productVariantRepository.findById(variantId).orElseThrow(() -> {
            log.warn("Variant not found. variantId={}", variantId);
            return new ResourceNotFoundException("Variant not found");
        });

        int existingImageCount = productVariantImageRepository.countByProductVariantId(variantId);
        log.debug("Existing variant image count. variantId={}, existingImageCount={}", variantId, existingImageCount);


        if (existingImageCount > AppConstants.MAX_FILE_UPLOAD) {
            log.warn("Maximum image limit reached. variantId={}, existingImageCount={}, maxAllowed={}", variantId, existingImageCount, AppConstants.MAX_FILE_UPLOAD);
            throw new ResourceAlreadyExistsException("Maximum 5 image allowed");
        }
        int totalImage = existingImageCount + images.images().size();
        log.info(String.valueOf(totalImage));
        if (totalImage > AppConstants.MAX_FILE_UPLOAD) {
            log.warn("Image upload rejected. variantId={}, existingImageCount={}, requestedImageCount={}, totalImageCount={}, maxAllowed={}", variantId, existingImageCount, images.images().size(), totalImage, AppConstants.MAX_FILE_UPLOAD);
            throw new ResourceAlreadyExistsException("Maximum 5 images allowed");
        }

        List<ProductVariantImage> productVariantImages = new ArrayList<>();
        for (int i = 0; i < images.images().size(); i++) {
            MultipartFile image = images.images().get(i);
            CloudinaryUploadResult uploadResult = cloudinaryService.uploadImage(image, CloudinaryFolder.PRODUCT_IMAGES);
            int displayOrder = existingImageCount + i + 1;
            boolean primary = existingImageCount == 0 && i == 0;

            ProductVariantImage imageRecord = ProductVariantImage.builder()
                    .productVariantId(variantId)
                    .imageUrl(uploadResult.secureUrl())
                    .imagePublicId(uploadResult.publicId())
                    .displayOrder(displayOrder)
                    .primary(primary)
                    .build();

            productVariantImages.add(imageRecord);
        }

        productVariantImageRepository.saveAll(productVariantImages);

        return objectMapper.convertValue(
                productVariantImages,
                new TypeReference<List<ProductVariantImageResponse>>() {
                });
    }

    public ProductVariantImageResponse replaceImage(UUID productId, UUID variantId, UUID variantImageId, ReplaceImage image) {
        Product productExisted = productRepository.findById(productId).orElseThrow(() -> {
            log.warn("Product not found. productId={}", productId);
            return new ResourceNotFoundException("Product not found");
        });
        ProductVariant productVariantExisted = productVariantRepository.findById(variantId).orElseThrow(() -> {
            log.warn("Variant not found. variantId={}", variantId);
            return new ResourceNotFoundException("Variant not found");
        });

        ProductVariantImage productVariantImage = productVariantImageRepository.findById(variantImageId).orElseThrow(() -> {
            log.warn("Variant image not found. variantImageId={}", variantImageId);
            return new ResourceNotFoundException("variantImageId not found");
        });

        String oldPublicId = productVariantImage.getImagePublicId();

        CloudinaryUploadResult uploadResult = cloudinaryService.uploadImage(image.image(), CloudinaryFolder.PRODUCT_IMAGES);

        productVariantImage.setImageUrl(uploadResult.secureUrl());
        productVariantImage.setImagePublicId(uploadResult.publicId());

        productVariantImageRepository.save(productVariantImage);
        if (oldPublicId != null && !oldPublicId.isEmpty()) {
            cloudinaryService.removeImage(oldPublicId);
        }

        return objectMapper.convertValue(productVariantImage, ProductVariantImageResponse.class);
    }

    public ProductVariantImageResponse setVariantImagePrimary(UUID productId, UUID variantId, UUID variantImageId) {
        Product productExisted = productRepository.findById(productId).orElseThrow(() -> {
            log.warn("Product not found. productId={}", productId);
            return new ResourceNotFoundException("Product not found");
        });
        ProductVariant productVariantExisted = productVariantRepository.findById(variantId).orElseThrow(() -> {
            log.warn("Variant not found. variantId={}", variantId);
            return new ResourceNotFoundException("Variant not found");
        });

        ProductVariantImage productVariantImage = productVariantImageRepository.findById(variantImageId).orElseThrow(() -> {
            log.warn("Variant image not found. variantImageId={}", variantImageId);
            return new ResourceNotFoundException("variantImageId not found");
        });

        productVariantImageRepository.findByProductVariantIdAndPrimaryTrue(variantId)
                .ifPresent(currentPrimary -> currentPrimary.setPrimary(false));

        productVariantImage.setPrimary(true);

        productVariantImageRepository.save(productVariantImage);

        return objectMapper.convertValue(productVariantImage, ProductVariantImageResponse.class);
    }

    public List<ProductVariantImageResponse> reorderImages(UUID productId, UUID variantId, ReorderImages reorderImages) {
        Product productExisted = productRepository.findById(productId).orElseThrow(() -> {
            log.warn("Product not found. productId={}", productId);
            return new ResourceNotFoundException("Product not found");
        });
        ProductVariant productVariantExisted = productVariantRepository.findById(variantId).orElseThrow(() -> {
            log.warn("Variant not found. variantId={}", variantId);
            return new ResourceNotFoundException("Variant not found");
        });

        List<ProductVariantImage> existingImages = productVariantImageRepository.findAllByProductVariantId(productVariantExisted.getId());

        List<UUID> requestedIds = reorderImages.imageIds();
        // Check duplicate IDs
        Set<UUID> uniqueIds = new HashSet<>(requestedIds);

        if (uniqueIds.size() != requestedIds.size()) {
            throw new BadRequestException("Duplicate image IDs are not allowed");
        }
        // Get IDs that actually exist in DB
        Set<UUID> existingIds = existingImages.stream().map(ProductVariantImage::getId).collect(Collectors.toSet());
        if (!existingIds.containsAll(requestedIds)) {
            throw new BadRequestException("One or more image IDs do not belong to this variant");
        }

        // Check missing image IDs
        if (requestedIds.size() != existingIds.size()) {
            throw new BadRequestException("All variant images must be included when reordering");
        }

        Map<UUID, ProductVariantImage> imageMap = existingImages.stream().collect(Collectors.toMap(ProductVariantImage::getId, image -> image));
        // assign new display order
        for (int i = 0; i < requestedIds.size(); i++) {
            UUID imageId = requestedIds.get(i);
            ProductVariantImage image = imageMap.get(imageId);
            image.setDisplayOrder(i + 1);
        }
        List<ProductVariantImage> savedImages = productVariantImageRepository.saveAll(existingImages);
        return objectMapper.convertValue(
                savedImages,
                new TypeReference<List<ProductVariantImageResponse>>() {
                }
        );
    }

    @Transactional
    public void deleteVariantImage(UUID productId, UUID variantId, UUID imageVariantId) {
        Product productExisted = productRepository.findById(productId).orElseThrow(() -> {
            log.warn("Product not found. productId={}", productId);
            return new ResourceNotFoundException("Product not found");
        });

        ProductVariant productVariantExisted = productVariantRepository.findById(variantId).orElseThrow(() -> {
            log.warn("Variant not found. variantId={}", variantId);
            return new ResourceNotFoundException("Variant not found");
        });

        if (!productVariantExisted.getProductId().equals(productExisted.getId())) {
            throw new ResourceNotFoundException("Variant not found for this product");
        }

        ProductVariantImage productVariantImageExisted = productVariantImageRepository.findById(imageVariantId)
                .orElseThrow(() -> {
                    log.warn("Variant image not found. imageId={}, variantId={}", imageVariantId, variantId);
                    return new ResourceNotFoundException("Variant image not found");
                });

        if (!productVariantImageExisted.getProductVariantId().equals(productVariantExisted.getId())) {
            throw new ResourceNotFoundException("Variant Image is not found by product variant");
        }

        boolean deletedImageWasPrimary = productVariantImageExisted.isPrimary();

        cloudinaryService.removeImage(productVariantImageExisted.getImagePublicId());

        productVariantImageRepository.delete(productVariantImageExisted);

        List<ProductVariantImage> remainingImages = productVariantImageRepository.findAllByProductVariantIdOrderByDisplayOrderAsc(variantId);

        for (int i = 0; i < remainingImages.size(); i++) {
            ProductVariantImage remainingImage = remainingImages.get(i);

            remainingImage.setDisplayOrder(i + 1);

            if (deletedImageWasPrimary) {
                remainingImage.setPrimary(i == 0);
            }
        }
        productVariantImageRepository.saveAll(remainingImages);
    }
}
