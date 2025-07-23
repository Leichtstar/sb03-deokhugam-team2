package com.twogether.deokhugam.review.mapper;

import com.twogether.deokhugam.book.entity.Book;
import com.twogether.deokhugam.review.dto.ReviewDto;
import com.twogether.deokhugam.review.entity.Review;
import com.twogether.deokhugam.review.repository.ReviewRepository;
import com.twogether.deokhugam.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class ReviewMapper {

    private final ReviewRepository reviewRepository;

    public ReviewDto toDto(Review review, boolean likeByMe){
        if (review == null){
            return null;
        }

        Book book = review.getBook();
        User user = review.getUser();

        if (book == null || user == null){
            throw new IllegalStateException("Review가 null 값인 도서나 사용자를 참조하고 있습니다.");
        }

        return new ReviewDto(
                review.getId(),
                book.getId(),
                review.getBookTitle(),
                review.getBookThumbnailUrl(),
                user.getId(),
                review.getUserNickName(),
                review.getContent(),
                review.getRating(),
                review.getLikeCount(),
                review.getCommentCount(),
                likeByMe,
                review.getCreatedAt(),
                review.getUpdatedAt()
        );
    }
}
