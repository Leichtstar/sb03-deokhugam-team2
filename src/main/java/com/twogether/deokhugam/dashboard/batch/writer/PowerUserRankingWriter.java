package com.twogether.deokhugam.dashboard.batch.writer;

import com.twogether.deokhugam.common.exception.DeokhugamException;
import com.twogether.deokhugam.common.exception.ErrorCode;
import com.twogether.deokhugam.dashboard.entity.PowerUserRanking;
import com.twogether.deokhugam.dashboard.repository.PowerUserRankingRepository;
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
        List<? extends PowerUserRanking> rankingList = items.getItems();

        if (rankingList == null || rankingList.isEmpty()) {
            log.warn("파워 유저 랭킹 저장 스킵: 저장할 데이터가 없습니다.");
            return;
        }

        try {
            for (int i = 0; i < rankingList.size(); i++) {
                PowerUserRanking current = rankingList.get(i);

                if (i > 0) {
                    PowerUserRanking previous = rankingList.get(i - 1);
                    if (Double.compare(current.getScore(), previous.getScore()) == 0) {
                        current.assignRank(previous.getRank());
                    } else {
                        current.assignRank(i + 1);
                    }
                } else {
                    current.assignRank(1);
                }
            }

            powerUserRankingRepository.saveAll(rankingList);
            log.info("파워 유저 랭킹 {}건 저장 완료", rankingList.size());
        } catch (Exception e) {
            log.error("파워 유저 랭킹 저장 실패", e);
            throw new DeokhugamException(ErrorCode.RANKING_SAVE_FAILED, e);
        }
    }
}