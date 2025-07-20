package com.twogether.deokhugam.notification;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.twogether.deokhugam.common.dto.CursorPageResponse;
import com.twogether.deokhugam.notification.controller.NotificationController;
import com.twogether.deokhugam.notification.dto.NotificationDto;
import com.twogether.deokhugam.notification.service.NotificationQueryService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(NotificationController.class)
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NotificationQueryService notificationQueryService;

    @Test
    @DisplayName("알림 목록 조회")
    void 알림_목록_정상_조회() throws Exception {
        UUID userId = UUID.randomUUID();
        CursorPageResponse<NotificationDto> mockResponse =
            new CursorPageResponse<>(List.of(), null, null, 20, false);

        when(notificationQueryService.getNotifications(any(), any(), any(), any(), any()))
            .thenReturn(mockResponse);

        mockMvc.perform(get("/api/notifications")
                .header("Deokhugam-Request-User-ID", userId.toString()))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("알림 목록 조회 - 커서 기반 파라미터 포함")
    void 알림_목록_조회_성공_커서파라미터_포함() throws Exception {
        // given
        UUID userId = UUID.randomUUID();
        LocalDateTime time = LocalDateTime.now();

        NotificationDto dto = new NotificationDto(
            UUID.randomUUID(),
            userId,
            UUID.randomUUID(),
            "테스트 도서",
            "알림 내용입니다",
            false,
            time,
            time
        );

        CursorPageResponse<NotificationDto> mockResponse = new CursorPageResponse<>(
            List.of(dto),
            time.minusMinutes(1).toString(),
            time.minusMinutes(1),
            1,
            true
        );

        String cursor = time.minusMinutes(5).toString();
        String after = time.minusMinutes(10).toString();

        when(notificationQueryService.getNotifications(
            eq(userId),
            eq(cursor),
            eq(LocalDateTime.parse(after)),
            eq(10),
            eq(Sort.Direction.DESC)
        )).thenReturn(mockResponse);

        // when & then
        mockMvc.perform(get("/api/notifications")
                .header("Deokhugam-Request-User-ID", userId.toString())
                .param("cursor", cursor)
                .param("after", after)
                .param("limit", "10")
                .param("direction", "DESC")
            )
            .andExpect(status().isOk());
    }
}