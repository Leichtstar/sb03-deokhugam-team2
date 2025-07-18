package com.twogether.deokhugam.review.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ReviewUpdateRequest(
        @NotBlank(message = "리뷰 내용은 필수 입력 항목입니다.")
        @Size(max = 5000, message = "내용은 5000자를 초과할 수 없습니다.")
        String content,

        @Min(value = 1, message = "리뷰 평점은 최소 1점 이상이어야 합니다.")
        @Max(value = 5, message = "리뷰 평점은 최대 5점 이하이어야 합니다.")
        int rating
) { }
