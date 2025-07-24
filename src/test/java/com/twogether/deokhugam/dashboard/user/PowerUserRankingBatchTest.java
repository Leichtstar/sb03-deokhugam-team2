package com.twogether.deokhugam.dashboard.user;

import static org.assertj.core.api.Assertions.assertThat;

import com.twogether.deokhugam.dashboard.entity.PowerUserRanking;
import com.twogether.deokhugam.dashboard.entity.RankingPeriod;
import com.twogether.deokhugam.dashboard.repository.PowerUserRankingRepository;
import java.util.Comparator;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;

@SpringBootTest
@SpringBatchTest
@DisplayName("PowerUserRanking 배치 통합 테스트")
@TestPropertySource(properties = "spring.profiles.active=test")
@Sql("/sql/popular_ranking_test_data.sql")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class PowerUserRankingBatchTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private Job powerUserRankingJob;

    @Autowired
    private PowerUserRankingRepository rankingRepository;

    @BeforeEach
    void setUp() {
        jobLauncherTestUtils.setJob(powerUserRankingJob);
    }

    @ParameterizedTest(name = "period={0}인 파워 유저 배치 Job 실행")
    @EnumSource(RankingPeriod.class)
    @DisplayName("기간별 파워 유저 배치 성공 테스트")
    void runPowerUserRankingBatch(RankingPeriod period) throws Exception {
        // given
        JobParameters jobParameters = new JobParametersBuilder()
            .addString("period", period.name())
            .addString("now", "2025-07-22T00:00:00Z")
            .addLong("time", System.currentTimeMillis())
            .toJobParameters();

        // when
        JobExecution execution = jobLauncherTestUtils.launchJob(jobParameters);

        // then
        assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);

        List<PowerUserRanking> rankings = rankingRepository.findAllByPeriod(period);
        rankings.sort(Comparator.comparing(PowerUserRanking::getRank));

        assertThat(rankings).isNotEmpty();
        assertThat(rankings).allMatch(ranking -> ranking.getPeriod() == period);
        assertThat(rankings.get(0).getRank()).isEqualTo(1);
        for (int i = 1; i < rankings.size(); i++) {
            assertThat(rankings.get(i).getRank()).isGreaterThanOrEqualTo(rankings.get(i - 1).getRank());
        }

        if (period == RankingPeriod.DAILY) {
            assertThat(rankings).hasSize(1);
        } else if (period == RankingPeriod.WEEKLY) {
            assertThat(rankings).hasSize(1);
        } else if (period == RankingPeriod.MONTHLY) {
            assertThat(rankings).hasSize(2);
        } else if (period == RankingPeriod.ALL_TIME) {
            assertThat(rankings).hasSize(3);
        }
    }
}