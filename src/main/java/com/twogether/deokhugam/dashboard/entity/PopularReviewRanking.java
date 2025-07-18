package com.twogether.deokhugam.dashboard.entity;

import com.twogether.deokhugam.book.entity.Book;
import com.twogether.deokhugam.review.entity.Review;
import com.twogether.deokhugam.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "popular_review_ranking")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class PopularReviewRanking {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @NotNull
    private UUID id;

    @NotNull
    @Column(name = "review_id", columnDefinition = "UUID", nullable = false)
    private UUID reviewId;

    @NotNull
    @Column(name = "user_id", columnDefinition = "UUID", nullable = false)
    private UUID userId;

    @NotNull
    @Column(name = "user_nickname", length = 50, nullable = false)
    private String userNickname;

    @NotNull
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @PositiveOrZero
    @Column(nullable = false)
    private double rating;

    @NotNull
    @Column(name = "book_id", columnDefinition = "UUID", nullable = false)
    private UUID bookId;

    @NotNull
    @Column(name = "book_title", length = 255, nullable = false)
    private String bookTitle;

    @Column(name = "book_thumbnail_url", columnDefinition = "TEXT")
    private String bookThumbnailUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @NotNull
    private RankingPeriod period;

    @PositiveOrZero
    @Column(nullable = false)
    private double score;

    @PositiveOrZero
    @Column(name = "like_count", nullable = false)
    private long likeCount;

    @PositiveOrZero
    @Column(name = "comment_count", nullable = false)
    private long commentCount;

    @Positive
    @Column(nullable = false)
    private int rank;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public void assignRank(int rank) {
        this.rank = rank;
    }
}