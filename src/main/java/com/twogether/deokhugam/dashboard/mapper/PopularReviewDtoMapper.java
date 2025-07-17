package com.twogether.deokhugam.dashboard.mapper;

import com.twogether.deokhugam.dashboard.dto.response.PopularReviewDto;
import com.twogether.deokhugam.dashboard.entity.PopularReviewRanking;
import java.util.List;

public class PopularReviewDtoMapper {

    public static PopularReviewDto toDto(PopularReviewRanking r) {
        return new PopularReviewDto(
            r.getId(),
            r.getReviewId(),
            r.getBookId(),
            r.getBookTitle(),
            r.getBookThumbnailUrl(),
            r.getUserId(),
            r.getUserNickname(),
            r.getContent(),
            r.getRating(),
            r.getPeriod(),
            r.getCreatedAt(),
            r.getRank(),
            r.getScore(),
            r.getLikeCount(),
            r.getCommentCount()
        );
    }

    public static List<PopularReviewDto> toDtoList(List<PopularReviewRanking> list) {
        return list.stream()
            .map(PopularReviewDtoMapper::toDto)
            .toList();
    }
}