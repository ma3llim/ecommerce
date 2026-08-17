package org.ecommerce.catelog.service.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ecommerce.catelog.dtos.admin.request.ProductFaqCreateRequest;
import org.ecommerce.catelog.dtos.admin.request.ProductFaqStatusRequest;
import org.ecommerce.catelog.dtos.admin.request.ProductFaqUpdateRequest;
import org.ecommerce.catelog.dtos.admin.response.ProductFaqResponse;
import org.ecommerce.catelog.entities.Product;
import org.ecommerce.catelog.entities.ProductFaq;
import org.ecommerce.catelog.repository.ProductFaqRepository;
import org.ecommerce.catelog.repository.ProductRepository;
import org.ecommerce.common.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminProductFaqService {
    private final ProductFaqRepository productFaqRepository;
    private final ProductRepository productRepository;
    private final ObjectMapper objectMapper;

    public ProductFaqResponse create(UUID productId, ProductFaqCreateRequest request) {
        Product product = productRepository.findById(productId).orElseThrow(() -> {
            log.warn("Cannot create product FAQ. Product not found. productId={}", productId);
            return new ResourceNotFoundException("Unable to create FAQ because product with id " + productId + " was not found."
            );
        });

        ProductFaq faq = ProductFaq.builder()
                .productId(product.getId()).question(request.question())
                .answer(request.answer()).active(true)
                .build();

        ProductFaq savedFaq = productFaqRepository.save(faq);

        log.info("Product FAQ created successfully. productId={}, faqId={}", productId, savedFaq.getId()
        );

        return objectMapper.convertValue(savedFaq, ProductFaqResponse.class);
    }

    public List<ProductFaqResponse> getAll(UUID productId) {
        if (!productRepository.existsById(productId)) {
            log.warn("Cannot fetch product FAQs. Product not found. productId={}", productId);
            throw new ResourceNotFoundException("Unable to fetch FAQs because product with id " + productId + " was not found."
            );
        }

        List<ProductFaqResponse> responses = productFaqRepository.findAllByProductIdOrderByCreatedAtAsc(productId)
                .stream()
                .map(productFaq -> objectMapper.convertValue(productFaq, ProductFaqResponse.class))
                .toList();

        log.info("Product FAQs fetched successfully. productId={}, faqCount={}", productId, responses.size());

        return responses;
    }

    public ProductFaqResponse getById(UUID productId, UUID faqId) {
        ProductFaq faq = productFaqRepository.findByIdAndProductId(faqId, productId).orElseThrow(() -> {
            log.warn("Product FAQ not found. productId={}, faqId={}", productId, faqId);
            return new ResourceNotFoundException("FAQ with id " + faqId + " was not found for product " + productId + ".");
        });

        log.info("Product FAQ fetched successfully. productId={}, faqId={}", productId, faqId);

        return objectMapper.convertValue(faq, ProductFaqResponse.class);
    }

    public ProductFaqResponse update(UUID productId, UUID faqId, ProductFaqUpdateRequest request) {
        ProductFaq faq = productFaqRepository.findByIdAndProductId(faqId, productId).orElseThrow(() -> {
            log.warn("Cannot update product FAQ. FAQ not found. productId={}, faqId={}", productId, faqId);
            return new ResourceNotFoundException("Unable to update FAQ because FAQ with id " + faqId + " was not found for product " + productId + "'.");
        });

        if (request.question() != null) {
            faq.setQuestion(request.question());
        }
        if (request.answer() != null) {
            faq.setAnswer(request.answer());
        }

        ProductFaq updatedFaq = productFaqRepository.save(faq);

        log.info("Product FAQ updated successfully. productId={}, faqId={}", productId, faqId);

        return objectMapper.convertValue(updatedFaq, ProductFaqResponse.class);
    }

    public void delete(UUID productId, UUID faqId) {
        ProductFaq faq = productFaqRepository.findByIdAndProductId(faqId, productId).orElseThrow(() -> {
            log.warn("Cannot delete product FAQ. FAQ not found. productId={}, faqId={}", productId, faqId);
            return new ResourceNotFoundException("Unable to delete FAQ because FAQ with id " + faqId + " was not found for product " + productId + ".");
        });

        productFaqRepository.delete(faq);

        log.info("Product FAQ deleted successfully. productId={}, faqId={}", productId, faqId);
    }

    public ProductFaqResponse updateStatus(UUID productId, UUID faqId, ProductFaqStatusRequest request) {
        ProductFaq faq = productFaqRepository.findByIdAndProductId(faqId, productId).orElseThrow(() -> {
            log.warn("Cannot update product FAQ status. FAQ not found. productId={}, faqId={}", productId, faqId);
            return new ResourceNotFoundException("Unable to update FAQ status because FAQ with id " + faqId + " was not found for product " + productId + ".");
        });

        faq.setActive(request.active());

        ProductFaq updatedFaq = productFaqRepository.save(faq);

        log.info("Product FAQ status updated successfully. productId={}, faqId={}, active={}", productId, faqId, updatedFaq.isActive());

        return objectMapper.convertValue(updatedFaq, ProductFaqResponse.class);
    }

}
