package com.twogether.deokhugam.review.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.twogether.deokhugam.book.entity.Book;
import com.twogether.deokhugam.book.repository.BookRepository;
import com.twogether.deokhugam.review.dto.ReviewDto;
import com.twogether.deokhugam.review.dto.ReviewLikeDto;
import com.twogether.deokhugam.review.dto.request.ReviewCreateRequest;
import com.twogether.deokhugam.review.entity.Review;
import com.twogether.deokhugam.review.entity.ReviewLike;
import com.twogether.deokhugam.review.exception.ReviewExistException;
import com.twogether.deokhugam.review.mapper.ReviewLikeMapper;
import com.twogether.deokhugam.review.mapper.ReviewMapper;
import com.twogether.deokhugam.review.repository.ReviewLikeRepository;
import com.twogether.deokhugam.review.repository.ReviewRepository;
import com.twogether.deokhugam.user.entity.User;
import com.twogether.deokhugam.user.repository.UserRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("Review 단위 테스트")
public class BasicReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private ReviewLikeRepository reviewLikeRepository;

    @Mock
    private ReviewMapper reviewMapper;

    @Mock
    private ReviewLikeMapper reviewLikeMapper;

   @InjectMocks
   private BasicReviewService basicReviewService;

    private UUID bookId;
    private UUID userId;
    private UUID reviewId;
    private ReviewCreateRequest reviewCreateRequest;
    private Book testBook;
    private User testUser;
    private Review testReview;
    private ReviewLike testReviewLike;

    @BeforeEach
    void setup() {
        // Mock 객체 주입 확인
        assertNotNull(reviewRepository);
        assertNotNull(userRepository);
        assertNotNull(bookRepository);
        assertNotNull(reviewMapper);
        assertNotNull(basicReviewService);

        bookId = UUID.randomUUID();
        userId = UUID.randomUUID();
        reviewId = UUID.randomUUID();

        reviewCreateRequest = new ReviewCreateRequest(
                bookId, userId, "재밌는 책이다.", 4
        );

        testBook = new Book(
        "더쿠의 심리학",
        "박인규",
        "더쿠에 대한 심도깊은 해설",
        "이북리더즈",
        LocalDate.of(1989, 5, 12)
        );

        testUser = new User(
                "test@test.com",
                "테스트",
                "Asdf1234!"
        );

        testReview = new Review(
                testBook,
                testUser,
                "재밌는 책입니다.",
                3
        );

        testReviewLike = new ReviewLike(
                testReview,
                testUser,
                true
        );
    }

    @Test
    @DisplayName("유효한 요청으로 리뷰를 생성할 수 있다.")
    void create_Review() {
        // Mock 객체
        Book mockBook = mock(Book.class);
        User mockUser = mock(User.class);
        ReviewLike mockReviewLike = mock(ReviewLike.class);
        ReviewDto expectedDto = mock(ReviewDto.class);

        Review review = new Review(mockBook, mockUser, "재밌는 책이다.", 4);

        when(reviewRepository.existsByUserIdAndBookId(userId, bookId)).thenReturn(false);
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(mockBook));
        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        when(reviewRepository.save(any(Review.class))).thenReturn(review);
        when(reviewLikeRepository.save(any(ReviewLike.class))).thenReturn(mockReviewLike);
        when(reviewMapper.toDto(any(Review.class))).thenReturn(expectedDto);

        // When
        ReviewDto result = basicReviewService.create(reviewCreateRequest);

        // Then
        assertEquals(expectedDto, result);
        verify(reviewRepository).save(any(Review.class));
        verify(reviewLikeRepository).save(any(ReviewLike.class));
    }

    @Test
    @DisplayName("이미 리뷰가 존재하는 경우 예외가 발생한다.")
    void review_exist() {
        when(reviewRepository.existsByUserIdAndBookId(userId, bookId)).thenReturn(true);

        // when & then
        assertThrows(ReviewExistException.class, () -> {
            basicReviewService.create(reviewCreateRequest);
        });

        // 혹시라도 save()가 호출되면 안 됨
        verify(reviewRepository, never()).save(any());
    }

    @Test
    @DisplayName("도서를 찾을 수 없는 경우 예외가 발생한다.")
    void book_notFound() {
        when(reviewRepository.existsByUserIdAndBookId(userId, bookId)).thenReturn(false);
        when(bookRepository.findById(bookId)).thenReturn(Optional.empty());

        // when & then
        assertThrows(NoSuchElementException.class, () -> {
            basicReviewService.create(reviewCreateRequest);
        });

        // 혹시라도 save()가 호출되면 안 됨
        verify(reviewRepository, never()).save(any());
    }

    @Test
    @DisplayName("사용자를 찾을 수 없는 경우 예외가 발생한다.")
    void user_notFound() {
        Book mockBook = mock(Book.class);

        when(reviewRepository.existsByUserIdAndBookId(userId, bookId)).thenReturn(false);
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(mockBook));

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // when & then
        assertThrows(NoSuchElementException.class, () -> {
            basicReviewService.create(reviewCreateRequest);
        });

        // 혹시라도 save()가 호출되면 안 됨
        verify(reviewRepository, never()).save(any());
    }

    @Nested
    @DisplayName("리뷰 조회 테스트")
    class ReadReviewTest {

        /**
         * 리뷰 상세 기능에 대한 테스트 - refactor 단계
         * 아직 상세 조회 메서드가 존재하지 않으므로 컴파일 에러 발생
         */
        @Test
        @DisplayName("id로 리뷰를 조회하면 해당하는 리뷰를 반환해야 한다. - 리뷰 상세 조회")
        void shouldReturnReview_whenGivenValidId() {

            // Given
            testReview.updateLikeCount(5L); // 좋아요 수를 예시로 세팅

            // 기대하는 Dto
            ReviewDto expectedDto = new ReviewDto(
                    testReview.getId(),
                    testBook.getId(),
                    testBook.getTitle(),
                    testBook.getThumbnailUrl(),
                    testUser.getId(),
                    testUser.getNickname(),
                    testReview.getContent(),
                    testReview.getRating(),
                    testReview.getLikeCount(),
                    testReview.getCommentCount(),
                    true, // likedByMe
                    testReview.getCreatedAt(),
                    testReview.getUpdatedAt()
            );

            when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(testReview));
            when(reviewLikeRepository.findByUserIdAndReviewId(userId, reviewId)).thenReturn(Optional.of(testReviewLike));

            // When
            ReviewDto result = basicReviewService.findById(reviewId, userId);

            // Then
            assertEquals(expectedDto.likedByMe(), result.likedByMe());
            assertEquals(expectedDto, result);
        }

        @Test
        @DisplayName("RED: 문자열로 리뷰 목록을 검색하면 조건에 맞는 목록이 조회되어야 한다.")
        void shouldReturnReviewList_whenGivenValidFilter(){

            // 테스트용 검색어
            String keyword = "더쿠";

            ReviewDto expectedDto1 = mock(ReviewDto.class);
            ReviewDto expectedDto2 = mock(ReviewDto.class);
            List<ReviewDto> expectedResult = List.of(expectedDto1, expectedDto2);

            when(reviewRepository.findByFilter(keyword)).thenReturn(expectedResult);

            // When
            List<ReviewDto> result = basicReviewService.findReviews(keyword);

            // Then
            assertEquals(2, result.size());
            assertEquals(expectedResult, result);
            verify(reviewRepository).findByFilter(keyword);
        }
    }

    @Test
    @DisplayName("리뷰 좋아요를 취소할 수 있어야 한다.")
    void shouldUpdate_ReviewLike_Unlike(){
        // Given
        testReview.updateLikeCount(3L);

        ReviewLike reviewLike = new ReviewLike(
                testReview,
                testUser,
                true
        );

        ReviewLikeDto expectedDto = new ReviewLikeDto(
                reviewId,
                userId,
                false
        );

        when(reviewLikeRepository.findByUserIdAndReviewId(userId, reviewId)).thenReturn(Optional.of(reviewLike));
        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(testReview));
        when(reviewLikeMapper.toDto(reviewLike)).thenReturn(expectedDto);

        // When
        ReviewLikeDto result = basicReviewService.reviewLike(reviewId, userId);

        // Then
        assertEquals(false, result.liked());
        assertEquals(2L, testReview.getLikeCount());
        verify(reviewLikeRepository).save(reviewLike);
        verify(reviewRepository).save(testReview);
    }

    @Test
    @DisplayName("리뷰 좋아요를 추가할 수 있어야 한다.")
    void shouldUpdate_ReviewLike_like(){
        // Given
        testReview.updateLikeCount(3L);

        ReviewLike reviewLike = new ReviewLike(
                testReview,
                testUser,
                false
        );

        ReviewLikeDto expectedDto = new ReviewLikeDto(
                reviewId,
                userId,
                true
        );

        when(reviewLikeRepository.findByUserIdAndReviewId(userId, reviewId)).thenReturn(Optional.of(reviewLike));
        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(testReview));
        when(reviewLikeMapper.toDto(reviewLike)).thenReturn(expectedDto);

        // When
        ReviewLikeDto result = basicReviewService.reviewLike(reviewId, userId);

        // Then
        assertEquals(true, result.liked());
        assertEquals(4L, testReview.getLikeCount());
        verify(reviewLikeRepository).save(reviewLike);
        verify(reviewRepository).save(testReview);
    }



}
