package com.twogether.deokhugam.notification;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.twogether.deokhugam.common.dto.CursorPageResponse;
import com.twogether.deokhugam.notification.controller.NotificationController;
import com.twogether.deokhugam.notification.dto.NotificationDto;
import com.twogether.deokhugam.notification.service.NotificationQueryService;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(NotificationController.class)
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NotificationQueryService notificationQueryService;

    @Test
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
}