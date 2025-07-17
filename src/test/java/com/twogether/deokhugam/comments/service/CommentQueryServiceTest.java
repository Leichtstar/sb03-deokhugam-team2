package com.twogether.deokhugam.comments.service;

import com.twogether.deokhugam.comments.dto.CommentResponse;
import com.twogether.deokhugam.comments.entity.Comment;
import com.twogether.deokhugam.comments.mapper.CommentMapper;
import com.twogether.deokhugam.comments.repository.CommentRepository;
import com.twogether.deokhugam.common.dto.CursorPageResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class CommentQueryServiceTest {

    @Mock CommentRepository commentRepository;
    @Mock CommentMapper commentMapper;
    @InjectMocks CommentQueryService sut;

    private static Comment stubComment(UUID reviewId, UUID id, LocalDateTime at, boolean deleted) {
        Comment c = mock(Comment.class);
        when(c.getId()).thenReturn(id);
        when(c.getCreatedAt()).thenReturn(at);
        return c;
    }

    private static CommentResponse stubDto(Comment c) {
        return new CommentResponse(
            c.getId(),
            "dummy",
            null,
            null, null,
            c.getCreatedAt(),
            null,
            c.getIsDeleted());
    }

    @Test
    void shouldReturnFirstPageInDescendingOrder() {
        UUID reviewId = UUID.randomUUID();
        Comment c1 = stubComment(reviewId, UUID.randomUUID(), LocalDateTime.now(), false);

        when(commentRepository.findSlice(eq(reviewId), isNull(), isNull(), eq(1), eq(false)))
            .thenReturn(List.of(c1));

        CommentResponse dto = new CommentResponse(c1.getId(), "x", null, null, null,
            c1.getCreatedAt(), null, false);
        when(commentMapper.toResponse(c1)).thenReturn(dto);

        CursorPageResponse<CommentResponse> res =
            sut.getComments(reviewId, Sort.Direction.DESC, null, null, 1);

        assertThat(res.getContent()).hasSize(1);
    }

    @Test
    void asc_with_cursor() {
        UUID reviewId = UUID.randomUUID();
        LocalDateTime base = LocalDateTime.of(2025,1,1,0,0);
        UUID baseId = UUID.randomUUID();
        String cursorRaw = DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(base) + "|" + baseId;
        String cursor = Base64.getUrlEncoder().encodeToString(cursorRaw.getBytes(UTF_8));

        Comment next = stubComment(reviewId, UUID.randomUUID(),
            base.plusSeconds(5), false);

        when(commentRepository.findSlice(eq(reviewId),
            eq(base), eq(baseId), eq(50), eq(true)))
            .thenReturn(List.of(next));

        CommentResponse dto = new CommentResponse(next.getId(), "y", null,
            null, null, next.getCreatedAt(), null, false);
        when(commentMapper.toResponse(next)).thenReturn(dto);

        CursorPageResponse<CommentResponse> res =
            sut.getComments(reviewId, Sort.Direction.ASC, cursor, null, null);

        assertThat(res.getNextCursor()).isNull();
    }

    @Test
    void shouldFallbackToDefaultWhenCursorIsInvalid() {
        UUID reviewId = UUID.randomUUID();
        LocalDateTime after = LocalDateTime.now().minusDays(1);

        // lenient 로 StrictStubs 무시
        lenient().when(commentRepository.findSlice(eq(reviewId),
                eq(after), isNull(), anyInt(), anyBoolean()))
            .thenReturn(List.of());

        sut.getComments(reviewId, Sort.Direction.DESC, "%%%bad%%%", after, 10);

        verify(commentRepository)
            .findSlice(eq(reviewId), isNull(), isNull(), anyInt(), eq(false));
    }

    @Test
    void limit_zero_replaced_by_defaultSize() {
        UUID rid = UUID.randomUUID();
        when(commentRepository.findSlice(eq(rid), isNull(), isNull(), eq(50), eq(false)))
            .thenReturn(List.of());

        sut.getComments(rid, Sort.Direction.DESC, null, null, 0);

        verify(commentRepository).findSlice(rid, null, null, 50, false);
    }

    @Test
    void null_direction_defaults_desc() {
        UUID rid = UUID.randomUUID();
        when(commentRepository.findSlice(eq(rid), isNull(), isNull(), anyInt(), eq(false)))
            .thenReturn(List.of());

        sut.getComments(rid, null, null, null, 5);

        verify(commentRepository).findSlice(eq(rid), isNull(), isNull(), eq(5), eq(false));
    }

    @Test
    void last_page_nextCursor_is_null() {
        UUID rid = UUID.randomUUID();
        Comment c = stubComment(rid, UUID.randomUUID(), LocalDateTime.now(), false);
        when(commentRepository.findSlice(eq(rid), isNull(), isNull(), eq(10), eq(false)))
            .thenReturn(List.of(c));      // size <= limit → hasNext=false

        CursorPageResponse<CommentResponse> res =
            sut.getComments(rid, Sort.Direction.DESC, null, null, 10);

        assertThat(res.getNextCursor()).isNull();
    }

    @Test
    void valid_cursor_decodes_and_passes_to_repo() {
        UUID rid = UUID.randomUUID();
        LocalDateTime base = LocalDateTime.of(2025,7,15,12,0);
        UUID baseId = UUID.randomUUID();

        // cursor 인코딩
        String raw = DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(base) + "|" + baseId;
        String cursor = Base64.getUrlEncoder().encodeToString(raw.getBytes(UTF_8));

        Comment c = stubComment(rid, UUID.randomUUID(), base.plusSeconds(1), false);
        CommentResponse dto = stubDto(c);

        when(commentRepository.findSlice(eq(rid), eq(base), eq(baseId), eq(50), eq(true)))
            .thenReturn(List.of(c));
        when(commentMapper.toResponse(c)).thenReturn(dto);


        sut.getComments(rid, Sort.Direction.ASC, cursor, null, null);

        verify(commentRepository).findSlice(rid, base, baseId, 50, true);
    }

    @Test
    void malformed_cursor_hits_catch_branch() {
        UUID rid = UUID.randomUUID();

        sut.getComments(rid, Sort.Direction.DESC, "abcd", null, 5);

        verify(commentRepository).findSlice(eq(rid), isNull(), isNull(), eq(5), eq(false));
    }

    @Test
    void hasNext_true_sets_nextCursor() {
        UUID rid = UUID.randomUUID();
        Comment c1 = stubComment(rid, UUID.randomUUID(), LocalDateTime.now(), false);
        Comment c2 = stubComment(rid, UUID.randomUUID(), LocalDateTime.now().minusSeconds(1), false);

        CommentResponse dto1 = stubDto(c1);

        when(commentRepository.findSlice(eq(rid), isNull(), isNull(), eq(1), eq(false)))
            .thenReturn(List.of(c1, c2));
        when(commentMapper.toResponse(any())).thenReturn(dto1);  // dto1 반환


        CursorPageResponse<CommentResponse> res =
            sut.getComments(rid, Sort.Direction.DESC, null, null, 1);

        assertThat(res.getNextCursor()).isNotNull();        // ③ 분기 실행 검증
    }


}
