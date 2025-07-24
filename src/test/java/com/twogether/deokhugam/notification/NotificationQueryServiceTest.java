package com.twogether.deokhugam.notification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.twogether.deokhugam.common.dto.CursorPageResponse;
import com.twogether.deokhugam.notification.dto.NotificationDto;
import com.twogether.deokhugam.notification.entity.Notification;
import com.twogether.deokhugam.notification.mapper.NotificationMapper;
import com.twogether.deokhugam.notification.repository.NotificationRepository;
import com.twogether.deokhugam.notification.service.NotificationQueryService;
import com.twogether.deokhugam.review.entity.Review;
import com.twogether.deokhugam.user.entity.User;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
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
        UUID userId = UUID.randomUUID();
        Instant baseTime = Instant.now();
        int limit = 2;

        User user = mock(User.class);
        Review review = mock(Review.class);

        List<Notification> notifications = List.of(
            Notification.builder()
                .id(UUID.randomUUID())
                .content("test")
                .confirmed(false)
                .createdAt(baseTime.minus(1, ChronoUnit.MINUTES))
                .updatedAt(baseTime.minus(1, ChronoUnit.MINUTES))
                .user(user)
                .review(review)
                .build(),
            Notification.builder()
                .id(UUID.randomUUID())
                .content("test2")
                .confirmed(true)
                .createdAt(baseTime.minus(2, ChronoUnit.MINUTES))
                .updatedAt(baseTime.minus(2, ChronoUnit.MINUTES))
                .user(user)
                .review(review)
                .build()
        );

        when(notificationRepository.findByUserIdWithoutAfter(eq(userId), any())).thenReturn(notifications);
        when(notificationMapper.toDto(any())).thenAnswer(invocation -> {
            Notification n = invocation.getArgument(0);
            return new NotificationDto(n.getId(), userId, UUID.randomUUID(), "도서 제목", n.getContent(), n.isConfirmed(), n.getCreatedAt(), n.getUpdatedAt());
        });

        CursorPageResponse<NotificationDto> result = notificationQueryService.getNotifications(
            userId, null, null, limit, Sort.Direction.DESC
        );

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.isHasNext()).isFalse();
        assertThat(result.getSize()).isEqualTo(2);
    }

    @Test
    void 알림_목록_조회_성공_after_있음() {
        UUID userId = UUID.randomUUID();
        Instant baseTime = Instant.now();
        Instant after = baseTime.minus(1, ChronoUnit.HOURS);
        int limit = 2;

        User user1 = mock(User.class);
        Review review1 = mock(Review.class);
        User user2 = mock(User.class);
        Review review2 = mock(Review.class);

        Notification notification1 = Notification.builder()
            .id(UUID.randomUUID())
            .content("test3")
            .confirmed(false)
            .createdAt(baseTime.minus(10, ChronoUnit.MINUTES))
            .updatedAt(baseTime.minus(10, ChronoUnit.MINUTES))
            .user(user1)
            .review(review1)
            .build();

        Notification notification2 = Notification.builder()
            .id(UUID.randomUUID())
            .content("test4")
            .confirmed(false)
            .createdAt(baseTime.minus(20, ChronoUnit.MINUTES))
            .updatedAt(baseTime.minus(20, ChronoUnit.MINUTES))
            .user(user2)
            .review(review2)
            .build();

        List<Notification> notifications = List.of(notification1, notification2);

        when(notificationRepository.findByUserIdWithAfter(eq(userId), eq(after), any()))
            .thenReturn(notifications);

        when(notificationMapper.toDto(any())).thenAnswer(invocation -> {
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

        CursorPageResponse<NotificationDto> result = notificationQueryService.getNotifications(
            userId, null, after, limit, Sort.Direction.DESC
        );

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.isHasNext()).isFalse();
        assertThat(result.getSize()).isEqualTo(2);
    }

    @Test
    void 알림_목록_조회_성공_cursor만_있는_경우() {
        UUID userId = UUID.randomUUID();
        int limit = 1;
        String cursor = Instant.now().minus(10, ChronoUnit.MINUTES).toString();
        Instant parsedCursor = Instant.parse(cursor);

        when(notificationRepository.findByUserIdWithCursor(eq(userId), eq(parsedCursor), any())).thenReturn(List.of());

        CursorPageResponse<NotificationDto> result = notificationQueryService.getNotifications(
            userId, cursor, null, limit, Sort.Direction.DESC
        );

        assertThat(result.getContent()).isEmpty();
        assertThat(result.isHasNext()).isFalse();
    }

    @Test
    void 알림_목록_조회_성공_cursor_and_after_있는_경우() {
        UUID userId = UUID.randomUUID();
        String cursor = Instant.now().minus(10, ChronoUnit.MINUTES).toString();
        Instant parsedCursor = Instant.parse(cursor);
        Instant after = Instant.now().minus(30, ChronoUnit.MINUTES);
        int limit = 1;

        when(notificationRepository.findByUserIdWithCursorAndAfter(eq(userId), eq(parsedCursor), eq(after), any())).thenReturn(List.of());

        CursorPageResponse<NotificationDto> result = notificationQueryService.getNotifications(
            userId, cursor, after, limit, Sort.Direction.DESC
        );

        assertThat(result.getContent()).isEmpty();
        assertThat(result.isHasNext()).isFalse();
    }
}