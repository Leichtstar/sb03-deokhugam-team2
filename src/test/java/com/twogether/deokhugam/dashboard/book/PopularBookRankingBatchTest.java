package com.twogether.deokhugam.dashboard.book;

import static org.assertj.core.api.Assertions.assertThat;

import com.twogether.deokhugam.dashboard.entity.RankingPeriod;
import com.twogether.deokhugam.dashboard.repository.PopularBookRankingRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

@SpringBootTest
@SpringBatchTest
@ActiveProfiles("test")
class PopularBookRankingBatchTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private PopularBookRankingRepository rankingRepository;

    @ParameterizedTest(name = "period={0}인 배치 Job이 성공적으로 실행되고 데이터가 저장된다")
    @EnumSource(RankingPeriod.class)
    @Sql(scripts = "/sql/popular_book_ranking_test_data.sql")
    @DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
    @DisplayName("기간별 배치 Job 성공 테스트")
    void batchJob_withPeriod_shouldSucceedAndSaveData(RankingPeriod period) throws Exception {
        // given
        JobParameters params = new JobParametersBuilder()
            .addString("period", period.name())
            .addLong("timestamp", System.currentTimeMillis())
            .toJobParameters();

        // when
        JobExecution execution = jobLauncherTestUtils.launchJob(params);

        // then
        assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(rankingRepository.findAll()).isNotEmpty();
    }
}