package com.twogether.deokhugam.review.repository.custom;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.twogether.deokhugam.book.entity.QBook;
import com.twogether.deokhugam.dashboard.batch.model.BookScoreDto;
import com.twogether.deokhugam.review.dto.request.ReviewSearchRequest;
import com.twogether.deokhugam.review.entity.QReview;
import com.twogether.deokhugam.review.entity.Review;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
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
            SELECT new com.twogether.deokhugam.dashboard.batch.model.BookScoreDto(
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
    SELECT new com.twogether.deokhugam.dashboard.batch.model.BookScoreDto(
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

    // 리뷰 목록 조회
    public Slice<Review> findReviewsWithCursor(ReviewSearchRequest request, Pageable pageable) {

        BooleanBuilder builder = buildSearchCondition(request);

        // 커서 조건 추가
        cursorCondition(builder, request);

        int pageSize = pageable.getPageSize();

        // 만약 조건 없다면 전체 조회
        List<Review> content  =  queryFactory
                .selectFrom(review)
                .where(builder.hasValue() ? builder : null)
                .orderBy(createOrderSpecifier(request.orderBy(), request.direction()))
                .limit(request.limit() + 1)
                .fetch();

        // hasNext 판단
        boolean hasNext = false;
        if (content.size() > pageSize){
            // 다음 요소 있는 거 확인했으니까 초과된 content는 없애버리기
            content.remove(pageSize);
            hasNext = true;
        }

        return new SliceImpl<>(content, pageable, hasNext);
    }

    // totalElement 구하기
    @Override
    public long totalElementCount(ReviewSearchRequest request) {

        BooleanBuilder builder = buildSearchCondition(request);

        // 만약 조건 없다면 전체 조회
        Long count =  queryFactory
                    .select(review.count())
                    .from(review)
                    .where(builder.hasValue() ? builder : null)
                    .fetchOne();

        return count != null ? count : 0L;
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

    /**
     * 중복 로직 분리 메서드
     */
    private BooleanBuilder buildSearchCondition(ReviewSearchRequest request){
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

        return builder;
    }

    /**
     * 정렬 조건 메서드
     */
    private OrderSpecifier<?>[] createOrderSpecifier(String orderBy, String direction){
        boolean isDesc = "DESC".equalsIgnoreCase(direction);

        // 사용자가 선택한 정렬 조건
        OrderSpecifier<?> reviews;
        if ("rating".equalsIgnoreCase(orderBy)) {
            reviews = isDesc ? review.rating.desc() : review.rating.asc();
        }
        else {
            // 기본 정렬은 createdAt
            reviews = isDesc ? review.createdAt.desc() : review.createdAt.asc();
        }

        // createdAt 으로 2차 정렬
        OrderSpecifier<?> secondary = isDesc ? review.createdAt.desc() : review.createdAt.asc();

        return new OrderSpecifier[]{reviews, secondary};
    }

    /**
     * 커서 조건 추가
     */
    private void cursorCondition(BooleanBuilder builder, ReviewSearchRequest request){
        // 1차 정렬에 따른 커서 값
        String cursor = request.cursor();

        // 이전 페이지 마지막 요소 생성시간
        String after = request.after();

        // 첫 페이지인 경우
        if(after == null){
            return;
        }

        try{
            Instant afterTime = Instant.parse(after);
            boolean isDesc = "DESC".equalsIgnoreCase(request.direction());

            if ("rating".equalsIgnoreCase(request.orderBy())){
                ratingCursor(builder, afterTime, cursor, isDesc);
            }
            else{
                createdAtCursor(builder, cursor, isDesc);
            }
        }
        catch (Exception e){
            log.warn("커서 조건을 위한 값 파싱 실패: {} ", after);
        }
    }

    // 평점 정렬 시 커서
    private void ratingCursor(BooleanBuilder builder, Instant afterTime, String cursor, boolean isDesc){
        if (cursor == null){
            if (isDesc) {
                builder.and(review.createdAt.lt(afterTime));
            }
            else{
                builder.and(review.createdAt.gt(afterTime));
            }
            return;
        }

        try {
            int cursorRating = Integer.parseInt(cursor);

            if (isDesc) {
                BooleanExpression cursorCondition = review.rating.lt(cursorRating)
                        .or(review.rating.eq(cursorRating).and(review.createdAt.lt(afterTime)));

                builder.and(cursorCondition);
            }
            else{
                BooleanExpression cursorCondition = review.rating.gt(cursorRating)
                        .or(review.rating.eq(cursorRating).and(review.createdAt.gt(afterTime)));

                builder.and(cursorCondition);
            }
        }catch (NumberFormatException e){
            // 파싱 오류 발생 시 -> 생성 시간만으로 커서 조건 생성
            if (isDesc) {
                builder.and(review.createdAt.lt(afterTime));
            }
            else{
                builder.and(review.createdAt.gt(afterTime));
            }
        }
    }

    // 생성 시간 기준 커서 조건
    private void createdAtCursor(BooleanBuilder builder, String cursor, boolean isDesc){
        try {
            Instant cursorTime = Instant.parse(cursor);

            if (isDesc) {
                builder.and(review.createdAt.lt(cursorTime));
            } else {
                builder.and(review.createdAt.gt(cursorTime));
            }
        }
        catch (Exception e){
            log.warn("커서 조건을 위한 값 파싱 실패: {} ", cursor);
        }
    }
}
