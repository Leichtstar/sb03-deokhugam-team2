package com.twogether.deokhugam.book.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;

public record BookUpdateRequest(
    @NotBlank(message = "제목은 필수 입력 사항입니다.") @Size(max = 255)
    String title,

    @NotBlank(message = "저자는 필수 입력 사항입니다.") @Size(max = 100)
    String author,

    @NotBlank(message = "설명은 필수 입력 사항입니다.")
    String description,

    @NotBlank(message = "출판사는 필수 입력 사항입니다.") @Size(max = 100)
    String publisher,

    @NotNull
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    LocalDate publishedDate
) {}
