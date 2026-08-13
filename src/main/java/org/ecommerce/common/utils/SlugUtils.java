package org.ecommerce.common.utils;

public final class SlugUtils {
    private SlugUtils() {
    }

    public static String generateSlug(String text) {
        if (text == null || text.isBlank()) {
            return "";
        }
        return text.trim().toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-");

    }
}
