package com.twogether.deokhugam.notification;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.twogether.deokhugam.notification.entity.Notification;
import com.twogether.deokhugam.notification.repository.NotificationRepository;
import com.twogether.deokhugam.notification.service.NotificationService;
import com.twogether.deokhugam.review.entity.Review;
import com.twogether.deokhugam.user.entity.User;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class NotificationServiceTest {

    private NotificationService notificationService;
    private NotificationRepository notificationRepository;

    private User writer;
    private User otherUser;
    private Review review;

    @BeforeEach
    void setUp() {
        notificationRepository = mock(NotificationRepository.class);
        notificationService = new NotificationService(notificationRepository);

        // 작성자
        writer = mock(User.class);
        UUID writerId = UUID.randomUUID();
        when(writer.getId()).thenReturn(writerId);
        when(writer.getNickname()).thenReturn("작성자");

        // 리뷰
        review = mock(Review.class);
        when(review.getUser()).thenReturn(writer);

        // 댓글 작성자 or 좋아요 누른 사람
        otherUser = mock(User.class);
        UUID otherUserId = UUID.randomUUID();
        when(otherUser.getId()).thenReturn(otherUserId);
        when(otherUser.getNickname()).thenReturn("댓글쓴이");
    }

    @Test
    void 댓글_알림이_정상적으로_생성된다() {
        notificationService.createCommentNotification(otherUser, review, "댓글 내용");

        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    void 좋아요_알림이_정상적으로_생성된다() {
        notificationService.createLikeNotification(otherUser, review);

        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    void 랭킹_알림이_정상적으로_생성된다() {
        notificationService.createRankingNotification(writer, review);

        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    void 자신에게는_댓글_알림이_생성되지_않는다() {
        notificationService.createCommentNotification(writer, review, "자기 댓글");

        verify(notificationRepository, never()).save(any(Notification.class));
    }

    @Test
    void 자신에게는_좋아요_알림이_생성되지_않는다() {
        notificationService.createLikeNotification(writer, review);

        verify(notificationRepository, never()).save(any(Notification.class));
    }
}