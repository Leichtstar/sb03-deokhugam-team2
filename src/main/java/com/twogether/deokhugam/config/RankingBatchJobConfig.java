package com.twogether.deokhugam.config;

import com.twogether.deokhugam.dashboard.batch.model.BookScoreDto;
import com.twogether.deokhugam.dashboard.batch.model.PowerUserScoreDto;
import com.twogether.deokhugam.dashboard.batch.model.ReviewScoreDto;
import com.twogether.deokhugam.dashboard.batch.processor.BookScoreProcessor;
import com.twogether.deokhugam.dashboard.batch.processor.PowerUserScoreProcessor;
import com.twogether.deokhugam.dashboard.batch.processor.ReviewScoreProcessor;
import com.twogether.deokhugam.dashboard.batch.reader.JpaBookScoreReader;
import com.twogether.deokhugam.dashboard.batch.reader.JpaPowerUserScoreReader;
import com.twogether.deokhugam.dashboard.batch.reader.JpaReviewScoreReader;
import com.twogether.deokhugam.dashboard.batch.writer.PopularBookRankingWriter;
import com.twogether.deokhugam.dashboard.batch.writer.PopularReviewRankingWriter;
import com.twogether.deokhugam.dashboard.batch.writer.PowerUserRankingWriter;
import com.twogether.deokhugam.dashboard.entity.PopularBookRanking;
import com.twogether.deokhugam.dashboard.entity.PopularReviewRanking;
import com.twogether.deokhugam.dashboard.entity.PowerUserRanking;
import com.twogether.deokhugam.dashboard.entity.RankingPeriod;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
public class RankingBatchJobConfig {

    private final EntityManager em;
    private final PopularBookRankingWriter bookWriter;
    private final PopularReviewRankingWriter reviewWriter;
    private final PowerUserRankingWriter powerUserRankingWriter;

    // [1] 도서 랭킹 Job
    @Bean
    public Job popularBookRankingJob(JobRepository jobRepository,
        PlatformTransactionManager transactionManager,
        JpaBookScoreReader reader) {
        return new JobBuilder("popularBookRankingJob", jobRepository)
            .start(popularBookRankingStep(jobRepository, transactionManager, reader))
            .build();
    }

    @Bean
    @JobScope
    public Step popularBookRankingStep(JobRepository jobRepository,
        PlatformTransactionManager transactionManager,
        JpaBookScoreReader reader) {
        ItemProcessor<BookScoreDto, PopularBookRanking> processor = new BookScoreProcessor(em);

        return new StepBuilder("popularBookStep", jobRepository)
            .<BookScoreDto, PopularBookRanking>chunk(100, transactionManager)
            .reader(reader)
            .processor(processor)
            .writer(bookWriter)
            .build();
    }

    // [2] 리뷰 랭킹 Job
    @Bean
    public Job popularReviewRankingJob(JobRepository jobRepository,
        PlatformTransactionManager transactionManager,
        JpaReviewScoreReader reader) {
        return new JobBuilder("popularReviewRankingJob", jobRepository)
            .start(popularReviewRankingStep(jobRepository, transactionManager, reader))
            .build();
    }

    @Bean
    @JobScope
    public Step popularReviewRankingStep(JobRepository jobRepository,
        PlatformTransactionManager transactionManager,
        JpaReviewScoreReader reader) {
        ItemProcessor<ReviewScoreDto, PopularReviewRanking> processor = new ReviewScoreProcessor();

        return new StepBuilder("popularReviewStep", jobRepository)
            .<ReviewScoreDto, PopularReviewRanking>chunk(100, transactionManager)
            .reader(reader)
            .processor(processor)
            .writer(reviewWriter)
            .build();
    }

    // [3] 파워 유저 랭킹 Job
    @Bean
    public Job powerUserRankingJob(JobRepository jobRepository,
        PlatformTransactionManager transactionManager,
        JpaPowerUserScoreReader reader) {
        return new JobBuilder("powerUserRankingJob", jobRepository)
            .start(powerUserRankingStep(jobRepository, transactionManager, reader))
            .build();
    }

    @Bean
    @JobScope
    public Step powerUserRankingStep(JobRepository jobRepository,
        PlatformTransactionManager transactionManager,
        JpaPowerUserScoreReader reader) {
        ItemProcessor<PowerUserScoreDto, PowerUserRanking> processor = new PowerUserScoreProcessor(em);

        return new StepBuilder("powerUserStep", jobRepository)
            .<PowerUserScoreDto, PowerUserRanking>chunk(100, transactionManager)
            .reader(reader)
            .processor(processor)
            .writer(powerUserRankingWriter)
            .build();
    }
}