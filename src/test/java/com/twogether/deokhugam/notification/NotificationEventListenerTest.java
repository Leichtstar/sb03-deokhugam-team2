package com.twogether.deokhugam.notification;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.twogether.deokhugam.notification.event.CommentCreatedEvent;
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
}