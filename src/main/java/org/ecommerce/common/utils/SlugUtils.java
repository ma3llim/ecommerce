package org.ecommerce.common.utils;

import java.text.Normalizer;

public final class SlugUtils {
    private SlugUtils() {
    }

    public static String generateSlug(String text) {
        if (text == null || text.isBlank()) {
            return "";
        }
        return Normalizer.normalize(text, Normalizer.Form.NFC)
                .replaceAll("\\p{M}", "")
                .toLowerCase().toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-");

    }
}
