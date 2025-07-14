package com.twogether.deokhugam.review.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record ReviewCreateRequest(

        @NotNull(message = "리뷰엔 책 정보가 필요합니다.")
        UUID bookId,

        @NotNull(message = "리뷰엔 사용자 정보가 필요합니다.")
        UUID userId,

        @NotBlank(message = "리뷰 내용은 필수 입력 항목입니다.")
        String content,

        @Min(value = 1, message = "리뷰 평점은 최소 1점 이상이어야 합니다.")
        @Max(value = 5, message = "리뷰 평점은 최대 5점 이하이어야 합니다.")
        int rating
) { }
