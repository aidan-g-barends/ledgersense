package com.ledgersense.dto;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

// NEW: our own stable page shape for every paginated endpoint.
// <T> = generic: works for transactions now, and anything else later.
public record PageResponse<T>(List<T> content, int page, int size,
                              long totalElements, int totalPages) {

    // Turns a Spring Page of entities into a PageResponse of DTOs
    public static <E, T> PageResponse<T> from(Page<E> page, Function<E, T> mapper) {
        return new PageResponse<>(
                page.getContent().stream().map(mapper).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages());
    }
}