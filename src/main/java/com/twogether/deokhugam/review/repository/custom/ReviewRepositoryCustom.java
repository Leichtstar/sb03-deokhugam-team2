package com.twogether.deokhugam.review.repository.custom;

import com.twogether.deokhugam.dashboard.batch.model.BookScoreDto;
import java.time.LocalDateTime;
import java.util.List;

public interface ReviewRepositoryCustom {
    List<BookScoreDto> calculateBookScores(LocalDateTime start, LocalDateTime end);
    List<BookScoreDto> calculateBookScoresAllTime();
}
