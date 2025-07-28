package com.twogether.deokhugam.dashboard.batch.writer;

import com.twogether.deokhugam.common.exception.DeokhugamException;
import com.twogether.deokhugam.common.exception.ErrorCode;
import com.twogether.deokhugam.dashboard.entity.PopularBookRanking;
import com.twogether.deokhugam.dashboard.entity.PowerUserRanking;
import com.twogether.deokhugam.dashboard.entity.RankingPeriod;
import com.twogether.deokhugam.dashboard.repository.PowerUserRankingRepository;
import java.time.Instant;
import java.util.ArrayList;
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
public class PowerUserRankingWriter implements ItemWriter<PowerUserRanking> {

    private final PowerUserRankingRepository powerUserRankingRepository;

    @Override
    public void write(Chunk<? extends PowerUserRanking> items) {
        List<PowerUserRanking> rankingList = new ArrayList<>(items.getItems());

        if (rankingList.isEmpty()) {
            log.warn("파워 유저 랭킹 저장 스킵: 저장할 데이터가 없습니다.");
            return;
        }

        try {
            rankingList.sort(
                Comparator.comparingDouble(PowerUserRanking::getScore).reversed()
                    .thenComparing(PowerUserRanking::getCreatedAt)
            );

            RankingPeriod period = rankingList.get(0).getPeriod();
            powerUserRankingRepository.deleteByPeriod(period);

            int indexRank = 1;
            int displayedRank = 1;
            double prevScore = Double.NEGATIVE_INFINITY;

            for (PowerUserRanking current : rankingList) {
                double score = current.getScore();

                if (Double.compare(score, prevScore) != 0) {
                    displayedRank = indexRank;
                }

                current.assignRank(displayedRank);
                prevScore = score;
                indexRank++;
            }

            powerUserRankingRepository.saveAll(rankingList);
            log.info("파워 유저 랭킹 {}건 저장 완료", rankingList.size());

        } catch (Exception e) {
            log.error("파워 유저 랭킹 저장 실패", e);
            throw new DeokhugamException(ErrorCode.RANKING_SAVE_FAILED, e);
        }
    }
}