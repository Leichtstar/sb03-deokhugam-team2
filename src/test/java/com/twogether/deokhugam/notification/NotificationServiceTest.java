package com.twogether.deokhugam.notification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.twogether.deokhugam.notification.dto.NotificationDto;
import com.twogether.deokhugam.notification.entity.Notification;
import com.twogether.deokhugam.notification.exception.NotificationNotFoundException;
import com.twogether.deokhugam.notification.mapper.NotificationMapper;
import com.twogether.deokhugam.notification.repository.NotificationRepository;
import com.twogether.deokhugam.notification.service.NotificationService;
import com.twogether.deokhugam.review.entity.Review;
import com.twogether.deokhugam.user.entity.User;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class NotificationServiceTest {

    private NotificationService notificationService;
    private NotificationRepository notificationRepository;
    private NotificationMapper notificationMapper;

    private User writer;
    private User otherUser;
    private Review review;

    @BeforeEach
    void setUp() {
        notificationRepository = mock(NotificationRepository.class);
        notificationMapper = mock(NotificationMapper.class);
        notificationService = new NotificationService(notificationRepository, notificationMapper);

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

    @Test
    void 알림_확인_상태_변경_성공() {
        // given
        UUID notificationId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Notification notification = mock(Notification.class);
        NotificationDto dto = mock(NotificationDto.class);

        when(notificationRepository.findByIdAndUserId(notificationId, userId))
            .thenReturn(Optional.of(notification));
        when(notificationMapper.toDto(notification)).thenReturn(dto);

        // when
        NotificationDto result = notificationService.updateConfirmedStatus(notificationId, userId, true);

        // then
        assertThat(result).isEqualTo(dto);
        verify(notification).setConfirmed(true);
        verify(notification).setUpdatedAt(any(LocalDateTime.class));
    }

    @Test
    void 알림이_없으면_예외를_던진다() {
        // given
        UUID notificationId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(notificationRepository.findByIdAndUserId(notificationId, userId))
            .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() ->
            notificationService.updateConfirmedStatus(notificationId, userId, true)
        ).isInstanceOf(NotificationNotFoundException.class);
    }
}