package com.twogether.deokhugam.dashboard.entity;

import com.twogether.deokhugam.book.entity.Book;
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
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "popular_book_ranking")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class PopularBookRanking {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @NotNull
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false, foreignKey = @ForeignKey(name = "fk_popular_book_ranking_book"))
    @NotNull
    private Book book;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @NotNull
    private RankingPeriod period;

    @Column(nullable = false)
    @PositiveOrZero
    private double score;

    @Column(name = "review_count", nullable = false)
    @PositiveOrZero
    private long reviewCount;

    @Column(nullable = false)
    @PositiveOrZero
    private double rating;

    @Column(nullable = false)
    @Positive
    private int rank;

    @Column(nullable = false, length = 255)
    @NotNull
    private String title;

    @Column(nullable = false, length = 100)
    @NotNull
    private String author;

    @Column(name = "thumbnail_url", columnDefinition = "TEXT")
    private String thumbnailUrl;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public void assignRank(int rank) {
        this.rank = rank;
    }
}