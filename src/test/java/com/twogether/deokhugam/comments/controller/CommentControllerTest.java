package com.twogether.deokhugam.comments.controller;

import com.twogether.deokhugam.comments.dto.CommentResponse;
import com.twogether.deokhugam.comments.service.CommentQueryService;
import com.twogether.deokhugam.comments.service.CommentService;

import com.twogether.deokhugam.common.dto.CursorPageResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
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

    @Test
    @DisplayName("댓글 단건 조회 성공")
    void getComment_success() throws Exception {
        UUID id = UUID.randomUUID();
        String content = "테스트";
        UUID userId = UUID.randomUUID();
        String userNickname = "user1";
        UUID reviewId = UUID.randomUUID();
        LocalDateTime createdAt = LocalDateTime.now();
        LocalDateTime updatedAt = LocalDateTime.now();
        Boolean isDeleted = false;

        CommentResponse dto = new CommentResponse(
            id, content, userId, userNickname, reviewId, createdAt, updatedAt, isDeleted
        );

        when(commentService.getComment(id)).thenReturn(dto);

        mockMvc.perform(get("/api/comments/{id}", id))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(id.toString()))
            .andExpect(jsonPath("$.content").value(content))
            .andExpect(jsonPath("$.userId").value(userId.toString()))
            .andExpect(jsonPath("$.userNickname").value(userNickname))
            .andExpect(jsonPath("$.reviewId").value(reviewId.toString()))
            .andExpect(jsonPath("$.isDeleted").value(isDeleted));
    }

    @Test
    @DisplayName("댓글 단건 조회 시 NotFound 예외")
    void getComment_notFound() throws Exception {
        UUID id = UUID.randomUUID();
        when(commentService.getComment(id)).thenThrow(new NoSuchElementException("댓글이 없습니다."));

        mockMvc.perform(get("/api/comments/{id}", id))
            .andExpect(status().isNotFound());
    }

}
