package com.twogether.deokhugam.dashboard.batch.writer;

import com.twogether.deokhugam.common.exception.DeokhugamException;
import com.twogether.deokhugam.common.exception.ErrorCode;
import com.twogether.deokhugam.dashboard.entity.PopularReviewRanking;
import com.twogether.deokhugam.dashboard.entity.RankingPeriod;
import com.twogether.deokhugam.dashboard.repository.PopularReviewRankingRepository;
import com.twogether.deokhugam.notification.event.PopularReviewRankedEvent;
import com.twogether.deokhugam.review.entity.Review;
import com.twogether.deokhugam.review.repository.ReviewRepository;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PopularReviewRankingWriter implements ItemWriter<PopularReviewRanking> {

    private final PopularReviewRankingRepository popularReviewRankingRepository;
    private final ReviewRepository reviewRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public void write(Chunk<? extends PopularReviewRanking> items) {
        List<PopularReviewRanking> rankingList = new ArrayList<>(items.getItems());

        if (rankingList.isEmpty()) {
            log.warn("인기 리뷰 랭킹 저장 스킵: 저장할 데이터가 없습니다.");
            throw new DeokhugamException(ErrorCode.RANKING_DATA_EMPTY);
        }

        try {
            rankingList.sort(
                Comparator.comparingDouble(PopularReviewRanking::getScore).reversed()
                    .thenComparing(Comparator.comparing(PopularReviewRanking::getCreatedAt).reversed())
            );

            RankingPeriod period = rankingList.get(0).getPeriod();
            popularReviewRankingRepository.deleteByPeriod(period);

            int rank = 1;
            double prevScore = Double.NEGATIVE_INFINITY;

            for (int i = 0; i < rankingList.size(); i++) {
                PopularReviewRanking current = rankingList.get(i);
                double score = current.getScore();

                if (Double.compare(score, prevScore) != 0) {
                    rank = i + 1;
                }

                current.assignRank(rank);
                prevScore = score;
            }

            popularReviewRankingRepository.saveAll(rankingList);
            log.info("인기 리뷰 랭킹 {}건 저장 완료", rankingList.size());

        } catch (Exception e) {
            log.error("인기 리뷰 랭킹 저장 실패", e);
            throw new DeokhugamException(ErrorCode.RANKING_SAVE_FAILED);
        }

        try {
            List<UUID> top10ReviewIds = rankingList.stream()
                .filter(r -> r.getRank() <= 10)
                .map(PopularReviewRanking::getReviewId)
                .toList();

            Map<UUID, Review> reviewMap = reviewRepository.findAllById(top10ReviewIds)
                .stream()
                .collect(Collectors.toMap(Review::getId, Function.identity()));

            for (PopularReviewRanking ranking : rankingList) {
                if (ranking.getRank() <= 10) {
                    Review review = reviewMap.get(ranking.getReviewId());
                    if (review != null) {
                        eventPublisher.publishEvent(new PopularReviewRankedEvent(review.getUser(), review));
                    } else {
                        log.warn("알림 이벤트 발행 스킵: 리뷰가 존재하지 않습니다. reviewId: {}", ranking.getReviewId());
                    }
                }
            }

        } catch (Exception e) {
            log.error("랭킹 알림 이벤트 발행 실패 - 랭킹 저장은 성공", e);
        }
    }
}
