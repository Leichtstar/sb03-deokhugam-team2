package com.twogether.deokhugam.notification.event;

import com.twogether.deokhugam.review.entity.Review;
import com.twogether.deokhugam.user.entity.User;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class PopularReviewRankedEvent extends ApplicationEvent {

    private final User user;
    private final Review review;

    public PopularReviewRankedEvent(User user, Review review) {
        super(user); // source는 user로 지정
        this.user = user;
        this.review = review;
    }
}