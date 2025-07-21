package com.twogether.deokhugam.review.repository;

import com.twogether.deokhugam.review.entity.Review;
import com.twogether.deokhugam.review.repository.custom.ReviewRepositoryCustom;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepository extends JpaRepository<Review, UUID>, ReviewRepositoryCustom {

    boolean existsByUserIdAndBookId(UUID userId, UUID bookId);
    boolean existsByUserIdAndBookIdAndIsDeletedFalse(UUID userId, UUID bookId);

    @Query("""
    SELECT COUNT(r), COALESCE(AVG(r.rating), 0)
    FROM Review r
    WHERE r.book.id = :bookId AND r.isDeleted = false
""")
    Object getReviewStats(@Param("bookId") UUID bookId);

}
