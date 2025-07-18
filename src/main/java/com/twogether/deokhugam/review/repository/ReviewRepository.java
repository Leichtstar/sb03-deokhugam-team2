package com.twogether.deokhugam.review.repository;

import com.twogether.deokhugam.review.entity.Review;
import com.twogether.deokhugam.review.repository.custom.ReviewRepositoryCustom;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepository extends JpaRepository<Review, UUID>, ReviewRepositoryCustom {

    boolean existsByUserIdAndBookIdAndIsDeletedFalse(UUID userId, UUID bookId);

    @Modifying
    @Query("UPDATE Review r SET r.commentCount = r.commentCount + 1, r.updatedAt = CURRENT_TIMESTAMP WHERE r.id = :reviewId")
    void incrementCommentCount(@Param("reviewId") UUID reviewId);

    @Modifying
    @Query("UPDATE Review r SET r.commentCount = r.commentCount - 1, r.updatedAt = CURRENT_TIMESTAMP WHERE r.id = :reviewId AND r.commentCount > 0 ")
    void decrementCommentCount(@Param("reviewId") UUID reviewId);

    @Query("SELECT COUNT(c) FROM Comment c WHERE c.review.id = :reviewId AND c.isDeleted = false")
    long countCommentsByReviewId(@Param("reviewId") UUID reviewId);
}
