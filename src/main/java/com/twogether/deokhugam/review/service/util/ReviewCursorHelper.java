package com.twogether.deokhugam.review.service.util;

import com.twogether.deokhugam.review.entity.Review;
import com.twogether.deokhugam.review.entity.ReviewLike;
import com.twogether.deokhugam.review.repository.ReviewLikeRepository;
import jakarta.annotation.Generated;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReviewCursorHelper {

    private final ReviewLikeRepository reviewLikeRepository;

    // likeByMe 일괄 조회 메서드
    @Generated("helper-method")
    public Map<UUID, Boolean> getLikeByMeMap(List<Review> reviews, UUID requestUserId){
        if (reviews.isEmpty() || requestUserId == null){
            return Map.of();
        }

        // 조회된 리뷰의 Id 목록
        List<UUID> reviewIds = reviews.stream()
                .map(Review::getId)
                .toList();

        // 리뷰 Id 목록과 요청자 Id를 이용해서 ReviewLike 목록 구하기
        List<ReviewLike> reviewLikes = reviewLikeRepository.findByUserIdAndReviewIdIn(requestUserId, reviewIds);

        // 조회 편하게 Map으로 만들기 (비정상적인 상황 (중복 키 발생)에 대비해 중복 키 처리 함수 마지막에 추가)
        return reviewLikes.stream()
                .collect(Collectors.toMap(
                        reviewLike -> reviewLike.getReviewLikePK().getReviewId(),
                        ReviewLike::isLiked,
                        (existing, replacement) -> replacement
                ));
    }

    // 커서 생성
    @Generated("helper-method")
    public String generateNextCursor(List<Review> reviews, String orderBy){
        if (reviews.isEmpty()) return null;

        Review lastReview = reviews.get(reviews.size() - 1);

        if ("rating".equalsIgnoreCase(orderBy)){

            return String.valueOf(lastReview.getRating());
        }
        else{
            return lastReview.getCreatedAt().toString();
        }
    }

    // afterAt 생성
    @Generated("helper-method")
    public String generateAfter(List<Review> reviews){
        if (reviews.isEmpty()) return null;

        Review lastReview = reviews.get(reviews.size() - 1);
        return lastReview.getCreatedAt().toString();
    }

}
