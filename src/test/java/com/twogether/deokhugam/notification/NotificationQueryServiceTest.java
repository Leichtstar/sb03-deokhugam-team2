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
    void 알림_목록_조회_성공() {
        // given
        UUID userId = UUID.randomUUID();
        LocalDateTime baseTime = LocalDateTime.now();
        int limit = 2;

        User user = mock(User.class);
        Review review = mock(Review.class);
        Book book = mock(Book.class);

        // 알림 엔티티 더미 생성
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

        when(notificationRepository.findByUserIdWithCursor(eq(userId), any(), any()))
            .thenReturn(notifications);

        // 실제 DTO 반환 (createdAt 포함)
        when(notificationMapper.toDto(any()))
            .thenReturn(new NotificationDto(
                UUID.randomUUID(),
                userId,
                UUID.randomUUID(),
                "테스트 도서",
                "알림 내용",
                false,
                baseTime.minusMinutes(1),
                baseTime.minusMinutes(1)
            ));

        // when
        CursorPageResponse<NotificationDto> result = notificationQueryService.getNotifications(
            userId, null, null, limit, Sort.Direction.DESC
        );

        // then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.isHasNext()).isTrue();
        assertThat(result.getSize()).isEqualTo(2);
    }
}