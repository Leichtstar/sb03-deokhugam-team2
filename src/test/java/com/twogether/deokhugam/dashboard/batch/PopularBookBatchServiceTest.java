package com.twogether.deokhugam.dashboard.batch;

import com.twogether.deokhugam.dashboard.repository.PopularBookRankingRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class PopularBookBatchServiceTest {

    @Autowired
    private PopularBookBatchService batchService;

    @Autowired
    private PopularBookRankingRepository rankingRepository;

    @Test
    @DisplayName("일간 인기 도서 배치 테스트 (null)")
    void testRunDailyBatch() {
        batchService.runDailyBatchNow();

        long count = rankingRepository.count();
        System.out.println("저장된 랭킹 수: " + count);

        // 추후 추가
        // Assertions.asserEquals(1, count);
    }
}
