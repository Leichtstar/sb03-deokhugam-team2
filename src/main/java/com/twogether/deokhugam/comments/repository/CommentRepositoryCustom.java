package com.twogether.deokhugam.comments.repository;

import com.twogether.deokhugam.comments.entity.Comment;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface CommentRepositoryCustom {
    List<Comment> findSlice(UUID reviewId,
                            Instant afterCreatedAt,
                            UUID afterId,
                            int limit,
                            boolean asc);
}
