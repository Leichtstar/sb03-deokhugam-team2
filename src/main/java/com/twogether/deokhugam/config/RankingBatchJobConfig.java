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
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@EnableBatchProcessing
@RequiredArgsConstructor
public class RankingBatchJobConfig {

    private final EntityManager em;

    @Bean
    public Job popularBookRankingJob(JobRepository jobRepository, Step popularBookRankingStep) {
        return new JobBuilder("popularBookRankingJob", jobRepository)
            .start(popularBookRankingStep)
            .build();
    }

    @Bean
    public Step popularBookRankingStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("popularBookRankingStep", jobRepository)
            .<BookScoreDto, PopularBookRanking>chunk(100, transactionManager)
            .reader(bookScoreReader())
            .processor(bookScoreProcessor())
            .writer(bookScoreWriter())
            .build();
    }

    @Bean
    public ItemReader<BookScoreDto> bookScoreReader() {
        return new JpaBookScoreReader(em, LocalDateTime.now().minusDays(1), LocalDateTime.now());
    }

    @Bean
    public ItemProcessor<BookScoreDto, PopularBookRanking> bookScoreProcessor() {
        return new BookScoreProcessor(em, RankingPeriod.DAILY);
    }

    @Bean
    public ItemWriter<PopularBookRanking> bookScoreWriter() {
        return items -> {
            for (PopularBookRanking ranking : items) {
                em.persist(ranking);
            }
            em.flush();
            em.clear();
        };
    }
}