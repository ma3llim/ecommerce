package org.ecommerce.common.utils;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public final class DateTimeUtil {
    private DateTimeUtil() {
    }

    private static final ZoneId DEFAULT_ZONE = ZoneId.of("Asia/Kolkata");

    private static final DateTimeFormatter DISPLAY_FORMATTER =
            DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a")
                    .withZone(DEFAULT_ZONE);

    public static String format(Instant instant) {
        if (instant == null) {
            return null;
        }

        return DISPLAY_FORMATTER.format(instant);
    }
}
