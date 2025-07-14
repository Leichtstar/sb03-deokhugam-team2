package com.twogether.deokhugam.book.dto;

import java.time.LocalDate;

public record NaverBookDto(
    String title,
    String author,
    String description,
    String publisher,
    LocalDate publishedDate,
    String isbn,
    String thumbnailImage //Base64로 인코딩
) {}
