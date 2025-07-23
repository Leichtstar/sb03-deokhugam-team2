package com.twogether.deokhugam.dashboard.batch.writer;

import com.twogether.deokhugam.common.exception.DeokhugamException;
import com.twogether.deokhugam.common.exception.ErrorCode;
import com.twogether.deokhugam.dashboard.entity.PopularBookRanking;
import com.twogether.deokhugam.dashboard.entity.RankingPeriod;
import com.twogether.deokhugam.dashboard.repository.PopularBookRankingRepository;
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
            RankingPeriod period = rankingList.get(0).getPeriod();
            popularBookRankingRepository.deleteByPeriod(period);

            for (int i = 0; i < rankingList.size(); i++) {
                rankingList.get(i).assignRank(i + 1);
            }

            popularBookRankingRepository.saveAll(rankingList);
            log.info("인기 도서 랭킹 {}건 저장 완료", rankingList.size());
        } catch (Exception e) {
            log.error("인기 도서 랭킹 저장 실패", e);
            throw new DeokhugamException(ErrorCode.RANKING_SAVE_FAILED);
        }
    }
}