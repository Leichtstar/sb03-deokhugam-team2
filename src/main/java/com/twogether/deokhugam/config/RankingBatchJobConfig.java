package com.twogether.deokhugam.config;

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
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableBatchProcessing
@RequiredArgsConstructor
public class RankingBatchJobConfig {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final EntityManager em;

    @Bean
    public Job popularBookRankingJob(Step popularBookRankingStep) {
        return jobBuilderFactory.get("popularBookRankingJob")
            .incrementer(new RunIdIncrementer())
            .start(popularBookRankingStep)
            .build();
    }

    @Bean
    public Step popularBookRankingStep() {
        return stepBuilderFactory.get("popularBookRankingStep")
            .<BookScoreDto, PopularBookRanking>chunk(100)
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