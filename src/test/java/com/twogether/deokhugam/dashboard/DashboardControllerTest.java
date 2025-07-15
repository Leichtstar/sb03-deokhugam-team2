package com.twogether.deokhugam.dashboard;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.twogether.deokhugam.common.dto.CursorPageResponse;
import com.twogether.deokhugam.common.exception.DeokhugamException;
import com.twogether.deokhugam.common.exception.ErrorCode;
import com.twogether.deokhugam.dashboard.controller.DashboardController;
import com.twogether.deokhugam.dashboard.dto.response.PopularBookDto;
import com.twogether.deokhugam.dashboard.service.DashboardService;
import java.util.Collections;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(DashboardController.class)
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DashboardService dashboardService;

    @Test
    @DisplayName("인기 도서 목록을 정상적으로 조회할 수 있다")
    void getPopularBooks_success() throws Exception {
        // given
        CursorPageResponse<PopularBookDto> dummyResponse = new CursorPageResponse<>(
            Collections.emptyList(), // content
            null,                   // nextCursor
            null,                   // nextAfter
            10,                     // size
            false                   // hasNext
        );

        Mockito.when(dashboardService.getPopularBooks(any()))
            .thenReturn(dummyResponse);

        // when & then
        mockMvc.perform(get("/api/books/popular")
                .param("period", "DAILY")
                .param("direction", "ASC")
                .param("limit", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.hasNext").value(false));
    }

    @Test
    @DisplayName("잘못된 정렬 방향이면 400을 반환한다")
    void getPopularBooks_invalidDirection() throws Exception {
        Mockito.when(dashboardService.getPopularBooks(any()))
            .thenThrow(new DeokhugamException(ErrorCode.INVALID_DIRECTION));

        mockMvc.perform(get("/api/books/popular")
                .param("period", "DAILY")
                .param("direction", "WRONG")
                .param("limit", "10"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("INVALID_DIRECTION"));
    }
}
