package com.twogether.deokhugam.review.entity;

import com.twogether.deokhugam.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "review_like")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
public class ReviewLike {

    @EmbeddedId
    private ReviewLikePK reviewLikePK;

    @ManyToOne
    @JoinColumn(name = "review_id", insertable = false, updatable = false)
    private Review review;

    @ManyToOne
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @Column(name = "liked", nullable = false)
    private boolean liked;

    public ReviewLike(Review review, User user, boolean liked) {
        this.reviewLikePK = new ReviewLikePK(review.getId(), user.getId());
        this.review = review;
        this.user = user;
        this.liked = liked;
    }

    public void updateLike(boolean liked){
        this.liked = liked;
    }
}
