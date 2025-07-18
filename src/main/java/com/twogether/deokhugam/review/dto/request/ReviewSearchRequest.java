package com.twogether.deokhugam.review.dto.request;

import jakarta.validation.constraints.NotNull;
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
        @NotNull(message = "조회 요청자의 id는 필수입니다.")
        UUID requestUserId
) { }
