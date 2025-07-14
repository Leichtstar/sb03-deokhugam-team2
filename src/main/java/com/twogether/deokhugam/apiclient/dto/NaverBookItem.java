package com.twogether.deokhugam.apiclient.dto;

public record NaverBookItem(
    String title,
    String author,
    String description,
    String publisher,
    String pubdate,  // yyyyMMdd
    String isbn,
    String image
) {}
