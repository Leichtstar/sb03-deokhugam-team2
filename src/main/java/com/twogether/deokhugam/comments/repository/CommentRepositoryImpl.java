package com.twogether.deokhugam.comments.repository;

import com.querydsl.core.types.OrderSpecifier;
import com.twogether.deokhugam.comments.entity.QComment;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.twogether.deokhugam.comments.entity.Comment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class CommentRepositoryImpl implements CommentRepositoryCustom {

    private final JPAQueryFactory query;
    private static final QComment COMMENT = QComment.comment;

    @Override
    public List<Comment> findSlice(UUID reviewId,
                                   LocalDateTime afterCreatedAt,
                                   UUID afterId,
                                   int limit,
                                   boolean asc) {

        /* ---------- where ---------- */
        BooleanBuilder where = new BooleanBuilder()
            .and(COMMENT.isDeleted.isFalse())
            .and(COMMENT.review.id.eq(reviewId));

        if (afterCreatedAt != null) {
            // ASC이면 gt / DESC이면 lt
            BooleanExpression cursorCmp = asc
                ? COMMENT.createdAt.gt(afterCreatedAt)
                : COMMENT.createdAt.lt(afterCreatedAt);

        BooleanExpression tieBreakCmp = null;
        if (afterId != null) {
            tieBreakCmp = asc
                ? COMMENT.createdAt.eq(afterCreatedAt).and(COMMENT.id.gt(afterId))
                : COMMENT.createdAt.eq(afterCreatedAt).and(COMMENT.id.lt(afterId));
        }

            where.and(tieBreakCmp != null ? cursorCmp.or(tieBreakCmp) : cursorCmp);
        }

        OrderSpecifier<?> createdOrder = asc
            ? COMMENT.createdAt.asc()
            : COMMENT.createdAt.desc();

        OrderSpecifier<?> idOrder = asc
            ? COMMENT.id.asc()
            : COMMENT.id.desc();

        return query.selectFrom(COMMENT)
            .where(where)
            .orderBy(createdOrder, idOrder)
            .limit(limit + 1)        // 다음 페이지 여부 확인용 +1
            .fetch();
    }
}
