package com.twogether.deokhugam.dashboard.batch.writer;

import com.twogether.deokhugam.common.exception.DeokhugamException;
import com.twogether.deokhugam.common.exception.ErrorCode;
import com.twogether.deokhugam.dashboard.entity.PopularReviewRanking;
import com.twogether.deokhugam.dashboard.repository.PopularReviewRankingRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PopularReviewRankingWriter implements ItemWriter<PopularReviewRanking> {

    private final PopularReviewRankingRepository popularReviewRankingRepository;

    @Override
    public void write(Chunk<? extends PopularReviewRanking> items) {
        List<? extends PopularReviewRanking> rankingList = items.getItems();

        if (rankingList == null || rankingList.isEmpty()) {
            log.warn("인기 리뷰 랭킹 저장 스킵: 저장할 데이터가 없습니다.");
            throw new DeokhugamException(ErrorCode.RANKING_DATA_EMPTY);
        }

        try {
            for (int i = 0; i < rankingList.size(); i++) {
                rankingList.get(i).assignRank(i + 1); // 1위부터
            }

            popularReviewRankingRepository.saveAll(rankingList);
            log.info("인기 리뷰 랭킹 {}건 저장 완료", rankingList.size());
        } catch (Exception e) {
            log.error("인기 리뷰 랭킹 저장 실패", e);
            throw new DeokhugamException(ErrorCode.RANKING_SAVE_FAILED);
        }
    }
}