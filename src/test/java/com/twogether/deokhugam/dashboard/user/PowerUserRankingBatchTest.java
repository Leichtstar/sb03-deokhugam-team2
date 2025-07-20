package com.twogether.deokhugam.dashboard.user;

import static org.assertj.core.api.Assertions.assertThat;

import com.twogether.deokhugam.dashboard.entity.PowerUserRanking;
import com.twogether.deokhugam.dashboard.entity.RankingPeriod;
import com.twogether.deokhugam.dashboard.repository.PowerUserRankingRepository;
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
@Sql("/sql/power_user_ranking_test_data.sql")
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
            .addLong("time", System.currentTimeMillis())
            .toJobParameters();

        // when
        JobExecution execution = jobLauncherTestUtils.launchJob(jobParameters);

        // then
        assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);

        List<PowerUserRanking> rankings = rankingRepository.findAll();
        assertThat(rankings).isNotEmpty();
        assertThat(rankings).allMatch(ranking -> ranking.getPeriod() == period);
        assertThat(rankings.get(0).getRank()).isEqualTo(1);
        for (int i = 0; i < rankings.size(); i++) {
            assertThat(rankings.get(i).getRank()).isEqualTo(i + 1);
        }
        assertThat(rankings).hasSize(2);
    }
}