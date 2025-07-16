package com.twogether.deokhugam.review.repository.custom;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.twogether.deokhugam.book.entity.QBook;
import com.twogether.deokhugam.dashboard.dto.BookScoreDto;
import com.twogether.deokhugam.review.dto.request.ReviewSearchRequest;
import com.twogether.deokhugam.review.entity.QReview;
import com.twogether.deokhugam.review.entity.Review;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

@Slf4j
@RequiredArgsConstructor
@Repository
public class ReviewRepositoryImpl implements ReviewRepositoryCustom {

    @PersistenceContext
    private EntityManager em;
    private final JPAQueryFactory queryFactory;
    private final QReview review = QReview.review;
    private final QBook book = QBook.book;

    @Override
    public List<BookScoreDto> calculateBookScores(LocalDateTime start, LocalDateTime end) {
        return em.createQuery("""
            SELECT new com.twogether.deokhugam.dashboard.dto.BookScoreDto(
            r.book.id,
            r.book.title,
            r.book.author,
            r.book.thumbnailUrl,
            COUNT(r),
            COALESCE(AVG(r.rating), 0) 
          )
          FROM Review r
          WHERE r.createdAt >= :start AND r.createdAt < :end
          GROUP BY r.book.id, r.book.title, r.book.author, r.book.thumbnailUrl
          ORDER BY COUNT(r) DESC, COALESCE(AVG(r.rating), 0) DESC
        """, BookScoreDto.class)
        .setParameter("start", start)
        .setParameter("end", end)
        .getResultList();
    }

    @Override
    public List<BookScoreDto> calculateBookScoresAllTime() {
        String jpql = """
    SELECT new com.twogether.deokhugam.dashboard.dto.BookScoreDto(
        r.book.id,
        r.book.title,
        r.book.author,
        r.book.thumbnailUrl,
        COUNT(r),
        COALESCE(AVG(r.rating), 0)
    )
    FROM Review r
    GROUP BY r.book.id, r.book.title, r.book.author, r.book.thumbnailUrl
    ORDER BY COUNT(r) DESC, COALESCE(AVG(r.rating), 0) DESC
""";

        return em.createQuery(jpql, BookScoreDto.class).getResultList();
    }

    @Override
    public List<Review> findByFilter(ReviewSearchRequest request) {
        BooleanBuilder builder = new BooleanBuilder();

        // 검색 키워드 (부분일치)
        if (StringUtils.hasText(request.keyword())){
            builder.and(keywordLike(request.keyword()));
        }

        // 작성자 id 검색 (완전 일치)
        if (request.userId() != null){
            builder.and(review.user.id.eq(request.userId()));
        }

        // 도서 id 검색 (완전 일치)
        if (request.bookId() != null){
            builder.and(review.book.id.eq(request.bookId()));
        }

        // 만약 조건 없다면 전체 조회
        return queryFactory
                .selectFrom(review)
                .where(builder.hasValue() ? builder : null)
                .fetch();

    }

    /**
     *
     * @param keyword 시용자가 입력한 검색어
     * @return 도서 제목, 리뷰 작성자 닉네임, 리류 내용에 keyword가 들어가는지 여부
     */
    private BooleanExpression bookTitleLike(String keyword) {
        return StringUtils.hasText(keyword) ? review.bookTitle.contains(keyword) : null;
    }

    private BooleanExpression reviewerLike(String keyword) {
        return StringUtils.hasText(keyword) ? review.userNickName.contains(keyword) : null;
    }

    private BooleanExpression contentLike(String keyword) {
        return StringUtils.hasText(keyword) ? review.content.contains(keyword) : null;
    }

    /**
     *
     * @param keyword 시용자가 입력한 검색어
     * @return OR로 묶인 부분일치 조건들
     */
    private BooleanBuilder keywordLike(String keyword){
        if (!StringUtils.hasText(keyword)) return null;

        BooleanBuilder booleanBuilder = new BooleanBuilder();

        booleanBuilder.or(bookTitleLike(keyword));
        booleanBuilder.or(reviewerLike(keyword));
        booleanBuilder.or(contentLike(keyword));

        return booleanBuilder;
    }
}
