package com.twogether.deokhugam.notification.service;

import com.twogether.deokhugam.notification.entity.Notification;
import com.twogether.deokhugam.notification.repository.NotificationRepository;
import com.twogether.deokhugam.review.entity.Review;
import com.twogether.deokhugam.user.entity.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    /**
     * 좋아요 알림 생성
     */
    @Transactional
    public void createLikeNotification(User liker, Review review) {
        User receiver = review.getUser();
        if (liker.getId().equals(receiver.getId())) return;

        String content = String.format("%s님이 나의 리뷰를 좋아요 했습니다.", liker.getNickname());
        Notification notification = Notification.of(receiver, review, content);
        notificationRepository.save(notification);
    }

    /**
     * 댓글 알림 생성
     */
    @Transactional
    public void createCommentNotification(User commenter, Review review, String commentContent) {
        User receiver = review.getUser();
        if (commenter.getId().equals(receiver.getId())) return;

        String content = String.format("%s님이 나의 리뷰에 댓글을 남겼습니다.\n%s",
            commenter.getNickname(), commentContent);

        Notification notification = Notification.of(receiver, review, content);
        notificationRepository.save(notification);
    }

    /**
     * 인기 리뷰 랭킹 진입 알림 생성
     */
    @Transactional
    public void createRankingNotification(User receiver, Review review) {
        String content = "나의 리뷰가 인기 리뷰 순위에 진입했습니다!";
        Notification notification = Notification.of(receiver, review, content);
        notificationRepository.save(notification);
    }
}