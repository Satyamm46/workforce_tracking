package com.institute.workforce_tracking.util;

import com.institute.workforce_tracking.constants.AppConstants;

/**
 * Shared helpers for safe pagination parameters.
 * Non-instantiable holder of static methods.
 */
public final class PageUtils {

    private PageUtils() {
        // Prevent instantiation.
    }

    /** Floors a requested page number at zero. */
    public static int safePage(int page) {
        return Math.max(page, 0);
    }

    /** Clamps a requested size to (0, MAX_PAGE_SIZE], defaulting when invalid. */
    public static int safeSize(int size) {
        if (size <= 0) {
            return AppConstants.DEFAULT_PAGE_SIZE;
        }
        return Math.min(size, AppConstants.MAX_PAGE_SIZE);
    }
}