package com.twogether.deokhugam.config;

import com.twogether.deokhugam.dashboard.batch.processor.BookScoreProcessor;
import com.twogether.deokhugam.dashboard.batch.reader.JpaBookScoreReader;
import com.twogether.deokhugam.dashboard.batch.writer.PopularBookRankingWriter;
import com.twogether.deokhugam.dashboard.batch.model.BookScoreDto;
import com.twogether.deokhugam.dashboard.entity.PopularBookRanking;
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
    private final PopularBookRankingWriter writer;

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

        if (periodKey == null) {
            throw new IllegalArgumentException("JobParameter 'period' must be provided");
        }

        RankingPeriod period = RankingPeriod.valueOf(periodKey.toUpperCase());

        ItemReader<BookScoreDto> reader = new JpaBookScoreReader(em, period);
        ItemProcessor<BookScoreDto, PopularBookRanking> processor = new BookScoreProcessor(em, period);

        return new StepBuilder("popularBookStep_" + period.name(), jobRepository)
            .<BookScoreDto, PopularBookRanking>chunk(100, transactionManager)
            .reader(reader)
            .processor(processor)
            .writer(writer)
            .build();
    }
}