package com.twogether.deokhugam.review.repository.custom;

import com.twogether.deokhugam.dashboard.dto.BookScoreDto;
import com.twogether.deokhugam.review.dto.request.ReviewSearchRequest;
import com.twogether.deokhugam.review.entity.Review;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface ReviewRepositoryCustom {
    List<BookScoreDto> calculateBookScores(LocalDateTime start, LocalDateTime end);
    List<BookScoreDto> calculateBookScoresAllTime();

    long totalElementCount(ReviewSearchRequest reviewSearchRequest);
    Slice<Review> findReviewsWithCursor(ReviewSearchRequest request, Pageable pageable);

}
