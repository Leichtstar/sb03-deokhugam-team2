package com.twogether.deokhugam.dashboard.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.twogether.deokhugam.common.dto.CursorPageResponse;
import com.twogether.deokhugam.common.exception.DeokhugamException;
import com.twogether.deokhugam.common.exception.ErrorCode;
import com.twogether.deokhugam.dashboard.dto.response.PowerUserDto;
import com.twogether.deokhugam.dashboard.service.PowerUserService;
import java.util.Collections;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(PowerUserController.class)
class PowerUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PowerUserService powerUserService;

    @Test
    @DisplayName("파워 유저 목록을 정상적으로 조회할 수 있다")
    void getPowerUsers_success() throws Exception {
        // given
        CursorPageResponse<PowerUserDto> dummyResponse = new CursorPageResponse<>(
            Collections.emptyList(),
            null,
            null,
            10,
            false
        );

        Mockito.when(powerUserService.getPowerUsers(any()))
            .thenReturn(dummyResponse);

        // when & then
        mockMvc.perform(get("/api/users/power")
                .param("period", "DAILY")
                .param("direction", "ASC")
                .param("limit", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.hasNext").value(false));
    }

    @Test
    @DisplayName("잘못된 정렬 방향이면 400을 반환한다")
    void getPowerUsers_invalidDirection() throws Exception {
        Mockito.when(powerUserService.getPowerUsers(any()))
            .thenThrow(new DeokhugamException(ErrorCode.INVALID_DIRECTION));

        mockMvc.perform(get("/api/users/power")
                .param("period", "DAILY")
                .param("direction", "WRONG")
                .param("limit", "10"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("INVALID_DIRECTION"));
    }
}