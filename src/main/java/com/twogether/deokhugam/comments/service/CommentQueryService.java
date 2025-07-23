package com.twogether.deokhugam.comments.service;

import com.twogether.deokhugam.comments.dto.CommentResponse;
import com.twogether.deokhugam.comments.entity.Comment;
import com.twogether.deokhugam.comments.mapper.CommentMapper;
import com.twogether.deokhugam.comments.repository.CommentRepository;
import com.twogether.deokhugam.common.dto.CursorPageResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentQueryService {

    private final CommentRepository commentRepository;
    private final CommentMapper mapper;

    private static final int DEFAULT_SIZE = 20;
    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_INSTANT;
    private static final Logger log = LoggerFactory.getLogger(CommentQueryService.class);

    public CursorPageResponse<CommentResponse> getComments(
        UUID reviewId,
        Direction direction,
        String cursor,
        Instant after,
        Integer limit
    ) {
        int pageSize = (limit == null || limit < 1) ? DEFAULT_SIZE : limit;
        boolean asc = direction != null && direction.isAscending();

        // ──────────────── 커서 해석 ────────────────
        Instant afterAt = null;
        UUID afterId = null;
        if (cursor != null && !cursor.isBlank()) {
            CursorInfo info = decodeCursor(cursor);
            if (info != null) {
                afterAt = info.createdAt();
                afterId = info.id();
            }
        } else if (after != null) {
            afterAt = after;
        }

        // ──────────────── 데이터 조회 ────────────────
        List<Comment> slice = commentRepository.findSlice(
            reviewId, afterAt, afterId, pageSize, asc);

        boolean hasNext = slice.size() > pageSize;
        if (hasNext) {
            slice = slice.subList(0, pageSize);
        }

        List<CommentResponse> dtoList = slice.stream()
            .map(mapper::toResponse)
            .toList();

        // ──────────────── nextCursor 인코딩 ────────────────
        String nextCursor = null;
        if (hasNext && !slice.isEmpty()) {
            Comment last = slice.get(slice.size() - 1); // Java 17 호환
            nextCursor = encodeCursor(last.getCreatedAt(), last.getId());
        }

        return new CursorPageResponse<>(
            dtoList,
            nextCursor,
            afterAt,
            pageSize,
            hasNext
        );
    }

    // ───────────────── Cursor encode / decode helpers ─────────────────

    private String encodeCursor(Instant createdAt, UUID id) {
        String raw = ISO.format(createdAt) + "|" + id;
        return Base64.getUrlEncoder()
            .encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }

    private record CursorInfo(Instant createdAt, UUID id) {
    }

    private CursorInfo decodeCursor(String cursor) {
        try {
            String raw = new String(
                Base64.getUrlDecoder().decode(cursor),
                StandardCharsets.UTF_8);
            String[] parts = raw.split("\\|");
            return new CursorInfo(
                Instant.parse(parts[0]),
                UUID.fromString(parts[1])
            );
        } catch (Exception e) {
            log.warn("Invalid cursor format: {}", cursor, e);
            return null; // 잘못된 커서는 무시하고 after 파라미터만 사용
        }
    }
}
