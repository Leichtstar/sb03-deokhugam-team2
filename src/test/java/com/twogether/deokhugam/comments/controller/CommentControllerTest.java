package com.twogether.deokhugam.comments.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twogether.deokhugam.comments.dto.CommentResponse;
import com.twogether.deokhugam.comments.dto.CommentUpdateRequest;
import com.twogether.deokhugam.comments.exception.CommentForbiddenException;
import com.twogether.deokhugam.comments.exception.CommentNotFoundException;
import com.twogether.deokhugam.comments.service.CommentQueryService;
import com.twogether.deokhugam.comments.service.CommentService;

import com.twogether.deokhugam.common.dto.CursorPageResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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
    @Autowired
    ObjectMapper objectMapper;


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

    @Test
    @DisplayName("댓글 수정 성공")
    void patchUpdate_success() throws Exception {
        UUID commentId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        CommentResponse response = new CommentResponse(
            commentId, "수정된 내용", userId, "user1", UUID.randomUUID(),
            LocalDateTime.now(), LocalDateTime.now(), false
        );

        when(commentService.updateComment(eq(commentId), eq(userId), any(CommentUpdateRequest.class)))
            .thenReturn(response);

        mockMvc.perform(patch("/api/comments/{id}", commentId)
                .header("Deokhugam-Request-User-Id", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new CommentUpdateRequest("수정된 내용"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(commentId.toString()))
            .andExpect(jsonPath("$.content").value("수정된 내용"));
    }

    @Test
    @DisplayName("댓글 수정 시 댓글 없음(404)")
    void patchUpdate_notFound() throws Exception {
        UUID commentId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(commentService.updateComment(eq(commentId), eq(userId), any(CommentUpdateRequest.class)))
            .thenThrow(new CommentNotFoundException());

        mockMvc.perform(patch("/api/comments/{id}", commentId)
                .header("Deokhugam-Request-User-Id", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new CommentUpdateRequest("수정"))))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("댓글 수정 시 권한 없음(403)")
    void patchUpdate_forbidden() throws Exception {
        UUID commentId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(commentService.updateComment(eq(commentId), eq(userId), any(CommentUpdateRequest.class)))
            .thenThrow(new CommentForbiddenException());

        mockMvc.perform(patch("/api/comments/{id}", commentId)
                .header("Deokhugam-Request-User-Id", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new CommentUpdateRequest("수정"))))
            .andExpect(status().isForbidden());
    }

}
