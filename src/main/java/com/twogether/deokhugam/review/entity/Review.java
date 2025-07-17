package com.twogether.deokhugam.review.entity;

import com.twogether.deokhugam.book.entity.Book;
import com.twogether.deokhugam.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

// 한 유저는 한 책에 한개의 리뷰만 가능
@Table(name = "reviews", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "book_id"})
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@Getter
@Entity
public class Review {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "book_title", length = 255, nullable = false)
    private String bookTitle;

    @Column(name = "book_thumbnail_url", columnDefinition = "TEXT")
    private String bookThumbnailUrl;

    @Column(name = "user_nickname", length = 50, nullable = false)
    private String userNickName;

    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "rating", nullable = false)
    private int rating;

    @Column(name = "like_count", nullable = false)
    private long likeCount = 0;

    @Column(name = "comment_count", nullable = false)
    private long commentCount = 0;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @PreUpdate
    public void onPreUpdate() {
        this.updatedAt = Instant.now();
    }

    public Review(Book book, User user, String content, int rating) {
        this.id = UUID.randomUUID();
        this.book = book;
        this.user = user;
        this.content = content;
        this.rating = rating;
        this.createdAt = Instant.now();

        this.bookTitle = book.getTitle();
        this.bookThumbnailUrl = book.getThumbnailUrl();
        this.userNickName = user.getNickname();
    }

    public void updateLikeCount(long likeCount){
        this.likeCount = likeCount;
        this.updatedAt = Instant.now();
    }

    public void updateReview(String content, int rating){
        if (!content.equals(this.content)){
            this.content = content;
        }

        if (rating != this.rating){
            this.rating = rating;
        }
    }

}
