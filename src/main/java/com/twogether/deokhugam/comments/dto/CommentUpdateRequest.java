package com.twogether.deokhugam.comments.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CommentUpdateRequest(
    @NotBlank(message = "내용은 비어 있을 수 없습니다.")
    @Size(max = 200, message = "내용은 200자를 초과할 수 없습니다.")
    String content
) {}
