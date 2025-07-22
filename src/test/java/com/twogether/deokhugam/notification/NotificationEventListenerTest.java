package com.twogether.deokhugam.notification;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.twogether.deokhugam.notification.event.CommentCreatedEvent;
import com.twogether.deokhugam.notification.event.PopularReviewRankedEvent;
import com.twogether.deokhugam.notification.event.ReviewLikedEvent;
import com.twogether.deokhugam.notification.listener.NotificationEventListener;
import com.twogether.deokhugam.notification.repository.NotificationRepository;
import com.twogether.deokhugam.notification.service.NotificationService;
import com.twogether.deokhugam.review.entity.Review;
import com.twogether.deokhugam.user.entity.User;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NotificationEventListenerTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private NotificationEventListener notificationEventListener;

    @Test
    void 댓글_작성_이벤트_발생시_알림이_저장된다() {
        // given
        User receiver = mock(User.class);
        User commenter = mock(User.class);
        Review review = mock(Review.class);

        UUID receiverId = UUID.randomUUID();
        UUID commenterId = UUID.randomUUID();

        when(receiver.getId()).thenReturn(receiverId);
        when(commenter.getId()).thenReturn(commenterId);
        when(review.getUser()).thenReturn(receiver);

        String commentContent = "댓글 내용입니다.";
        CommentCreatedEvent event = new CommentCreatedEvent(commenter, review, commentContent);

        // when
        notificationEventListener.handle(event);

        // then
        verify(notificationService).createCommentNotification(commenter, review, commentContent);
    }

    @Test
    void 리뷰_좋아요_이벤트_발생시_알림이_저장된다() {
        // given
        User receiver = mock(User.class);
        UUID receiverId = UUID.randomUUID();
        when(receiver.getId()).thenReturn(receiverId);

        User liker = mock(User.class);
        UUID likerId = UUID.randomUUID();
        when(liker.getId()).thenReturn(likerId);

        Review review = mock(Review.class);
        when(review.getUser()).thenReturn(receiver);

        ReviewLikedEvent event = new ReviewLikedEvent(liker, review);

        // when
        notificationEventListener.handle(event);

        // then
        verify(notificationService).createLikeNotification(liker, review);
    }

    @Test
    void 인기_리뷰_진입_이벤트_발생시_알림이_저장된다() {
        // given
        User user = mock(User.class);
        Review review = mock(Review.class);

        PopularReviewRankedEvent event = new PopularReviewRankedEvent(user, review);

        // when
        notificationEventListener.handlePopularReviewRanked(event);

        // then
        verify(notificationService).createRankingNotification(user, review);
    }

    @Test
    void 댓글_작성자가_작성자_본인이면_알림이_생성되지_않는다() {
        // given
        UUID id = UUID.randomUUID();
        User sameUser = mock(User.class);
        when(sameUser.getId()).thenReturn(id);

        Review review = mock(Review.class);
        when(review.getUser()).thenReturn(sameUser);

        CommentCreatedEvent event = new CommentCreatedEvent(sameUser, review, "자기 댓글");

        // when
        notificationEventListener.handle(event);

        // then
        verify(notificationService, never()).createCommentNotification(any(), any(), any());
    }

    @Test
    void 리뷰_좋아요_작성자가_본인이면_알림이_생성되지_않는다() {
        // given
        UUID id = UUID.randomUUID();
        User sameUser = mock(User.class);
        when(sameUser.getId()).thenReturn(id);

        Review review = mock(Review.class);
        when(review.getUser()).thenReturn(sameUser);

        ReviewLikedEvent event = new ReviewLikedEvent(sameUser, review);

        // when
        notificationEventListener.handle(event);

        // then
        verify(notificationService, never()).createLikeNotification(any(), any());
    }
}