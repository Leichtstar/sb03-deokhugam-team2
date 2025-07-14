package com.twogether.deokhugam.comments.repository;

import com.twogether.deokhugam.comments.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/**
 * 댓글 영속성을 담당하는 JPA 레포지토리.
 *
 * <p>향후 사용자‧리뷰 연관 관계가 완성되면
 * 메서드 시그니처( findByReviewId … )를 추가해 주세요.
 */
public interface CommentRepository extends JpaRepository<Comment, UUID> {
}