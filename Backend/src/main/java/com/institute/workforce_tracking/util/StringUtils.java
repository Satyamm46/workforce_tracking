package com.institute.workforce_tracking.util;

/**
 * String manipulation helpers reused across services.
 */
public final class StringUtils {

    private StringUtils() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Treats blank or empty batch input as "no batch" (stored as null).
     * Consistent normalization for lecture and series batch fields.
     *
     * @param batch raw batch string from user input
     * @return trimmed batch or null
     */
    public static String normalizeBatch(String batch) {
        if (batch == null || batch.isBlank()) {
            return null;
        }
        return batch.trim();
    }
}
