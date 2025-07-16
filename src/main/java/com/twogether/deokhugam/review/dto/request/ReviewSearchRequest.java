package com.twogether.deokhugam.review.dto.request;

import java.util.UUID;

public record ReviewSearchRequest (
        UUID userId,
        UUID bookId,
        String keyword,
        String orderBy,
        String direction,
        String cursor,
        String after,
        int limit,
        UUID requestUserId
) { }
