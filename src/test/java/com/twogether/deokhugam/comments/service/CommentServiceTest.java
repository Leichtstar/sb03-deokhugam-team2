package com.twogether.deokhugam.comments.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import com.twogether.deokhugam.review.repository.ReviewRepository;
import com.twogether.deokhugam.user.entity.User;
import com.twogether.deokhugam.user.repository.UserRepository;
import jakarta.validation.Validator;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

@ExtendWith(MockitoExtension.class)
@DisplayName("댓글 서비스 테스트")
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private CommentMapper commentMapper;

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private UserRepository userRepository;

    private Validator validator;

    // 알림 이벤트
    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private CommentService commentService;

    private UUID userId;
    private UUID reviewId;
    private CommentCreateRequest commentCreateRequest;

    @BeforeEach
    void setup() {
        // Validator 설정
        LocalValidatorFactoryBean validatorFactoryBean = new LocalValidatorFactoryBean();
        validatorFactoryBean.afterPropertiesSet();
        validator = validatorFactoryBean.getValidator();

        // Mock 객체 주입 확인
        assertNotNull(commentRepository);
        assertNotNull(commentMapper);
        assertNotNull(commentService);

        userId = UUID.randomUUID();
        reviewId = UUID.randomUUID();

        commentCreateRequest = new CommentCreateRequest(
            userId,
            reviewId,
            "테스트 댓글입니다."
        );
    }

    @Test
    @DisplayName("댓글 내용이 없는 경우 예외가 발생한다")
    void createComment_EmptyContent() {
        // given
        CommentCreateRequest invalidRequest = new CommentCreateRequest(
            userId,
            reviewId,
            ""
        );

        // when & then
        var violations = validator.validate(invalidRequest);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
            .anyMatch(violation -> violation.getMessage().equals("내용은 비어 있을 수 없습니다.")));
    }

    @Test
    @DisplayName("댓글 내용이 200자를 초과하는 경우 예외가 발생한다")
    void createComment_ContentTooLong() {
        // given
        String tooLongContent = "a".repeat(201);
        CommentCreateRequest invalidRequest = new CommentCreateRequest(
            userId,
            reviewId,
            tooLongContent
        );

        // when & then
        var violations = validator.validate(invalidRequest);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
            .anyMatch(violation -> violation.getMessage().equals("댓글은 200자를 초과할 수 없습니다.")));
    }

    @Test
    @DisplayName("댓글 엔티티가 정상적으로 생성된다")
    void comment_EntityCreation() {
        // given
        User mockUser = mock(User.class);
        Review mockReview = mock(Review.class);

        Comment comment = new Comment(mockUser, mockReview, "테스트 댓글입니다.");

        // then
        assertThat(comment.getContent()).isEqualTo("테스트 댓글입니다.");
        assertThat(comment.getIsDeleted()).isFalse();
        assertThat(comment.getUser()).isEqualTo(mockUser);
        assertThat(comment.getReview()).isEqualTo(mockReview);
    }

    @Test
    @DisplayName("댓글이 정상적으로 삭제 처리된다")
    void comment_Deletion() {
        // given
        User mockUser = mock(User.class);
        Review mockReview = mock(Review.class);
        Comment comment = new Comment(mockUser, mockReview, "테스트 댓글입니다.");

        // when
        comment.delete();

        // then
        assertThat(comment.getIsDeleted()).isTrue();
    }

    @Test
    @DisplayName("정상적으로 댓글 등록에 성공한다")
    void createComment_success() {
        // given
        User mockUser = mock(User.class);
        Review mockReview = mock(Review.class);
        when(mockReview.getId()).thenReturn(reviewId);

        Comment mockComment = new Comment(mockUser, mockReview, "테스트 댓글입니다.");
        CommentResponse expectedResponse = mock(CommentResponse.class);

        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(mockReview));
        when(userRepository.findById(any())).thenReturn(Optional.of(mockUser));
        when(commentRepository.save(any(Comment.class))).thenReturn(mockComment);

        when(commentMapper.toResponse(any(Comment.class))).thenReturn(expectedResponse);

        // when
        CommentResponse actualResponse = commentService.createComment(commentCreateRequest);

        // then
        assertThat(actualResponse).isEqualTo(expectedResponse);
        verify(reviewRepository, times(1)).findById(any());
        verify(userRepository, times(1)).findById(any());
        verify(commentRepository, times(1)).save(any(Comment.class));
        verify(reviewRepository, times(1)).incrementCommentCount(mockReview.getId());
        verify(commentMapper, times(1)).toResponse(any(Comment.class));
        verify(eventPublisher).publishEvent(any(CommentCreatedEvent.class));
    }



    @Test
    @DisplayName("정상적으로 댓글을 상세 조회한다")
    void getComment_success() throws Exception {
        UUID id = UUID.randomUUID();
        String content = "테스트";
        UUID userId = UUID.randomUUID();
        String userNickname = "user1";
        UUID reviewId = UUID.randomUUID();
        Instant createdAt = Instant.now();
        Instant updatedAt = Instant.now();
        Boolean isDeleted = false;

        Comment comment = mock(Comment.class);
        when(comment.getIsDeleted()).thenReturn(false);
        when(commentRepository.findById(id)).thenReturn(Optional.of(comment));

        CommentResponse dto = new CommentResponse(
            id,             // UUID id
            content,        // String content
            userId,         // UUID userId
            userNickname,   // String userNickname
            reviewId,       // UUID reviewId
            createdAt,      // Instant createdAt
            updatedAt,      // Instant updatedAt
            isDeleted       // Boolean isDeleted
        );

        when(commentMapper.toResponse(comment)).thenReturn(dto);

        CommentResponse result = commentService.getComment(id);

        assertThat(result).isEqualTo(dto);
        verify(commentRepository).findById(id);
        verify(commentMapper).toResponse(comment);
    }

    @Test
    @DisplayName("삭제된 댓글 조회 시 예외를 던진다")
    void getComment_deleted() {
        UUID id = UUID.randomUUID();
        Comment comment = mock(Comment.class);
        when(comment.getIsDeleted()).thenReturn(true);
        when(commentRepository.findById(id)).thenReturn(Optional.of(comment));

        assertThatThrownBy(() -> commentService.getComment(id))
            .isInstanceOf(CommentNotFoundException.class);
    }

    @Test
    @DisplayName("존재하지 않는 댓글 조회 시 예외를 던진다")
    void getComment_notFound() {
        UUID id = UUID.randomUUID();
        when(commentRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commentService.getComment(id))
            .isInstanceOf(CommentNotFoundException.class);
    }

    @Test
    @DisplayName("댓글을 정상적으로 수정한다")
    void updateComment_success() {
        UUID commentId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        User user = mock(User.class);

        Comment comment = mock(Comment.class);
        CommentUpdateRequest request = new CommentUpdateRequest("수정된 내용");
        CommentResponse expectedResponse = mock(CommentResponse.class);

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));
        when(comment.getIsDeleted()).thenReturn(false);
        when(comment.getUser()).thenReturn(user);
        when(comment.getUser().getId()).thenReturn(userId);
        when(commentMapper.toResponse(comment)).thenReturn(expectedResponse);

        CommentResponse result = commentService.updateComment(commentId, userId, request);

        verify(comment).editContent("수정된 내용");
        assertThat(result).isEqualTo(expectedResponse);
    }

    @Test
    @DisplayName("삭제된 댓글을 수정 시 예외 발생")
    void updateComment_deleted() {
        UUID commentId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Comment comment = mock(Comment.class);

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));
        when(comment.getIsDeleted()).thenReturn(true);

        assertThatThrownBy(() -> commentService.updateComment(commentId, userId, new CommentUpdateRequest("수정")))
            .isInstanceOf(CommentNotFoundException.class);
    }

    @Test
    @DisplayName("존재하지 않는 댓글을 수정 시 예외 발생")
    void updateComment_notFound() {
        UUID commentId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(commentRepository.findById(commentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commentService.updateComment(commentId, userId, new CommentUpdateRequest("수정")))
            .isInstanceOf(CommentNotFoundException.class);
    }

    @Test
    @DisplayName("타인이 쓴 댓글을 수정 시 권한 예외 발생")
    void updateComment_forbidden() {
        UUID commentId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID anotherUserId = UUID.randomUUID();
        Comment comment = mock(Comment.class);
        User anotherUser = mock(User.class);

        when(anotherUser.getId()).thenReturn(anotherUserId);
        when(comment.getIsDeleted()).thenReturn(false);
        when(comment.getUser()).thenReturn(anotherUser);
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

        assertThatThrownBy(() -> commentService.updateComment(commentId, userId, new CommentUpdateRequest("수정")))
            .isInstanceOf(CommentForbiddenException.class);
    }

    @Test
    @DisplayName("댓글 논리삭제에 성공한다")
    void deleteLogical_success() {
        UUID commentId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID reviewId = UUID.randomUUID();

        User mockUser = mock(User.class);
        when(mockUser.getId()).thenReturn(userId);

        Review mockReview = mock(Review.class);
        when(mockReview.getId()).thenReturn(reviewId);

        Comment mockComment = mock(Comment.class);
        when(mockComment.getUser()).thenReturn(mockUser);
        when(mockComment.getReview()).thenReturn(mockReview);

        when(mockComment.getIsDeleted()).thenReturn(false);

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(mockComment));

        // when
        commentService.deleteLogical(commentId, userId);

        // then
        verify(reviewRepository).decrementCommentCount(reviewId);
        verify(commentRepository).logicalDeleteById(commentId);
    }

    @Test
    @DisplayName("논리삭제 - 존재하지 않는 댓글이면 예외 발생")
    void deleteLogical_notFound() {
        UUID commentId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        when(commentRepository.findById(commentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commentService.deleteLogical(commentId, userId))
            .isInstanceOf(CommentNotFoundException.class);
    }


    @Test
    @DisplayName("논리삭제 - 타인 댓글이면 권한 예외 발생")
    void deleteLogical_forbidden() {
        UUID commentId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        UUID attackerId = UUID.randomUUID();
        User mockUser = mock(User.class);
        when(mockUser.getId()).thenReturn(ownerId);

        Comment mockComment = mock(Comment.class);
        when(mockComment.getUser()).thenReturn(mockUser);

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(mockComment));

        assertThatThrownBy(() -> commentService.deleteLogical(commentId, attackerId))
                .isInstanceOf(CommentForbiddenException.class);
    }


    @Test
    @DisplayName("논리삭제 - 이미 삭제된 댓글이면 예외 발생")
    void deleteLogical_alreadyDeleted() {
        UUID commentId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        User mockUser = mock(User.class);
        when(mockUser.getId()).thenReturn(userId);

        Comment mockComment = mock(Comment.class);
        when(mockComment.getUser()).thenReturn(mockUser);
        when(mockComment.getIsDeleted()).thenReturn(true);

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(mockComment));

        assertThatThrownBy(() -> commentService.deleteLogical(commentId, userId))
            .isInstanceOf(CommentAlreadyDeletedException.class)
            .hasMessageContaining("이미 삭제된 댓글입니다");
    }

    @Test
    @DisplayName("댓글 물리삭제에 성공한다")
    void deletePhysical_success() {
        UUID commentId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID reviewId = UUID.randomUUID();

        User mockUser = mock(User.class);
        when(mockUser.getId()).thenReturn(userId);

        Review mockReview = mock(Review.class);
        when(mockReview.getId()).thenReturn(reviewId);

        Comment mockComment = mock(Comment.class);
        when(mockComment.getUser()).thenReturn(mockUser);
        when(mockComment.getReview()).thenReturn(mockReview);

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(mockComment));
        when(mockComment.getIsDeleted()).thenReturn(false);

        commentService.deletePhysical(commentId, userId);

        verify(reviewRepository).decrementCommentCount(reviewId);
        verify(commentRepository).deleteById(commentId);
    }

    @Test
    @DisplayName("댓글이 논리삭제 되어 있다면 comment count 감소 함수를 호출하지 않아야 한다.")
    void shouldNotCall_decrementCommentCount_whenAlreadyLogicalDeleted() {
        UUID commentId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID reviewId = UUID.randomUUID();

        User mockUser = mock(User.class);
        when(mockUser.getId()).thenReturn(userId);

        Comment mockComment = mock(Comment.class);
        when(mockComment.getUser()).thenReturn(mockUser);

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(mockComment));
        when(mockComment.getIsDeleted()).thenReturn(true);

        commentService.deletePhysical(commentId, userId);

        verify(reviewRepository, never()).decrementCommentCount(reviewId);
        verify(commentRepository).deleteById(commentId);
    }

    @Test
    @DisplayName("물리삭제 - 존재하지 않는 댓글이면 예외 발생")
    void deletePhysical_notFound() {
        UUID commentId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        when(commentRepository.findById(commentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commentService.deletePhysical(commentId, userId))
            .isInstanceOf(CommentNotFoundException.class);
    }

    @Test
    @DisplayName("물리삭제 - 타인 댓글이면 권한 예외 발생")
    void deletePhysical_forbidden() {
        UUID commentId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        UUID attackerId = UUID.randomUUID();
        User mockUser = mock(User.class);
        when(mockUser.getId()).thenReturn(ownerId);

        Comment mockComment = mock(Comment.class);
        when(mockComment.getUser()).thenReturn(mockUser);

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(mockComment));

        assertThatThrownBy(() -> commentService.deletePhysical(commentId, attackerId))
            .isInstanceOf(CommentForbiddenException.class);
    }
}