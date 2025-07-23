package com.twogether.deokhugam.review.service;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.twogether.deokhugam.book.entity.Book;
import com.twogether.deokhugam.book.exception.BookNotFoundException;
import com.twogether.deokhugam.book.repository.BookRepository;
import com.twogether.deokhugam.common.dto.CursorPageResponseDto;
import com.twogether.deokhugam.review.dto.ReviewDto;
import com.twogether.deokhugam.review.dto.ReviewLikeDto;
import com.twogether.deokhugam.review.dto.request.ReviewCreateRequest;
import com.twogether.deokhugam.review.dto.request.ReviewSearchRequest;
import com.twogether.deokhugam.review.dto.request.ReviewUpdateRequest;
import com.twogether.deokhugam.review.entity.Review;
import com.twogether.deokhugam.review.entity.ReviewLike;
import com.twogether.deokhugam.review.exception.ReviewExistException;
import com.twogether.deokhugam.review.exception.ReviewNotOwnedException;
import com.twogether.deokhugam.review.mapper.ReviewLikeMapper;
import com.twogether.deokhugam.review.mapper.ReviewMapper;
import com.twogether.deokhugam.review.repository.ReviewLikeRepository;
import com.twogether.deokhugam.review.repository.ReviewRepository;
import com.twogether.deokhugam.review.service.util.ReviewCursorHelper;
import com.twogether.deokhugam.user.entity.User;
import com.twogether.deokhugam.user.exception.UserNotFoundException;
import com.twogether.deokhugam.user.repository.UserRepository;
import jakarta.validation.Validator;
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
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

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

    @Mock
    private ReviewCursorHelper reviewCursorHelper;

    // 알림 이벤트
    @Mock
    private ApplicationEventPublisher eventPublisher;

   @InjectMocks
   private BasicReviewService basicReviewService;

    private Validator validator;
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
        // Validator 설정
        LocalValidatorFactoryBean validatorFactoryBean = new LocalValidatorFactoryBean();
        validatorFactoryBean.afterPropertiesSet();
        validator = validatorFactoryBean.getValidator();

        // Mock 객체 주입 확인
        assertNotNull(reviewRepository);
        assertNotNull(userRepository);
        assertNotNull(bookRepository);
        assertNotNull(reviewLikeRepository);
        assertNotNull(reviewMapper);
        assertNotNull(basicReviewService);
        assertNotNull(eventPublisher);

        // verify 검증을 위해 ReviewCursorHelper를 진짜 객체로 대체함 (내부에는 mock된 reviewLikeRepository 주입)
        reviewCursorHelper = new ReviewCursorHelper(reviewLikeRepository);
        ReflectionTestUtils.setField(basicReviewService, "reviewCursorHelper", reviewCursorHelper);

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
    @DisplayName("리뷰 내용이 없는 경우 예외가 발생해야 한다.")
    void shouldNotValid_whenReviewIsEmpty() {
        // Given
        ReviewCreateRequest invalidReviewRequest = new ReviewCreateRequest(
                bookId,
                userId,
                "",
                2
        );

        // When & Then
        var violations = validator.validate(invalidReviewRequest);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(violation -> violation.getMessage().equals("리뷰 내용은 필수 입력 항목입니다.")));

    }

    @Test
    @DisplayName("리뷰 내용이 5000자를 넘는 경우 예외가 발생해야 한다.")
    void shouldNotValid_whenReviewContentIsOverSize() {
        // Given
        String tooLongContent = "a".repeat(5001);
        ReviewCreateRequest invalidReviewRequest = new ReviewCreateRequest(
                bookId,
                userId,
                tooLongContent,
                2
        );

        // When & Then
        var violations = validator.validate(invalidReviewRequest);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(violation -> violation.getMessage().equals("내용은 5000자를 초과할 수 없습니다.")));

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

        when(reviewRepository.existsByUserIdAndBookIdAndIsDeletedFalse(userId, bookId)).thenReturn(false);
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(mockBook));
        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        when(reviewRepository.save(any(Review.class))).thenReturn(review);
        when(reviewLikeRepository.save(any(ReviewLike.class))).thenReturn(mockReviewLike);
        when(reviewMapper.toDto(any(Review.class), anyBoolean())).thenReturn(expectedDto);

        // When
        ReviewDto result = basicReviewService.create(reviewCreateRequest);

        // Then
        assertEquals(expectedDto, result);

        verify(reviewRepository).save(any(Review.class));
        verify(bookRepository).updateBookReviewStats(bookId);
        verify(bookRepository).save(any(Book.class));
        verify(reviewLikeRepository).save(any(ReviewLike.class));
    }

    @Test
    @DisplayName("이미 리뷰가 존재하는 경우 예외가 발생한다.")
    void review_exist() {
        when(reviewRepository.existsByUserIdAndBookIdAndIsDeletedFalse(userId, bookId)).thenReturn(true);

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
        when(reviewRepository.existsByUserIdAndBookIdAndIsDeletedFalse(userId, bookId)).thenReturn(false);
        when(bookRepository.findById(bookId)).thenReturn(Optional.empty());

        // when & then
        assertThrows(BookNotFoundException.class, () -> {
            basicReviewService.create(reviewCreateRequest);
        });

        // 혹시라도 save()가 호출되면 안 됨
        verify(reviewRepository, never()).save(any());
    }

    @Test
    @DisplayName("사용자를 찾을 수 없는 경우 예외가 발생한다.")
    void user_notFound() {
        Book mockBook = mock(Book.class);

        when(reviewRepository.existsByUserIdAndBookIdAndIsDeletedFalse(userId, bookId)).thenReturn(false);
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(mockBook));

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // when & then
        assertThrows(UserNotFoundException.class, () -> {
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
            when(reviewMapper.toDto(testReview, true)).thenReturn(expectedDto);
            when(reviewLikeRepository.findByUserIdAndReviewId(userId, reviewId)).thenReturn(Optional.of(testReviewLike));

            // When
            ReviewDto result = basicReviewService.findById(reviewId, userId);

            // Then
            assertEquals(expectedDto.likedByMe(), result.likedByMe());
            assertEquals(expectedDto, result);
        }

        @Test
        @DisplayName("리뷰 목록을 검색하면 조건에 맞는 목록이 조회되어야 한다.")
        void shouldReturnReviewList_whenGivenValidFilter(){

            // 테스트용
            ReviewSearchRequest request = new ReviewSearchRequest(
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    "더쿠감",
                    null, null, null, null,
                    50,
                    UUID.randomUUID()
            );

            Review expectedReview1 = mock(Review.class);
            Review expectedReview2 = mock(Review.class);
            List<Review> expectedResult = List.of(expectedReview1, expectedReview2);

            // 좋아요 Map 용
            UUID reviewId1 = UUID.randomUUID();
            UUID reviewId2 = UUID.randomUUID();
            when(expectedReview1.getId()).thenReturn(reviewId1);
            when(expectedReview2.getId()).thenReturn(reviewId2);

            // 리뷰 정보 동기화 용
            User mockUser = mock(User.class);
            when(mockUser.getNickname()).thenReturn("닉네임");
            when(expectedReview1.getUser()).thenReturn(mockUser);
            when(expectedReview2.getUser()).thenReturn(mockUser);

            when(expectedReview1.getUserNickName()).thenReturn("닉네임");
            when(expectedReview2.getUserNickName()).thenReturn("닉네임");

            Book mockBook = mock(Book.class);
            when(mockBook.getTitle()).thenReturn("책 제목");
            when(mockBook.getThumbnailUrl()).thenReturn("http://thumbnail.url");
            when(expectedReview1.getBook()).thenReturn(mockBook);
            when(expectedReview2.getBook()).thenReturn(mockBook);

            when(expectedReview1.getBookTitle()).thenReturn("책 제목");
            when(expectedReview2.getBookTitle()).thenReturn("책 제목");
            when(expectedReview1.getBookThumbnailUrl()).thenReturn("http://thumbnail.url");
            when(expectedReview2.getBookThumbnailUrl()).thenReturn("http://thumbnail.url");

            Pageable pageable = PageRequest.of(0, 50);
            Slice<Review> mockSlice = new SliceImpl<>(expectedResult, pageable, false);

            when(reviewRepository.findReviewsWithCursor(request, pageable)).thenReturn(mockSlice);
            when(reviewRepository.totalElementCount(request)).thenReturn(10L);

            // 좋아요 없음
            when(reviewLikeRepository.findByUserIdAndReviewIdIn(request.requestUserId(), List.of(reviewId1, reviewId2))).thenReturn(List.of());

            // List<ReviewDto> 생성 부분
            ReviewDto reviewDto1 = mock(ReviewDto.class);
            ReviewDto reviewDto2 = mock(ReviewDto.class);
            when(reviewMapper.toDto(expectedReview1, false)).thenReturn(reviewDto1);
            when(reviewMapper.toDto(expectedReview2, false)).thenReturn(reviewDto2);

            // When
            CursorPageResponseDto<ReviewDto> responseDto = basicReviewService.findReviews(request);

            // Then
            assertAll(
                    () -> assertEquals(2, responseDto.content().size()),
                    () -> assertEquals(50, responseDto.size()),
                    () -> assertEquals(10, responseDto.totalElement()),
                    () -> assertNull(responseDto.nextCursor()),
                    () -> assertNull(responseDto.nextAfter()),
                    () -> assertFalse(responseDto.hasNext())
            );

            // 호출 검증
            verify(reviewRepository).findReviewsWithCursor(request, pageable);
            verify(reviewRepository).totalElementCount(request);
            verify(reviewLikeRepository).findByUserIdAndReviewIdIn(request.requestUserId(), List.of(reviewId1, reviewId2));

            verify(reviewMapper).toDto(expectedReview1, false);
            verify(reviewMapper).toDto(expectedReview2, false);
        }
    }

    @Test
    @DisplayName("작성자는 본인의 리뷰를 수정할 수 있어야 한다.")
    void shouldUpdate_review_content(){
        // Given
        Review mockReview = mock(Review.class);
        User mockUser = mock(User.class);

        UUID requestUserId = UUID.randomUUID();
        UUID reviewId1 = UUID.randomUUID();
        ReviewUpdateRequest updateRequest = new ReviewUpdateRequest(
                "수정했습니다.",
                3
        );

        ReviewLike reviewLike = new ReviewLike(
                testReview,
                testUser,
                true
        );

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

        when(mockReview.getId()).thenReturn(reviewId1);
        when(mockReview.getUser()).thenReturn(mockUser);
        when(mockUser.getId()).thenReturn(requestUserId);

        when(reviewRepository.findById(reviewId1)).thenReturn(Optional.of(mockReview));

        when(reviewLikeRepository.findByUserIdAndReviewId(requestUserId, reviewId1)).thenReturn(Optional.of(reviewLike));
        when(reviewMapper.toDto(any(Review.class), anyBoolean())).thenReturn(expectedDto);

        // When
        basicReviewService.updateReview(mockReview.getId(), requestUserId, updateRequest);

        // Then
        verify(mockReview).updateReview("수정했습니다.", 3);
        verify(reviewRepository).save(mockReview);
    }

    @Test
    @DisplayName("작성자가 아닌 사람은 리뷰를 수정할 수 없다.")
    void cannotUpdateReview_whenUserNotAuthor(){
        // Given
        Review mockReview = mock(Review.class);
        User mockUser = mock(User.class);

        UUID requestUserId = UUID.randomUUID();
        UUID reviewId1 = UUID.randomUUID();
        UUID reviewerId = UUID.randomUUID();

        ReviewUpdateRequest updateRequest = new ReviewUpdateRequest(
                "수정 하고싶습니다",
                4
        );

        when(mockReview.getUser()).thenReturn(mockUser);
        when(mockUser.getId()).thenReturn(reviewerId);

        when(reviewRepository.findById(reviewId1)).thenReturn(Optional.of(mockReview));

        assertThrows(ReviewNotOwnedException.class, () -> {
            basicReviewService.updateReview(reviewId1, requestUserId, updateRequest);
        });

        // Then - 수정 메서드가 진짜 호출 안 됐는지?
        verify(mockReview, never()).updateReview(anyString(), anyInt());
        verify(reviewRepository, never()).save(any());
    }

    @Test
    @DisplayName("작성자는 리뷰를 논리삭제 할 수 있다.")
    void shouldSoftDelete_whenUserIsAuthor(){
        // Given
        User mockUser = mock(User.class);
        Book mockBook = mock(Book.class);

        UUID mockReviewId = UUID.randomUUID();
        UUID requestUserId = UUID.randomUUID();
        UUID mockBookId = UUID.randomUUID();

        Review deletedReview = new Review(mockBook, testUser, "논리 삭제된 리뷰", 5);
        Review spyReview = spy(deletedReview); // 실제 동작하는 spy 객체

        when(reviewRepository.findById(mockReviewId)).thenReturn(Optional.of(spyReview));

        when(spyReview.getBook()).thenReturn(mockBook);
        when(mockBook.getId()).thenReturn(mockBookId);

        when(bookRepository.findById(mockBookId)).thenReturn(Optional.of(mockBook));

        when(spyReview.getUser()).thenReturn(mockUser);
        when(mockUser.getId()).thenReturn(requestUserId);

        // When
        basicReviewService.deleteReviewSoft(mockReviewId, requestUserId);

        // Then
        assertTrue(spyReview.isDeleted());
        verify(bookRepository).updateBookReviewStats(mockBookId);
        verify(reviewRepository).save(spyReview);
        verify(bookRepository).save(mockBook);

        // mock으로 만든 Review는 가짜 객체라서 내부 필드를 변경하지 않음
    }

    @Test
    @DisplayName("작성자가 아닌 사람은 리뷰를 논리삭제 할 수 없다.")
    void cannotSoftDelete_whenUserIsNotAuthor() {
        // Given
        Review mockReview = mock(Review.class);
        User mockUser = mock(User.class);
        Book mockBook = mock(Book.class);

        UUID mockReviewId = UUID.randomUUID();
        UUID mockUserId = UUID.randomUUID();
        UUID requestUserId = UUID.randomUUID();
        UUID mockBookId = UUID.randomUUID();

        when(reviewRepository.findById(mockReviewId)).thenReturn(Optional.of(mockReview));
        when(mockReview.getUser()).thenReturn(mockUser);
        when(mockUser.getId()).thenReturn(mockUserId);

        when(mockReview.getBook()).thenReturn(mockBook);
        when(mockBook.getId()).thenReturn(mockBookId);

        when(bookRepository.findById(mockBookId)).thenReturn(Optional.of(mockBook));

        assertThrows(ReviewNotOwnedException.class, () -> {
            basicReviewService.deleteReviewSoft(mockReviewId, requestUserId);
        });

        verify(mockReview, never()).updateIsDelete(true);
        verify(bookRepository, never()).updateBookReviewStats(mockBookId);
        verify(reviewRepository, never()).save(mockReview);
        verify(bookRepository, never()).save(mockBook);
    }


    @Test
    @DisplayName("작성자는 리뷰를 물리 삭제 할 수 있다.")
    void shouldHardDelete_whenUserIsAuthor(){
        // Given
        UUID mockReviewId = UUID.randomUUID();
        UUID requestUserId = UUID.randomUUID();
        UUID mockBookId = UUID.randomUUID();

        User mockUser = mock(User.class);
        when(mockUser.getId()).thenReturn(requestUserId);

        Book mockBook = mock(Book.class);
        when(mockBook.getId()).thenReturn(mockBookId);

        Review review = mock(Review.class);
        when(review.getBook()).thenReturn(mockBook);
        when(review.getUser()).thenReturn(mockUser);

        when(reviewRepository.findById(mockReviewId)).thenReturn(Optional.of(review));
        when(bookRepository.findById(mockBookId)).thenReturn(Optional.of(mockBook));

        // When
        basicReviewService.deleteReviewHard(mockReviewId, requestUserId);

        // Then
        verify(bookRepository).updateBookReviewStats(mockBookId);
        verify(reviewRepository).delete(review);
        verify(bookRepository).save(mockBook);

    }

    @Test
    @DisplayName("작성자가 아닌 사람은 리뷰를 물리 삭제 할 수 없다.")
    void cannotHardDelete_whenUserIsNotAuthor() {
        // Given
        UUID mockReviewId = UUID.randomUUID();
        UUID mockUserId = UUID.randomUUID();
        UUID requestUserId = UUID.randomUUID();
        UUID mockBookId = UUID.randomUUID();

        User mockUser = mock(User.class);
        when(mockUser.getId()).thenReturn(mockUserId);

        Book mockBook = mock(Book.class);
        when(mockBook.getId()).thenReturn(mockBookId);

        Review mockReview = mock(Review.class);
        when(mockReview.getUser()).thenReturn(mockUser);
        when(mockReview.getBook()).thenReturn(mockBook);

        when(reviewRepository.findById(mockReviewId)).thenReturn(Optional.of(mockReview));
        when(bookRepository.findById(mockBookId)).thenReturn(Optional.of(mockBook));

        assertThrows(ReviewNotOwnedException.class, () -> {
            basicReviewService.deleteReviewHard(mockReviewId, requestUserId);
        });

        verify(bookRepository, never()).updateBookReviewStats(mockBookId);
        verify(reviewRepository, never()).delete(mockReview);
        verify(bookRepository, never()).save(mockBook);
    }

    @Test
    @DisplayName("리뷰 좋아요를 취소할 수 있어야 한다.")
    void shouldUpdate_ReviewLike_Unlike(){
        // Given
        testReview.updateLikeCount(3L);  // 초기 좋아요 개수

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

    @Test
    @DisplayName("리뷰에 좋아요 정보가 없는 경우 생성되어야 한다.")
    void shouldCreate_ReviewLike_whenIsEmpty(){
        testReview.updateLikeCount(3L);

        when(reviewLikeRepository.findByUserIdAndReviewId(userId, reviewId)).thenReturn(Optional.empty());
        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(testReview));
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

        ReviewLike reviewLike = new ReviewLike(
                testReview,
                testUser,
                true
        );

        ReviewLikeDto expectedDto = new ReviewLikeDto(
                reviewId,
                userId,
                true
        );

        when(reviewLikeMapper.toDto(any(ReviewLike.class))).thenReturn(expectedDto);

        ReviewLikeDto result = basicReviewService.reviewLike(reviewId, userId);

        // Then
        assertEquals(true, result.liked());
        assertEquals(4L, testReview.getLikeCount());
        verify(reviewLikeRepository).save(any(ReviewLike.class));
        verify(reviewRepository).save(any(Review.class));
        verify(reviewLikeMapper).toDto(any(ReviewLike.class));
    }

}
