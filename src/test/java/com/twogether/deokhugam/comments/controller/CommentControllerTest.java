package com.twogether.deokhugam.comments.controller;

import com.twogether.deokhugam.comments.dto.CommentResponse;
import com.twogether.deokhugam.comments.service.CommentQueryService;
import com.twogether.deokhugam.comments.service.CommentService;

import com.twogether.deokhugam.common.dto.CursorPageResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CommentController.class)
class CommentControllerTest {

    @Autowired
    MockMvc mockMvc;
    @MockitoBean
    CommentService commentService;          // 등록 API 용
    @MockitoBean
    CommentQueryService queryService;       // 목록 API 용

    @Test
    void listEndpoint_mapsToServiceAndReturns200() throws Exception {
        UUID rid = UUID.randomUUID();
        CursorPageResponse<CommentResponse> dummy =
            new CursorPageResponse<>(List.of(), null, null, 0, false);
        when(queryService.getComments(any(), any(), any(), any(), any()))
            .thenReturn(dummy);

        mockMvc.perform(get("/api/comments")
                .param("reviewId", rid.toString())
                .param("direction", "DESC"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray());
    }
}
