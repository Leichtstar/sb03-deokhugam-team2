package com.twogether.deokhugam.book.dto.request;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;

public record BookCreateRequest(
    @NotBlank(message = "제목은 필수 입력 사항입니다.") @Size(max = 255)
    String title,

    @NotBlank(message = "저자는 필수 입력 사항입니다.") @Size(max = 100)
    String author,

    @Size(max = 2000)
    String description,

    @NotBlank(message = "출판사는 필수 입력 사항입니다.") @Size(max = 100)
    String publisher,

    @NotNull
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    LocalDate publishedDate,

    @Pattern(regexp = "^$|(\\d{9}[\\dXx])|(\\d{13})$", message = "유효한 ISBN 형식이 아닙니다.")
    @Nullable
    String isbn // optional
) {}