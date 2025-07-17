package com.twogether.deokhugam.config;

import com.twogether.deokhugam.dashboard.batch.model.ReviewScoreDto;
import com.twogether.deokhugam.dashboard.batch.processor.BookScoreProcessor;
import com.twogether.deokhugam.dashboard.batch.processor.ReviewScoreProcessor;
import com.twogether.deokhugam.dashboard.batch.reader.JpaBookScoreReader;
import com.twogether.deokhugam.dashboard.batch.reader.JpaReviewScoreReader;
import com.twogether.deokhugam.dashboard.batch.writer.PopularBookRankingWriter;
import com.twogether.deokhugam.dashboard.batch.model.BookScoreDto;
import com.twogether.deokhugam.dashboard.batch.writer.PopularReviewRankingWriter;
import com.twogether.deokhugam.dashboard.entity.PopularBookRanking;
import com.twogether.deokhugam.dashboard.entity.PopularReviewRanking;
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

    // [1] 도서 랭킹 Job
    @Bean
    public Job popularBookRankingJob(JobRepository jobRepository,
        PlatformTransactionManager transactionManager) {
        return new JobBuilder("popularBookRankingJob", jobRepository)
            .start(popularBookRankingStep(jobRepository, transactionManager, null))
            .build();
    }

    @Bean
    @JobScope
    public Step popularBookRankingStep(JobRepository jobRepository,
        PlatformTransactionManager transactionManager,
        @Value("#{jobParameters['period']}") String periodKey) {

        RankingPeriod period = RankingPeriod.valueOf(periodKey.toUpperCase());
        ItemReader<BookScoreDto> reader = new JpaBookScoreReader(em, period);
        ItemProcessor<BookScoreDto, PopularBookRanking> processor = new BookScoreProcessor(em, period);

        return new StepBuilder("popularBookStep_" + period.name(), jobRepository)
            .<BookScoreDto, PopularBookRanking>chunk(100, transactionManager)
            .reader(reader)
            .processor(processor)
            .writer(bookWriter)
            .build();
    }

    // [2] 리뷰 랭킹 Job
    @Bean
    public Job popularReviewRankingJob(JobRepository jobRepository,
        PlatformTransactionManager transactionManager) {
        return new JobBuilder("popularReviewRankingJob", jobRepository)
            .start(popularReviewRankingStep(jobRepository, transactionManager, null))
            .build();
    }

    @Bean
    @JobScope
    public Step popularReviewRankingStep(JobRepository jobRepository,
        PlatformTransactionManager transactionManager,
        @Value("#{jobParameters['period']}") String periodKey) {

        RankingPeriod period = RankingPeriod.valueOf(periodKey.toUpperCase());
        ItemReader<ReviewScoreDto> reader = new JpaReviewScoreReader(em, period);
        ItemProcessor<ReviewScoreDto, PopularReviewRanking> processor = new ReviewScoreProcessor(period);

        return new StepBuilder("popularReviewStep_" + period.name(), jobRepository)
            .<ReviewScoreDto, PopularReviewRanking>chunk(100, transactionManager)
            .reader(reader)
            .processor(processor)
            .writer(reviewWriter)
            .build();
    }
}