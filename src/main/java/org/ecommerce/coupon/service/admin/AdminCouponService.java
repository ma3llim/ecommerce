package org.ecommerce.coupon.service.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ecommerce.common.dtos.PageResponse;
import org.ecommerce.common.enums.DiscountType;
import org.ecommerce.common.enums.VisibleStatus;
import org.ecommerce.common.exception.BadRequestException;
import org.ecommerce.common.exception.ResourceAlreadyExistsException;
import org.ecommerce.common.exception.ResourceNotFoundException;
import org.ecommerce.coupon.dtos.admin.request.CreateCouponRequest;
import org.ecommerce.coupon.dtos.admin.request.UpdateCouponRequest;
import org.ecommerce.coupon.dtos.admin.request.UpdateCouponStatusRequest;
import org.ecommerce.coupon.dtos.admin.response.CouponResponse;
import org.ecommerce.coupon.entities.Coupon;
import org.ecommerce.coupon.repository.CouponRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminCouponService {
    private final CouponRepository couponRepository;
    private final ObjectMapper objectMapper;

    public CouponResponse createCoupon(CreateCouponRequest request) {
        String code = normalizeCode(request.code());

        if (couponRepository.existsByCodeIgnoreCase(code)) {
            log.warn("Create coupon rejected: coupon code already exists. code={}", code);
            throw new ResourceAlreadyExistsException("Coupon code already exists");
        }

        validateCouponData(request.discountType(), request.discountValue(), request.validFrom(), request.validUntil());

        Coupon coupon = Coupon.builder()
                .code(code)
                .name(request.name().trim())
                .description(request.description())
                .discountType(request.discountType())
                .discountValue(request.discountValue())
                .minimumOrderAmount(request.minimumOrderAmount())
                .maximumDiscountAmount(request.maximumDiscountAmount())
                .usageLimit(request.usageLimit())
                .usedCount(0)
                .validFrom(request.validFrom())
                .validUntil(request.validUntil())
                .active(true)
                .build();

        Coupon savedCoupon = couponRepository.save(coupon);

        log.info("Coupon created successfully. couponId={}, code={}", savedCoupon.getId(), savedCoupon.getCode());

        return objectMapper.convertValue(savedCoupon, CouponResponse.class);
    }

    public PageResponse<CouponResponse> getCoupons(String search, Pageable pageable) {
        Page<Coupon> couponPage;

        if (search != null && !search.isBlank()) {
            couponPage = couponRepository.findByCodeContainingIgnoreCase(search.trim(), pageable);
        } else {
            couponPage = couponRepository.findAll(pageable);
        }

        List<CouponResponse> responses = couponPage.getContent().stream()
                .map(coupon -> objectMapper.convertValue(coupon, CouponResponse.class)).toList();

        return new PageResponse<>(
                responses,
                couponPage.getNumber(),
                couponPage.getSize(),
                couponPage.getTotalElements(),
                couponPage.getTotalPages(),
                couponPage.isFirst(),
                couponPage.isLast()
        );
    }

    public CouponResponse updateCoupon(UUID codeId, UpdateCouponRequest request) {
        Coupon coupon = couponRepository.findById(codeId).orElseThrow(() -> {
            log.warn("Update coupon failed: coupon not found. codeId={}", codeId);
            return new ResourceNotFoundException("Coupon not found");
        });

        validateCouponData(request.discountType(), request.discountValue(), request.validFrom(), request.validUntil());

        if (request.usageLimit() != null && request.usageLimit() < coupon.getUsedCount()) {
            log.warn("Update coupon failed: usage limit is less than used count. couponId={}, usedCount={}, requestedLimit={}",
                    coupon.getId(), coupon.getUsedCount(), request.usageLimit()
            );
            throw new BadRequestException("Usage limit cannot be less than used count");
        }

        if (request.name() != null) {
            coupon.setName(request.name().trim());
        }

        if (request.description() != null) {
            coupon.setDescription(request.description());
        }

        if (request.discountType() != null) {
            coupon.setDiscountType(request.discountType());
        }

        if (request.discountValue() != null) {
            coupon.setDiscountValue(request.discountValue());
        }

        if (request.minimumOrderAmount() != null) {
            coupon.setMinimumOrderAmount(request.minimumOrderAmount());
        }

        if (request.maximumDiscountAmount() != null) {
            coupon.setMaximumDiscountAmount(request.maximumDiscountAmount());
        }

        if (request.usageLimit() != null) {
            coupon.setUsageLimit(request.usageLimit());
        }
        Coupon updatedCoupon = couponRepository.save(coupon);

        log.info("Coupon updated successfully. couponId={}, code={}",
                updatedCoupon.getId(), updatedCoupon.getCode());

        return objectMapper.convertValue(coupon, CouponResponse.class);
    }

    public CouponResponse updateCouponStatus(UUID codeId, UpdateCouponStatusRequest request) {
        Coupon coupon = couponRepository.findById(codeId).orElseThrow(() -> {
            log.warn("Update coupon status failed: coupon not found. codeId={}", codeId);
            return new ResourceNotFoundException("Coupon not found");
        });

        boolean isActive = request.status() == VisibleStatus.ACTIVE;

        coupon.setActive(isActive);
        Coupon updatedCoupon = couponRepository.save(coupon);

        log.info("Coupon status updated successfully. couponId={}, code={}, active={}",
                updatedCoupon.getId(), updatedCoupon.getCode(), updatedCoupon.isActive());

        return objectMapper.convertValue(coupon, CouponResponse.class);
    }

    public void deleteCoupon(UUID codeId) {
        Coupon coupon = couponRepository.findById(codeId).orElseThrow(() -> {
            log.warn("Delete coupon failed: coupon not found. codeId={}", codeId);
            return new ResourceNotFoundException("Coupon not found");
        });

        couponRepository.delete(coupon);
        log.info("Coupon deleted successfully. couponId={}, code={}", coupon.getId(), coupon.getCode());
    }


    public CouponResponse getCoupon(UUID codeId) {
        Coupon coupon = couponRepository.findById(codeId).orElseThrow(() -> {
            log.warn("Get coupon failed: coupon not found. codeId={}", codeId);
            return new ResourceNotFoundException("Coupon not found");
        });

        return objectMapper.convertValue(coupon, CouponResponse.class);
    }


    private String normalizeCode(String code) {
        return code.trim().toUpperCase();
    }

    private void validateCouponData(
            DiscountType discountType,
            BigDecimal discountValue,
            Instant validFrom,
            Instant validUntil
    ) {
        if (discountType == DiscountType.PERCENTAGE && discountValue.compareTo(BigDecimal.valueOf(100)) > 0) {
            log.warn("Coupon validation failed: percentage discount exceeds 100. discountType={}, discountValue={}",
                    discountType, discountValue);
            throw new BadRequestException("Percentage discount cannot exceed 100%");
        }
        if (!validUntil.isAfter(validFrom)) {
            log.warn("Coupon validation failed: validUntil must be after validFrom. validFrom={}, validUntil={}",
                    validFrom, validUntil);
            throw new BadRequestException("Coupon valid until date must be after valid from date");
        }
    }
}
