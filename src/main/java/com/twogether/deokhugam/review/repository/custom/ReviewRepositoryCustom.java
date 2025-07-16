package com.twogether.deokhugam.review.repository.custom;

import com.twogether.deokhugam.dashboard.dto.BookScoreDto;
import com.twogether.deokhugam.review.dto.request.ReviewSearchRequest;
import com.twogether.deokhugam.review.entity.Review;
import java.time.LocalDateTime;
import java.util.List;

public interface ReviewRepositoryCustom {
    List<BookScoreDto> calculateBookScores(LocalDateTime start, LocalDateTime end);
    List<BookScoreDto> calculateBookScoresAllTime();

    List<Review> findByFilter(ReviewSearchRequest reviewSearchRequest);
}
