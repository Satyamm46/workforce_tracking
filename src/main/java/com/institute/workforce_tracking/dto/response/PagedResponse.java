package com.institute.workforce_tracking.dto.response;

import java.util.List;

import org.springframework.data.domain.Page;

/**
 * A framework-neutral wrapper for a page of results.
 *
 * <p>Spring Data returns a {@link Page}, but exposing that type directly in the
 * API would leak Spring's internal JSON shape into our contract. This DTO
 * re-expresses paging in our own stable structure, built from a {@code Page}
 * via {@link #from(Page)}.</p>
 *
 * @param <T>           the type of the items in the page
 * @param content       the items on the current page
 * @param page          the current page number (zero-based)
 * @param size          the page size
 * @param totalElements total number of items across all pages
 * @param totalPages    total number of pages
 * @param first         whether this is the first page
 * @param last          whether this is the last page
 */
public record PagedResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last
) {

    /**
     * Builds a PagedResponse from a Spring Data {@link Page}. The page should
     * already contain the outbound DTO type (map entities to DTOs first).
     *
     * @param page the source page of DTOs
     * @param <T>  the item type
     * @return a framework-neutral paged response
     */
    public static <T> PagedResponse<T> from(Page<T> page) {
        return new PagedResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast()
        );
    }
}