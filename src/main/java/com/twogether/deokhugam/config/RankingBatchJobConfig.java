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
import com.twogether.deokhugam.user.entity.User;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
public class RankingBatchJobConfig {

    private final EntityManager em;
    private final MeterRegistry meterRegistry;

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

        ItemProcessor<BookScoreDto, PopularBookRanking> processor =
            new BookScoreProcessor(em, meterRegistry);

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
        JpaReviewScoreReader reader,
        ItemProcessor<ReviewScoreDto, PopularReviewRanking> reviewScoreProcessor) {
        return new JobBuilder("popularReviewRankingJob", jobRepository)
            .start(popularReviewRankingStep(jobRepository, transactionManager, reader, reviewScoreProcessor))
            .build();
    }

    @Bean
    @JobScope
    public Step popularReviewRankingStep(JobRepository jobRepository,
        PlatformTransactionManager transactionManager,
        JpaReviewScoreReader reader,
        ItemProcessor<ReviewScoreDto, PopularReviewRanking> reviewScoreProcessor) {
        return new StepBuilder("popularReviewStep", jobRepository)
            .<ReviewScoreDto, PopularReviewRanking>chunk(100, transactionManager)
            .reader(reader)
            .processor(reviewScoreProcessor)
            .writer(reviewWriter)
            .build();
    }

    @Bean
    @JobScope
    public ItemProcessor<ReviewScoreDto, PopularReviewRanking> reviewScoreProcessor(
        @Value("#{jobParameters['now']}") String now
    ) {
        return new ReviewScoreProcessor(Instant.parse(now), meterRegistry, em);
    }

    // [3] 파워 유저 랭킹 Job
    @Bean
    public Job powerUserRankingJob(JobRepository jobRepository,
        PlatformTransactionManager transactionManager,
        JpaPowerUserScoreReader reader,
        ItemProcessor<PowerUserScoreDto, PowerUserRanking> powerUserScoreProcessor) {
        return new JobBuilder("powerUserRankingJob", jobRepository)
            .start(powerUserRankingStep(jobRepository, transactionManager, reader, powerUserScoreProcessor))
            .build();
    }

    @Bean
    @JobScope
    public Step powerUserRankingStep(JobRepository jobRepository,
        PlatformTransactionManager transactionManager,
        JpaPowerUserScoreReader reader,
        ItemProcessor<PowerUserScoreDto, PowerUserRanking> powerUserScoreProcessor) {
        return new StepBuilder("powerUserStep", jobRepository)
            .<PowerUserScoreDto, PowerUserRanking>chunk(100, transactionManager)
            .reader(reader)
            .processor(powerUserScoreProcessor)
            .writer(powerUserRankingWriter)
            .build();
    }

    @Bean
    @JobScope
    public ItemProcessor<PowerUserScoreDto, PowerUserRanking> powerUserScoreProcessor(
        @Value("#{jobParameters['now']}") String now,
        @Value("#{jobParameters['period']}") String periodString
    ) {
        Instant nowInstant = Instant.parse(now);
        RankingPeriod period = RankingPeriod.valueOf(periodString);
        boolean isAllTime = (period == RankingPeriod.ALL_TIME);
        Instant start = isAllTime ? null : period.getStartTime(nowInstant);
        Instant end = isAllTime ? null : period.getEndTime(nowInstant);

        Set<UUID> userIds = new HashSet<>();

        // 1. 리뷰 유저
        var q1 = em.createQuery("""
                SELECT DISTINCT r.user.id FROM Review r
                WHERE r.isDeleted = false""" + (isAllTime ? "" : " AND r.createdAt BETWEEN :start AND :end"),
            UUID.class);
        if (!isAllTime) {
            q1.setParameter("start", start);
            q1.setParameter("end", end);
        }
        userIds.addAll(q1.getResultList());

        // 2. 댓글 유저
        var q2 = em.createQuery("""
                SELECT DISTINCT c.user.id FROM Comment c
                WHERE c.isDeleted = false""" + (isAllTime ? "" : " AND c.createdAt BETWEEN :start AND :end"),
            UUID.class);
        if (!isAllTime) {
            q2.setParameter("start", start);
            q2.setParameter("end", end);
        }
        userIds.addAll(q2.getResultList());

        // 3. 좋아요 유저
        var q3 = em.createQuery("""
                SELECT DISTINCT l.reviewLikePK.userId FROM ReviewLike l
                WHERE l.liked = true""" + (isAllTime ? "" : " AND l.review.createdAt BETWEEN :start AND :end"),
            UUID.class);
        if (!isAllTime) {
            q3.setParameter("start", start);
            q3.setParameter("end", end);
        }
        userIds.addAll(q3.getResultList());

        // 4. User Map 생성
        Map<UUID, User> userMap = em.createQuery("""
                SELECT u FROM User u WHERE u.id IN :ids
            """, User.class)
            .setParameter("ids", userIds)
            .getResultList().stream()
            .collect(Collectors.toMap(User::getId, u -> u));

        return new PowerUserScoreProcessor(userMap, nowInstant, meterRegistry, em);
    }
}
