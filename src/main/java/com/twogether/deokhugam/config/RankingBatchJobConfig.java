package com.twogether.deokhugam.config;

import com.twogether.deokhugam.dashboard.batch.BookScoreProcessor;
import com.twogether.deokhugam.dashboard.batch.JpaBookScoreReader;
import com.twogether.deokhugam.dashboard.batch.model.BookScoreDto;
import com.twogether.deokhugam.dashboard.entity.PopularBookRanking;
import com.twogether.deokhugam.dashboard.entity.RankingPeriod;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
public class RankingBatchJobConfig {

    private final EntityManager em;

    @Bean
    public Job popularBookRankingJob(JobRepository jobRepository,
        Step popularBookDailyStep,
        Step popularBookWeeklyStep,
        Step popularBookMonthlyStep,
        Step popularBookAllTimeStep) {
        return new JobBuilder("popularBookRankingJob", jobRepository)
            .start(popularBookDailyStep)
            .next(popularBookWeeklyStep)
            .next(popularBookMonthlyStep)
            .next(popularBookAllTimeStep)
            .build();
    }

    @Bean
    public Step popularBookDailyStep(JobRepository jobRepository,
        PlatformTransactionManager transactionManager) {
        LocalDateTime now = LocalDateTime.now();
        return createStep(jobRepository, transactionManager, "popularBookDailyStep",
            RankingPeriod.DAILY, now.minusDays(1), now);
    }

    @Bean
    public Step popularBookWeeklyStep(JobRepository jobRepository,
        PlatformTransactionManager transactionManager) {
        LocalDateTime now = LocalDateTime.now();
        return createStep(jobRepository, transactionManager, "popularBookWeeklyStep",
            RankingPeriod.WEEKLY, now.minusWeeks(1), now);
    }

    @Bean
    public Step popularBookMonthlyStep(JobRepository jobRepository,
        PlatformTransactionManager transactionManager) {
        LocalDateTime now = LocalDateTime.now();
        return createStep(jobRepository, transactionManager, "popularBookMonthlyStep",
            RankingPeriod.MONTHLY, now.minusMonths(1), now);
    }

    @Bean
    public Step popularBookAllTimeStep(JobRepository jobRepository,
        PlatformTransactionManager transactionManager) {
        return createStep(jobRepository, transactionManager, "popularBookAllTimeStep",
            RankingPeriod.ALL_TIME, null, null);
    }

    private Step createStep(JobRepository jobRepository,
        PlatformTransactionManager transactionManager,
        String stepName,
        RankingPeriod period,
        LocalDateTime start,
        LocalDateTime end) {

        ItemReader<BookScoreDto> reader = new JpaBookScoreReader(em, start, end);
        ItemProcessor<BookScoreDto, PopularBookRanking> processor = new BookScoreProcessor(em, period.name());
        ItemWriter<PopularBookRanking> writer = items -> {
            for (PopularBookRanking ranking : items) em.persist(ranking);
            em.flush();
            em.clear();
        };

        return new StepBuilder(stepName, jobRepository)
            .<BookScoreDto, PopularBookRanking>chunk(100, transactionManager)
            .reader(reader)
            .processor(processor)
            .writer(writer)
            .build();
    }
}