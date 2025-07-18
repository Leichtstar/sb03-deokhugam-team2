package com.twogether.deokhugam.common.dto;

import java.util.List;

public record CursorPageResponseDto<T> (
        List<T> content,
        String nextCursor,
        String nextAfter,
        int size,
        long totalElement,
        boolean hasNext
) { }
