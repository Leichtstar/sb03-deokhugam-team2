-- 유저
INSERT INTO users (
    id, email, nickname, password, created_at, updated_at, is_deleted
)
VALUES (
           '22222222-2222-2222-2222-000000000001',
           'user1@example.com',
           '일간유저',
           'pw1234',
           TIMESTAMP '2025-07-22 00:00:00',
           TIMESTAMP '2025-07-22 00:00:00',
           FALSE
       );

-- 도서
INSERT INTO books (
    id, title, author, publisher, published_date, review_count, rating, created_at, updated_at, is_deleted, description
)
VALUES (
           '11111111-1111-1111-1111-000000000001',
           '일간 책',
           '작가A',
           '출판사A',
           DATE '2024-01-01',
           1,
           4,
           TIMESTAMP '2025-07-22 00:00:00',
           TIMESTAMP '2025-07-22 00:00:00',
           FALSE,
           '일간용 도서'
       );

-- 리뷰
INSERT INTO reviews (
    id, book_id, user_id, rating, content, created_at, updated_at, is_deleted,
    user_nickname, book_title, book_thumbnail_url, like_count, comment_count
)
VALUES (
           '33333333-3333-3333-3333-000000000001',
           '11111111-1111-1111-1111-000000000001',
           '22222222-2222-2222-2222-000000000001',
           4,
           '일간 리뷰',
           TIMESTAMP '2025-07-22 09:00:00',
           TIMESTAMP '2025-07-22 09:00:00',
           FALSE,
           '일간유저',
           '일간 책',
           'http://image.com/1.jpg',
           5,
           2
       );

-- 삭제 대상 알림 (updated_at이 8일 전)
INSERT INTO notifications (
    id, user_id, review_id, content, confirmed, created_at, updated_at
)
VALUES (
           '44444444-4444-4444-4444-000000000001',
           '22222222-2222-2222-2222-000000000001',
           '33333333-3333-3333-3333-000000000001',
           '7일 지난 알림',
           TRUE,
           TIMESTAMP '2025-07-10 10:00:00',
           TIMESTAMP '2025-07-14 00:00:00'
       );

-- 삭제되지 않아야 할 알림 (updated_at이 하루 전)
INSERT INTO notifications (
    id, user_id, review_id, content, confirmed, created_at, updated_at
)
VALUES (
           '44444444-4444-4444-4444-000000000002',
           '22222222-2222-2222-2222-000000000001',
           '33333333-3333-3333-3333-000000000001',
           '최근 알림',
           TRUE,
           TIMESTAMP '2025-07-20 10:00:00',
           TIMESTAMP '2025-07-21 00:00:00'
       );
