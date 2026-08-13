package org.ecommerce.common.enums;

public enum CloudinaryFolder {
    PROFILE_IMAGES("ecommerce/profile-images"),
    PRODUCT_IMAGES("ecommerce/product-images"),
    CATEGORY_IMAGES("ecommerce/category-images"),
    REVIEW_IMAGES("ecommerce/review-images");

    private final String path;

    CloudinaryFolder(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }
}
