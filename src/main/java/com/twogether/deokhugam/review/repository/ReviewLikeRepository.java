package com.twogether.deokhugam.review.repository;

import com.twogether.deokhugam.review.entity.ReviewLike;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewLikeRepository extends JpaRepository<ReviewLike, UUID> {

    Optional<ReviewLike> findByUserIdAndReviewId(UUID userId, UUID reviewId);

    List<ReviewLike> findByUserIdAndReviewIdIn(UUID userId, List<UUID> reviewIds);
}
