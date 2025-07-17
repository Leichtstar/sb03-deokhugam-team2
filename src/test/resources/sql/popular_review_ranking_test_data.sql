-- books
INSERT INTO books (id, title, author, publisher, published_date, review_count, rating, created_at, is_deleted, description)
VALUES ('11111111-1111-1111-1111-111111111111', '테스트책', '작가', '출판사', '2024-01-01', 1, 4.5, '2025-07-16 00:00:00', false, '테스트 책 설명');

-- users
INSERT INTO users (id, email, nickname, password, created_at, is_deleted)
VALUES ('22222222-2222-2222-2222-000000000001', 'user1@example.com', '테스터1', 'pw1234', '2025-07-16 00:00:00', false);

-- 리뷰
INSERT INTO reviews (
    id, book_id, user_id, book_title, book_thumbnail_url, user_nickname,
    content, rating, like_count, comment_count, created_at, updated_at, is_deleted
)
VALUES (
           '33333333-3333-3333-3333-333333333333',
           '11111111-1111-1111-1111-111111111111',
           '22222222-2222-2222-2222-000000000001',
           '테스트책', 'http://example.com/img.jpg', '테스터1',
           '훌륭한 책입니다', 4.5, 5, 15,
           '2025-07-17 09:00:00', '2025-07-17 09:00:00', false
       );