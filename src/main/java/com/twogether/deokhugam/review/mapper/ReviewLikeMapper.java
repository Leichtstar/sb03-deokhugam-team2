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

        if (reviewLike.getReview() == null || reviewLike.getUser() == null){
            throw new IllegalStateException("ReviewLike가 null 값인 리뷰나 사용자를 참조하고 있습니다.");
        }

        return new ReviewLikeDto(
                reviewLike.getReview().getId(),
                reviewLike.getUser().getId(),
                reviewLike.isLiked()
        );
    }
}
