package com.twogether.deokhugam.notification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.twogether.deokhugam.book.entity.Book;
import com.twogether.deokhugam.common.dto.CursorPageResponse;
import com.twogether.deokhugam.notification.dto.NotificationDto;
import com.twogether.deokhugam.notification.entity.Notification;
import com.twogether.deokhugam.notification.mapper.NotificationMapper;
import com.twogether.deokhugam.notification.repository.NotificationRepository;
import com.twogether.deokhugam.notification.service.NotificationQueryService;
import com.twogether.deokhugam.review.entity.Review;
import com.twogether.deokhugam.user.entity.User;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

@ExtendWith(MockitoExtension.class)
class NotificationQueryServiceTest {

    @InjectMocks
    private NotificationQueryService notificationQueryService;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private NotificationMapper notificationMapper;

    @Test
    void 알림_목록_조회_성공_after_없음() {
        // given
        UUID userId = UUID.randomUUID();
        LocalDateTime baseTime = LocalDateTime.now();
        int limit = 2;

        User user = mock(User.class);
        Review review = mock(Review.class);

        List<Notification> notifications = List.of(
            Notification.builder()
                .id(UUID.randomUUID())
                .content("test")
                .confirmed(false)
                .createdAt(baseTime.minusMinutes(1))
                .user(user)
                .review(review)
                .build(),
            Notification.builder()
                .id(UUID.randomUUID())
                .content("test2")
                .confirmed(true)
                .createdAt(baseTime.minusMinutes(2))
                .user(user)
                .review(review)
                .build()
        );

        when(notificationRepository.findByUserIdWithoutAfter(eq(userId), any()))
            .thenReturn(notifications);
        when(notificationMapper.toDto(any()))
            .thenAnswer(invocation -> {
                Notification n = invocation.getArgument(0);
                return new NotificationDto(
                    n.getId(),
                    userId,
                    UUID.randomUUID(),
                    "도서 제목",
                    n.getContent(),
                    n.isConfirmed(),
                    n.getCreatedAt(),
                    n.getUpdatedAt()
                );
            });

        // when
        CursorPageResponse<NotificationDto> result = notificationQueryService.getNotifications(
            userId, null, null, limit, Sort.Direction.DESC
        );

        // then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.isHasNext()).isTrue();
        assertThat(result.getSize()).isEqualTo(2);
    }

    @Test
    void 알림_목록_조회_성공_after_있음() {
        // given
        UUID userId = UUID.randomUUID();
        LocalDateTime baseTime = LocalDateTime.now();
        LocalDateTime after = baseTime.minusHours(1);
        int limit = 1;

        User user = mock(User.class);
        Review review = mock(Review.class);

        Notification notification = Notification.builder()
            .id(UUID.randomUUID())
            .content("test3")
            .confirmed(false)
            .createdAt(baseTime.minusMinutes(10))
            .user(user)
            .review(review)
            .build();

        when(notificationRepository.findByUserIdWithAfter(eq(userId), eq(after), any()))
            .thenReturn(List.of(notification));
        when(notificationMapper.toDto(any()))
            .thenReturn(new NotificationDto(
                notification.getId(),
                userId,
                UUID.randomUUID(),
                "도서 제목",
                notification.getContent(),
                notification.isConfirmed(),
                notification.getCreatedAt(),
                notification.getUpdatedAt()
            ));

        // when
        CursorPageResponse<NotificationDto> result = notificationQueryService.getNotifications(
            userId, after.toString(), after, limit, Sort.Direction.DESC
        );

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.isHasNext()).isTrue();
        assertThat(result.getSize()).isEqualTo(1);
    }
}