package com.twogether.deokhugam.review.mapper;

import com.twogether.deokhugam.review.dto.ReviewLikeDto;
import com.twogether.deokhugam.review.entity.ReviewLike;
import org.springframework.stereotype.Component;

@Component
public class ReviewLikeMapper {

    public ReviewLikeDto toDto(ReviewLike reviewLike){
        if (reviewLike == null){
            return null;
        }

        return new ReviewLikeDto(
                reviewLike.getReview().getId(),
                reviewLike.getUser().getId(),
                reviewLike.isLiked()
        );
    }
}
