package com.twogether.deokhugam.dashboard.dto;

import java.util.UUID;

public record BookScoreDto(
    UUID bookId,
    String title,
    String author,
    String thumbnailUrl,
    long reviewCount,
    double averageRating
) {
    public double calculateScore() {
        return reviewCount * 0.4 + averageRating * 0.6;
    }
}
