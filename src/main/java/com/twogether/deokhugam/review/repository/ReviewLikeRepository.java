package com.twogether.deokhugam.review.repository;

import com.twogether.deokhugam.review.entity.ReviewLike;
import java.util.Optional;
import java.util.UUID;
import org.aspectj.apache.bcel.classfile.Module.Open;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewLikeRepository extends JpaRepository<ReviewLike, UUID> {

    boolean existsByUserIdAndReviewId(UUID userId, UUID reviewId);
    Optional<ReviewLike> findByUserIdAndReviewId(UUID userId, UUID reviewId);
}
