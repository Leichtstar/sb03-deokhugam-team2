package com.twogether.deokhugam.review.service;

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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class BasicReviewService implements ReviewService{

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final ReviewMapper reviewMapper;

    // 리뷰 생성
    @Override
    @Transactional
    public ReviewDto create(ReviewCreateRequest request) {
        // 이미 존재하는 리뷰 시 예외
        if (reviewRepository.existsByUserIdAndBookId(request.userId(), request.bookId())){
            throw new ReviewExistException(request.userId(), request.bookId());
        }

        // 리뷰 작성하려는 책, 유저
        Book reviewdBook = bookRepository.findById(request.bookId())
                .orElseThrow(
                        () -> new NoSuchElementException("책을 찾을 수 없습니다. " + request.bookId()));

        User reviewer = userRepository.findById(request.userId())
                .orElseThrow(
                        () -> new NoSuchElementException("사용자를 찾을 수 없습니다. " + request.userId()));

        Review review = new Review(reviewdBook, reviewer, request.content(), request.rating());
        reviewRepository.save(review);

        log.info("[BasicReviewService] 리뷰 등록 성공");

        return reviewMapper.toDto(review);
    }

}
