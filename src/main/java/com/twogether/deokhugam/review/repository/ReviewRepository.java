package com.twogether.deokhugam.review.repository;

import com.twogether.deokhugam.review.entity.Review;
import com.twogether.deokhugam.review.repository.custom.ReviewRepositoryCustom;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepository extends JpaRepository<Review, UUID>, ReviewRepositoryCustom {

    boolean existsByUserIdAndBookId(UUID userId, UUID bookId);

}
