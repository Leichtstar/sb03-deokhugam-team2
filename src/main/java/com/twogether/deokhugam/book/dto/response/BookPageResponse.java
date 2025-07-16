package com.twogether.deokhugam.book.dto.response;

import java.time.Instant;
import java.util.List;

public record BookPageResponse<T>(
    List<T> content,
    String nextCursor,
    Instant nextAfter,
    int size,
    long totalElements,
    boolean hasNext
) {}
