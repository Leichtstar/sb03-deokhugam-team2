package com.twogether.deokhugam.comments.service;

import com.twogether.deokhugam.comments.dto.CommentCreateRequest;
import com.twogether.deokhugam.comments.dto.CommentResponse;
import com.twogether.deokhugam.comments.dto.CommentUpdateRequest;
import com.twogether.deokhugam.comments.entity.Comment;
import com.twogether.deokhugam.comments.exception.CommentAlreadyDeletedException;
import com.twogether.deokhugam.comments.exception.CommentForbiddenException;
import com.twogether.deokhugam.comments.exception.CommentNotFoundException;
import com.twogether.deokhugam.comments.mapper.CommentMapper;
import com.twogether.deokhugam.comments.repository.CommentRepository;
import com.twogether.deokhugam.notification.event.CommentCreatedEvent;
import com.twogether.deokhugam.review.entity.Review;
import com.twogether.deokhugam.review.exception.ReviewNotFoundException;
import com.twogether.deokhugam.review.repository.ReviewRepository;
import com.twogether.deokhugam.user.entity.User;
import com.twogether.deokhugam.user.exception.UserNotFoundException;
import com.twogether.deokhugam.user.repository.UserRepository;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

/**
 * 댓글 도메인의 비즈니스 로직을 담당.
 */
@Service
@RequiredArgsConstructor
@Validated
public class CommentService {

    private static final Logger log = LoggerFactory.getLogger(CommentService.class);

    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    // 알림용
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 댓글 등록.
     */
    @Transactional
    public CommentResponse createComment(@Valid CommentCreateRequest request) {
        log.debug("댓글 생성 요청: reviewId={}, userId={}", request.reviewId(), request.userId());

        Review review = findReviewOrThrow(request.reviewId());
        User user = findUserOrThrow(request.userId());
      
        Comment entity = new Comment(user, review, request.content());
        Comment saved = commentRepository.save(entity);

        // 알림 이벤트 발행
        eventPublisher.publishEvent(
            new CommentCreatedEvent(user, review, saved.getContent())
        );

        reviewRepository.incrementCommentCount(request.reviewId());
        review.updateUpdatedAt();
        reviewRepository.save(review);

        log.debug("댓글 생성 완료: commentId={}", saved.getId());

        return commentMapper.toResponse(saved);
    }

    /**
     * 댓글 상세 조회.
     */
    @Transactional(readOnly = true)
    public CommentResponse getComment(UUID id) {
        log.debug("댓글 조회 요청: commentId={}", id);
        Comment comment = findActiveCommentByIdOrThrow(id);
        return commentMapper.toResponse(comment);
    }

    /**
     * 댓글 내용 수정.
     */
    @Transactional
    public CommentResponse updateComment(UUID commentId, UUID userId, CommentUpdateRequest request) {
        log.debug("댓글 수정 요청: commentId={}, userId={}", commentId, userId);

        Comment comment = findActiveCommentByIdOrThrow(commentId);
        validateCommentOwner(comment, userId);

        comment.editContent(request.content());
        log.debug("댓글 수정 완료: commentId={}", commentId);

        return commentMapper.toResponse(comment);
    }

    /**
     * 댓글 논리적 삭제.
     */
    @Transactional
    public void deleteLogical(UUID commentId, UUID requestUserId) {
        log.debug("댓글 논리적 삭제 요청: commentId={}, userId={}", commentId, requestUserId);

        Comment comment = findCommentByIdOrThrow(commentId);
        validateCommentOwner(comment, requestUserId);
        validateNotDeleted(comment);

        reviewRepository.decrementCommentCount(comment.getReview().getId());
        commentRepository.logicalDeleteById(commentId);

        log.debug("댓글 논리적 삭제 완료: commentId={}", commentId);
    }

    /**
     * 댓글 물리적 삭제.
     */
    @Transactional
    public void deletePhysical(UUID commentId, UUID requestUserId) {
        log.debug("댓글 물리적 삭제 요청: commentId={}, userId={}", commentId, requestUserId);

        Comment comment = findCommentByIdOrThrow(commentId);
        validateCommentOwner(comment, requestUserId);

        if (!comment.getIsDeleted()) {
            decrementReviewCommentCount(comment.getReview());
        }

        commentRepository.deleteById(commentId);
        log.debug("댓글 물리적 삭제 완료: commentId={}", commentId);
    }

    // ================= 헬퍼 메소드 =================

    private Review findReviewOrThrow(UUID reviewId) {
        return reviewRepository.findById(reviewId)
            .orElseThrow(() -> new ReviewNotFoundException(reviewId));
    }

    private User findUserOrThrow(UUID userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException());
    }

    private Comment findCommentByIdOrThrow(UUID commentId) {
        return commentRepository.findById(commentId)
            .orElseThrow(CommentNotFoundException::new);
    }

    private Comment findActiveCommentByIdOrThrow(UUID commentId) {
        return commentRepository.findById(commentId)
            .filter(c -> !c.getIsDeleted())
            .orElseThrow(CommentNotFoundException::new);
    }

    private void validateCommentOwner(Comment comment, UUID userId) {
        if (!comment.getUser().getId().equals(userId)) {
            throw new CommentForbiddenException();
        }
    }

    private void validateNotDeleted(Comment comment) {
        if (Boolean.TRUE.equals(comment.getIsDeleted())) {
            throw new CommentAlreadyDeletedException("이미 삭제된 댓글입니다.");
        }
    }

    private void incrementReviewCommentCount(Review review) {
        reviewRepository.incrementCommentCount(review.getId());
    }

    private void decrementReviewCommentCount(Review review) {
        reviewRepository.decrementCommentCount(review.getId());
    }
}