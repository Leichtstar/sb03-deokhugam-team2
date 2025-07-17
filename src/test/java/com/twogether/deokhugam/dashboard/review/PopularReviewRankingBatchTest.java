package com.twogether.deokhugam.dashboard.review;

import static org.assertj.core.api.Assertions.assertThat;

import com.twogether.deokhugam.dashboard.entity.PopularReviewRanking;
import com.twogether.deokhugam.dashboard.repository.PopularReviewRankingRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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
@DisplayName("PopularReviewRanking 배치 통합 테스트")
@TestPropertySource(properties = "spring.profiles.active=test")
@Sql("/sql/popular_review_ranking_test_data.sql")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class PopularReviewRankingBatchTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private Job popularReviewRankingJob;

    @Autowired
    private PopularReviewRankingRepository rankingRepository;

    @BeforeEach
    void setUp() {
        jobLauncherTestUtils.setJob(popularReviewRankingJob);
    }

    @Test
    @DisplayName("리뷰 랭킹 배치가 성공적으로 실행되어 DB에 랭킹이 저장된다")
    void runReviewRankingBatch() throws Exception {
        // given
        JobParameters jobParameters = new JobParametersBuilder()
            .addString("period", "DAILY")
            .addLong("time", System.currentTimeMillis())
            .toJobParameters();

        // when
        JobExecution execution = jobLauncherTestUtils.launchJob(jobParameters);

        // then
        assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);

        List<PopularReviewRanking> rankings = rankingRepository.findAll();
        assertThat(rankings).hasSize(1);
        assertThat(rankings.get(0).getRank()).isEqualTo(1);
    }
}