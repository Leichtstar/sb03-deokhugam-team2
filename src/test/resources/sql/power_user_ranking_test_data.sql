-- 유저 등록
INSERT INTO users (id, email, password, nickname, created_at, updated_at, is_deleted)
VALUES
    ('11111111-1111-1111-1111-111111111111', 'test1@email.com', 'pw1', '사용자1', NOW(), NOW(), false),
    ('22222222-2222-2222-2222-222222222222', 'test2@email.com', 'pw2', '사용자2', NOW(), NOW(), false);

-- 도서 등록 (리뷰에 FK 필요)
INSERT INTO books (id, title, author, description, publisher, published_date, isbn, thumbnail_url, review_count, rating, created_at, updated_at, is_deleted)
VALUES
    ('00000000-0000-0000-0000-000000000001', '테스트 도서', '저자1', '설명1', '출판사1', '2023-01-01', '1234567890123', 'http://example.com/image.jpg', 2, 3.5, NOW(), NOW(), false);

-- 리뷰 등록
INSERT INTO reviews (id, book_id, user_id, book_title, book_thumbnail_url, user_nickname, content, rating, like_count, comment_count, created_at, updated_at, is_deleted)
VALUES
    ('aaaa1111-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000001', '11111111-1111-1111-1111-111111111111',
     '테스트 도서', 'http://example.com/image.jpg', '사용자1', '리뷰 내용 1', 4, 1, 1, NOW(), NOW(), false),

    ('aaaa1111-0000-0000-0000-000000000002', '00000000-0000-0000-0000-000000000001', '22222222-2222-2222-2222-222222222222',
     '테스트 도서', 'http://example.com/image.jpg', '사용자2', '리뷰 내용 2', 3, 1, 1, NOW(), NOW(), false);

-- 좋아요 등록
INSERT INTO review_like (review_id, user_id, liked)
VALUES
    ('aaaa1111-0000-0000-0000-000000000001', '22222222-2222-2222-2222-222222222222', true),
    ('aaaa1111-0000-0000-0000-000000000002', '11111111-1111-1111-1111-111111111111', true);

-- 댓글 등록
INSERT INTO comments (id, review_id, user_id, content, created_at, updated_at, is_deleted)
VALUES
    ('00000000-0000-0000-0000-000000000001', 'aaaa1111-0000-0000-0000-000000000001', '22222222-2222-2222-2222-222222222222', '댓글1', NOW(), NOW(), false),
    ('00000000-0000-0000-0000-000000000002', 'aaaa1111-0000-0000-0000-000000000002', '11111111-1111-1111-1111-111111111111', '댓글2', NOW(), NOW(), false);
