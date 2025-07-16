package com.twogether.deokhugam.comments.service;

import com.twogether.deokhugam.comments.dto.CommentResponse;
import com.twogether.deokhugam.comments.entity.Comment;
import com.twogether.deokhugam.comments.mapper.CommentMapper;
import com.twogether.deokhugam.comments.repository.CommentRepository;
import com.twogether.deokhugam.common.exception.dto.CursorPageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
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

    private static final int DEFAULT_SIZE = 50;
    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public CursorPageResponse<CommentResponse> getComments(
        UUID reviewId,
        Direction direction,
        String cursor,
        LocalDateTime after,
        Integer limit
    ) {
        int pageSize = (limit == null || limit < 1) ? DEFAULT_SIZE : limit;
        boolean asc = direction == null ? false : direction.isAscending();

        // 커서 해석
        LocalDateTime afterAt = null;
        UUID afterId = null;
        if (cursor != null && !cursor.isBlank()) {
            try {
                String raw = new String(Base64.getUrlDecoder().decode(cursor), StandardCharsets.UTF_8);
                String[] parts = raw.split("\\|");
                afterAt = LocalDateTime.parse(parts[0], ISO);
                afterId = UUID.fromString(parts[1]);
            } catch (Exception ignored) { }
        } else if (after != null) {
            afterAt = after;
        }

        // 데이터 조회
        List<Comment> slice = commentRepository.findSlice(
            reviewId, afterAt, afterId, pageSize, asc);

        boolean hasNext = slice.size() > pageSize;
        if (hasNext) slice = slice.subList(0, pageSize);

        List<CommentResponse> dtoList = slice.stream()
            .map(mapper::toResponse)
            .toList();

        // nextCursor 인코딩
        String nextCursor = null;
        if (hasNext && !slice.isEmpty()) {
            Comment last = slice.get(slice.size() - 1);
            String raw = ISO.format(last.getCreatedAt()) + "|" + last.getId();
            nextCursor = Base64.getUrlEncoder().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
        }

        return new CursorPageResponse<>(dtoList, nextCursor);
    }
}