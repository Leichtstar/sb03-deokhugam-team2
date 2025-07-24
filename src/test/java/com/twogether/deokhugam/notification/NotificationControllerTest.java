package com.twogether.deokhugam.notification;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twogether.deokhugam.common.dto.CursorPageResponse;
import com.twogether.deokhugam.notification.controller.NotificationController;
import com.twogether.deokhugam.notification.dto.NotificationDto;
import com.twogether.deokhugam.notification.dto.NotificationUpdateRequest;
import com.twogether.deokhugam.notification.exception.NotificationNotFoundException;
import com.twogether.deokhugam.notification.service.NotificationQueryService;
import com.twogether.deokhugam.notification.service.NotificationService;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(NotificationController.class)
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private NotificationQueryService notificationQueryService;

    @MockitoBean
    private NotificationService notificationService;

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
        Instant time = Instant.now();

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
            time.minusSeconds(60).toString(),
            time.minusSeconds(60),
            1,
            true
        );

        String cursor = time.minusSeconds(300).toString();
        String after = time.minusSeconds(600).toString();

        when(notificationQueryService.getNotifications(
            eq(userId),
            eq(cursor),
            eq(Instant.parse(after)),
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

    @DisplayName("알림 목록 조회 - 사용자 ID 헤더 누락 시 400 응답")
    @Test
    void 알림_조회_실패_헤더_누락() throws Exception {
        mockMvc.perform(get("/api/notifications"))
            .andExpect(status().isBadRequest());
    }

    @DisplayName("알림 목록 조회 - 음수 limit 파라미터")
    @Test
    void 알림_조회_실패_음수_limit() throws Exception {
        UUID userId = UUID.randomUUID();

        mockMvc.perform(get("/api/notifications")
                .header("Deokhugam-Request-User-ID", userId.toString())
                .param("limit", "-5"))
            .andExpect(status().isBadRequest());
    }

    @DisplayName("알림 목록 조회 - 유효하지 않은 정렬 direction")
    @Test
    void 알림_조회_실패_잘못된_direction() throws Exception {
        UUID userId = UUID.randomUUID();

        mockMvc.perform(get("/api/notifications")
                .header("Deokhugam-Request-User-ID", userId.toString())
                .param("direction", "INVALID"))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("알림 읽음 처리 - 성공")
    void 알림_읽음_처리_성공() throws Exception {
        UUID notificationId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Instant now = Instant.now();

        NotificationDto dto = new NotificationDto(
            notificationId,
            userId,
            UUID.randomUUID(),
            "테스트 도서",
            "알림 내용",
            true,
            now,
            now
        );

        NotificationUpdateRequest request = new NotificationUpdateRequest(true);
        String requestBody = objectMapper.writeValueAsString(request);

        when(notificationService.updateConfirmedStatus(notificationId, userId, true))
            .thenReturn(dto);

        mockMvc.perform(patch("/api/notifications/" + notificationId)
                .header("Deokhugam-Request-User-ID", userId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.confirmed").value(true));
    }

    @Test
    @DisplayName("알림 읽음 처리 - 알림이 존재하지 않으면 404")
    void 알림_읽음_실패_알림없음() throws Exception {
        UUID notificationId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        NotificationUpdateRequest request = new NotificationUpdateRequest(true);
        String requestBody = objectMapper.writeValueAsString(request);

        when(notificationService.updateConfirmedStatus(any(), any(), anyBoolean()))
            .thenThrow(new NotificationNotFoundException());

        mockMvc.perform(patch("/api/notifications/" + notificationId)
                .header("Deokhugam-Request-User-ID", userId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("전체 알림 읽음 처리 - 성공")
    void 전체_알림_읽음_처리_성공() throws Exception {
        // given
        UUID userId = UUID.randomUUID();

        // when & then
        mockMvc.perform(patch("/api/notifications/read-all")
                .header("Deokhugam-Request-User-ID", userId.toString()))
            .andExpect(status().isNoContent());

        verify(notificationService).markAllAsRead(userId);
    }
}