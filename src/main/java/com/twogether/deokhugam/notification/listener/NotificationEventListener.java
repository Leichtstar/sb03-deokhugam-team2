package com.twogether.deokhugam.notification.listener;

import com.twogether.deokhugam.notification.event.CommentCreatedEvent;
import com.twogether.deokhugam.notification.event.PopularReviewRankedEvent;
import com.twogether.deokhugam.notification.event.ReviewLikedEvent;
import com.twogether.deokhugam.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private final NotificationService notificationService;

    @EventListener
    public void handle(CommentCreatedEvent event) {
        if (!event.commenter().getId().equals(event.review().getUser().getId())) {
            notificationService.createCommentNotification(event.commenter(), event.review(), event.commentContent());
        }
    }

    @EventListener
    public void handle(ReviewLikedEvent event) {
        if (!event.liker().getId().equals(event.review().getUser().getId())) {
            notificationService.createLikeNotification(event.liker(), event.review());
        }
    }

    @Async
    @EventListener
    public void handlePopularReviewRanked(PopularReviewRankedEvent event) {
        log.info("[NotificationEventListener] 인기 리뷰 랭킹 알림 트리거 - userId={}, reviewId={}",
            event.getUser().getId(), event.getReview().getId());

        notificationService.createRankingNotification(event.getUser(), event.getReview());
    }
}