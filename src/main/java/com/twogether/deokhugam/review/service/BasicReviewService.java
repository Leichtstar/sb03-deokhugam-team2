package com.twogether.deokhugam.review.service;

import com.twogether.deokhugam.book.entity.Book;
import com.twogether.deokhugam.book.exception.BookNotFoundException;
import com.twogether.deokhugam.book.repository.BookRepository;
import com.twogether.deokhugam.book.service.BookService;
import com.twogether.deokhugam.common.dto.CursorPageResponseDto;
import com.twogether.deokhugam.notification.service.NotificationService;
import com.twogether.deokhugam.review.dto.ReviewDto;
import com.twogether.deokhugam.review.dto.ReviewLikeDto;
import com.twogether.deokhugam.review.dto.request.ReviewCreateRequest;
import com.twogether.deokhugam.review.dto.request.ReviewSearchRequest;
import com.twogether.deokhugam.review.dto.request.ReviewUpdateRequest;
import com.twogether.deokhugam.review.entity.Review;
import com.twogether.deokhugam.review.entity.ReviewLike;
import com.twogether.deokhugam.review.exception.ReviewExistException;
import com.twogether.deokhugam.review.exception.ReviewLikeNotFoundException;
import com.twogether.deokhugam.review.exception.ReviewNotFoundException;
import com.twogether.deokhugam.review.exception.ReviewNotOwnedException;
import com.twogether.deokhugam.review.mapper.ReviewLikeMapper;
import com.twogether.deokhugam.review.mapper.ReviewMapper;
import com.twogether.deokhugam.review.repository.ReviewLikeRepository;
import com.twogether.deokhugam.review.repository.ReviewRepository;
import com.twogether.deokhugam.review.service.util.ReviewCursorHelper;
import com.twogether.deokhugam.user.entity.User;
import com.twogether.deokhugam.user.repository.UserRepository;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class BasicReviewService implements ReviewService{

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final ReviewLikeRepository reviewLikeRepository;
    private final ReviewMapper reviewMapper;
    private final ReviewLikeMapper reviewLikeMapper;
    private final ReviewCursorHelper reviewCursorHelper;
    private final BookService bookService;
    private final NotificationService notificationService;

    // 리뷰 생성
    @Override
    @Transactional
    public ReviewDto create(ReviewCreateRequest request) {
        // 이미 존재하는 리뷰 시 예외
        if (reviewRepository.existsByUserIdAndBookIdAndIsDeletedFalse(request.userId(), request.bookId())){
            throw new ReviewExistException(request.userId(), request.bookId());
        }

        // 리뷰 작성하려는 책, 유저
        Book reviewedBook = bookRepository.findById(request.bookId())
                .orElseThrow(
                        () -> new NoSuchElementException("책을 찾을 수 없습니다. " + request.bookId()));

        User reviewer = userRepository.findById(request.userId())
                .orElseThrow(
                        () -> new NoSuchElementException("사용자를 찾을 수 없습니다. " + request.userId()));

        Review review = new Review(reviewedBook, reviewer, request.content(), request.rating());
        reviewRepository.save(review);

        ReviewLike reviewLike = new ReviewLike(
                review,
                reviewer,
                false
        );

        bookRepository.updateBookReviewStats(request.bookId());
        bookRepository.save(reviewedBook);

        reviewLikeRepository.save(reviewLike);
        bookService.updateReviewStats(request.bookId());
        log.info("[BasicReviewService] 리뷰 등록 성공");

        return reviewMapper.toDto(review, false);
    }

    // 리뷰 상세 조회
    @Override
    @Transactional(readOnly = true)
    public ReviewDto findById(UUID reviewId, UUID requestUserId){
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(
                        () -> new ReviewNotFoundException(reviewId));

        boolean likeByMe = reviewLikeRepository.findByUserIdAndReviewId(requestUserId, reviewId)
                    .map(ReviewLike::isLiked)
                    .orElse(false);

        log.info("[BasicReviewService]: 리뷰 조회 완료");

        ReviewDto test =  reviewMapper.toDto(review, likeByMe);
        return reviewMapper.toDto(review, likeByMe);
    }

    // 리뷰 목록 조회
    @Override
    @Transactional(readOnly = true)
    public CursorPageResponseDto<ReviewDto> findReviews(ReviewSearchRequest request) {
        // Pageable 생성
        Pageable pageable = PageRequest.of(0, request.limit());

        // Slice 메서드 호출
        Slice<Review> slice = reviewRepository.findReviewsWithCursor(request, pageable);

        // 좋아요 정보 조회
        Map<UUID, Boolean> likeByMeMap = reviewCursorHelper.getLikeByMeMap(slice.getContent(), request.requestUserId());

        // DTO 변환
        List<ReviewDto> reviewDtos = slice.getContent().stream()
                .map(review -> {
                            boolean likeByMe = likeByMeMap.getOrDefault(review.getId(), false);
                            return reviewMapper.toDto(review, likeByMe);
                        })
                .toList();

        // totalElement 구하기
        long totalElement = reviewRepository.totalElementCount(request);

        // 다음 커서 생성
        String nextCursor = slice.hasNext() ? reviewCursorHelper.generateNextCursor(slice.getContent(), request.orderBy()) : null;

        // after 생성
        String after = slice.hasNext() ? reviewCursorHelper.generateAfter(slice.getContent()) : null;

        // 반환값 생성
        CursorPageResponseDto<ReviewDto> responseDtoTest = new CursorPageResponseDto<>(
                reviewDtos,
                nextCursor,
                after,
                request.limit(),
                totalElement,
                slice.hasNext()
        );

        return responseDtoTest;
    }

    // 리뷰 수정 (리뷰 내용, 평점)
    @Override
    @Transactional
    public ReviewDto updateReview(UUID reviewId, UUID requestUserId, ReviewUpdateRequest updateRequest) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException(reviewId));

        if (!review.getUser().getId().equals(requestUserId)){
            throw new ReviewNotOwnedException();
        }
        review.updateReview(updateRequest.content(), updateRequest.rating());
        reviewRepository.save(review);

        log.info("[BasicReviewService]: 리뷰 수정 완료 newContent: {}, newRating: {}", updateRequest.content(), updateRequest.rating());

        boolean likeByMe = reviewLikeRepository.findByUserIdAndReviewId(requestUserId, reviewId)
                .map(ReviewLike::isLiked)
                .orElse(false);
        return reviewMapper.toDto(review, likeByMe);
    }

    // 리뷰 논리 삭제
    @Override
    @Transactional
    public void deleteReviewSoft(UUID reviewId, UUID requestUserId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException(reviewId));

        Book reviewedBook = bookRepository.findById(review.getBook().getId())
                .orElseThrow(BookNotFoundException::new);

        if (!review.getUser().getId().equals(requestUserId)){
            throw new ReviewNotOwnedException();
        }
        review.updateIsDelete(true);

        bookRepository.updateBookReviewStats(reviewedBook.getId());
        bookRepository.save(reviewedBook);

        reviewRepository.save(review);
        bookService.updateReviewStats(review.getBook().getId());

        log.info("[BasicReviewService]: 리뷰 논리 삭제 완료");
    }

    // 리뷰 물리 삭제
    @Override
    @Transactional
    public void deleteReviewHard(UUID reviewId, UUID requestUserId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException(reviewId));

        Book reviewedBook = bookRepository.findById(review.getBook().getId())
                .orElseThrow(BookNotFoundException::new);

        if (!review.getUser().getId().equals(requestUserId)){
            throw new ReviewNotOwnedException();
        }

        reviewRepository.delete(review);

        bookRepository.updateBookReviewStats(reviewedBook.getId());
        bookRepository.save(reviewedBook);

        log.info("[BasicReviewService]: 리뷰 물리 삭제 완료");
    }

    // 리뷰 좋아요 기능
    @Override
    @Transactional
    public ReviewLikeDto reviewLike(UUID reviewId, UUID userId) {

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(
                        () -> new ReviewNotFoundException(reviewId));

        // 좋아요가 비어있다면
        if (reviewLikeRepository.findByUserIdAndReviewId(userId, reviewId).isEmpty()){
            User reviewer = userRepository.findById(userId)
                    .orElseThrow(
                            () -> new NoSuchElementException("사용자를 찾을 수 없습니다. " + userId));

            ReviewLike newReviewLike = new ReviewLike(
                    review,
                    reviewer,
                    true
            );

            review.updateLikeCount(review.getLikeCount() + 1);

            reviewLikeRepository.save(newReviewLike);
            reviewRepository.save(review);

            // 좋아요 알림 생성 트리거
            if (!review.getUser().getId().equals(userId)) {
                notificationService.createLikeNotification(reviewer, review);
            }

            return reviewLikeMapper.toDto(newReviewLike);
        }
        else{

            ReviewLike reviewLike = reviewLikeRepository.findByUserIdAndReviewId(userId, reviewId)
                    .orElseThrow(
                            () -> new ReviewLikeNotFoundException());

            if (reviewLike.isLiked()){
                // 좋아요 상태가 true 라면
                reviewLike.updateLike(false);
                review.updateLikeCount(review.getLikeCount() - 1);
            }
            else{
                // 좋아요가 false 라면
                reviewLike.updateLike(true);
                review.updateLikeCount(review.getLikeCount() + 1);
            }

            reviewLikeRepository.save(reviewLike);
            reviewRepository.save(review);

            return reviewLikeMapper.toDto(reviewLike);
        }
    }
}
