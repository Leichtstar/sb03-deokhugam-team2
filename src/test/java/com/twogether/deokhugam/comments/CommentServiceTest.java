package com.twogether.deokhugam.comments;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.twogether.deokhugam.comments.dto.CommentCreateRequest;
import com.twogether.deokhugam.comments.entity.Comment;
import com.twogether.deokhugam.comments.mapper.CommentMapper;
import com.twogether.deokhugam.comments.repository.CommentRepository;
import com.twogether.deokhugam.comments.service.CommentService;
import com.twogether.deokhugam.review.entity.Review;
import com.twogether.deokhugam.user.entity.User;
import jakarta.validation.Validator;

import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

@ExtendWith(MockitoExtension.class)
@DisplayName("댓글 서비스 테스트")
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private CommentMapper commentMapper;

    private Validator validator;

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
            .anyMatch(violation -> violation.getMessage().equals("내용은 200자를 초과할 수 없습니다.")));
    }

    @Test
    @DisplayName("댓글 엔티티가 정상적으로 생성된다")
    void comment_EntityCreation() {
        // given
        User mockUser = mock(User.class);
        Review mockReview = mock(Review.class);

        // when
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
        Comment mockComment = new Comment(mock(User.class), mock(Review.class), "테스트 댓글입니다.");
        when(commentMapper.toEntity(any(CommentCreateRequest.class))).thenReturn(mockComment);
        when(commentRepository.save(any(Comment.class))).thenReturn(mockComment);

        // when
        commentService.createComment(commentCreateRequest);

        // then
        verify(commentMapper, times(1)).toEntity(any(CommentCreateRequest.class));
        verify(commentRepository, times(1)).save(any(Comment.class));
        // 필요하면 반환값까지 assertThat 등으로 검증
    }
}