package com.twogether.deokhugam.comments.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CommentCreateRequest(
    @NotNull(message = "userId는 필수입니다.") UUID userId,
    @NotNull(message = "reviewId는 필수입니다.") UUID reviewId,
    @NotBlank(message = "내용은 비어 있을 수 없습니다.")
    @Size(max = 200, message = "댓글은 {max}자를 초과할 수 없습니다.")
    @Pattern(regexp = "^[\\s\\S]*\\S[\\s\\S]*$", message = "공백으로만 이루어진 댓글은 작성할 수 없습니다.")
    String content
) {}
