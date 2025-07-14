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
import com.twogether.deokhugam.review.dto.request.ReviewCreateRequest;
import com.twogether.deokhugam.review.entity.Review;
import com.twogether.deokhugam.review.exception.ReviewExistException;
import com.twogether.deokhugam.review.mapper.ReviewMapper;
import com.twogether.deokhugam.review.repository.ReviewRepository;
import com.twogether.deokhugam.user.entity.User;
import com.twogether.deokhugam.user.repository.UserRepository;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
    private ReviewMapper reviewMapper;

   @InjectMocks
   private BasicReviewService basicReviewService;

    private UUID bookId;
    private UUID userId;
    private ReviewCreateRequest reviewCreateRequest;

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

        reviewCreateRequest = new ReviewCreateRequest(
                bookId, userId, "재밌는 책이다.", 4
        );
    }

    @Test
    @DisplayName("유효한 요청으로 리뷰를 생성할 수 있다.")
    void create_Review() {
        // Mock 객체
        Book mockBook = mock(Book.class);
        User mockUser = mock(User.class);
        ReviewDto expectedDto = mock(ReviewDto.class);

        Review review = new Review(mockBook, mockUser, "재밌는 책이다.", 4);

        when(reviewRepository.existsByUserIdAndBookId(userId, bookId)).thenReturn(false);
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(mockBook));
        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        when(reviewRepository.save(any(Review.class))).thenReturn(review);
        when(reviewMapper.toDto(any(Review.class))).thenReturn(expectedDto);

        // When
        ReviewDto result = basicReviewService.create(reviewCreateRequest);

        // Then
        assertEquals(expectedDto, result);
        verify(reviewRepository).save(any(Review.class));
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
}
