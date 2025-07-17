package com.twogether.deokhugam.comments.service;

import com.twogether.deokhugam.comments.dto.CommentCreateRequest;
import com.twogether.deokhugam.comments.dto.CommentResponse;
import com.twogether.deokhugam.comments.entity.Comment;
import com.twogether.deokhugam.comments.exception.CommentNotFoundException;
import com.twogether.deokhugam.comments.mapper.CommentMapper;
import com.twogether.deokhugam.comments.repository.CommentRepository;
import com.twogether.deokhugam.review.entity.Review;
import com.twogether.deokhugam.review.exception.ReviewNotFoundException;
import com.twogether.deokhugam.review.repository.ReviewRepository;
import com.twogether.deokhugam.user.entity.User;
import com.twogether.deokhugam.user.exception.UserNotFoundException;
import com.twogether.deokhugam.user.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.NoSuchElementException;
import java.util.UUID;

/**
 * 댓글 도메인의 비즈니스 로직을 담당.
 */
@Service
@RequiredArgsConstructor
@Validated
public class CommentService {

    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;

    /**
     * 댓글 등록.
     *
     * @param request 등록 요청 DTO
     * @return 등록 결과 DTO
     */
    public CommentResponse createComment(@Valid CommentCreateRequest request) {
        Review review = reviewRepository.findById(request.reviewId())
            .orElseThrow(() -> new ReviewNotFoundException(request.reviewId()));
        User user = userRepository.findById(request.userId())
            .orElseThrow(() -> new UserNotFoundException());
        Comment entity = new Comment(user, review, request.content());
        Comment saved = commentRepository.save(entity);
        return commentMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public CommentResponse getComment(UUID id) {
        Comment comment = commentRepository.findById(id)
            .filter(c -> !c.getIsDeleted())
            .orElseThrow(CommentNotFoundException::new);
        return commentMapper.toResponse(comment);
    }
}
