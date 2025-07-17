package com.twogether.deokhugam.review.repository;

import com.twogether.deokhugam.review.entity.Review;
import java.util.UUID;

import com.twogether.deokhugam.review.repository.custom.ReviewRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepository extends JpaRepository<Review, UUID>, ReviewRepositoryCustom {

    boolean existsByUserIdAndBookId(UUID userId, UUID bookId);

}
