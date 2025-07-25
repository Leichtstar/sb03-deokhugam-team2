package com.twogether.deokhugam.dashboard.batch.writer;

import com.twogether.deokhugam.common.exception.DeokhugamException;
import com.twogether.deokhugam.common.exception.ErrorCode;
import com.twogether.deokhugam.dashboard.entity.PopularBookRanking;
import com.twogether.deokhugam.dashboard.entity.RankingPeriod;
import com.twogether.deokhugam.dashboard.repository.PopularBookRankingRepository;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PopularBookRankingWriter implements ItemWriter<PopularBookRanking> {

    private final PopularBookRankingRepository popularBookRankingRepository;

    @Override
    public void write(Chunk<? extends PopularBookRanking> items) {
        List<? extends PopularBookRanking> rankingList = items.getItems();

        if (rankingList == null || rankingList.isEmpty()) {
            log.warn("인기 도서 랭킹 저장 스킵: 저장할 데이터가 없습니다.");
            throw new DeokhugamException(ErrorCode.RANKING_DATA_EMPTY);
        }

        try {
            // 점수 기준 내림차순 정렬
            rankingList.sort(Comparator.comparingDouble(PopularBookRanking::getScore).reversed());

            // 기간별 기존 랭킹 제거
            RankingPeriod period = rankingList.get(0).getPeriod();
            popularBookRankingRepository.deleteByPeriod(period);

            // 동점 처리 포함 랭크 부여
            int rank = 1;
            double prevScore = Double.NEGATIVE_INFINITY;

            for (int i = 0; i < rankingList.size(); i++) {
                PopularBookRanking current = rankingList.get(i);
                double score = current.getScore();

                if (Double.compare(score, prevScore) != 0) {
                    rank = i + 1;
                }

                current.assignRank(rank);
                prevScore = score;
            }

            popularBookRankingRepository.saveAll(rankingList);
            log.info("인기 도서 랭킹 {}건 저장 완료", rankingList.size());

        } catch (Exception e) {
            log.error("인기 도서 랭킹 저장 실패", e);
            throw new DeokhugamException(ErrorCode.RANKING_SAVE_FAILED);
        }
    }
}