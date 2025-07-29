-- books
INSERT INTO books (
    id, title, author, publisher, published_date, review_count, rating, created_at, updated_at, is_deleted, description
)
VALUES
    ('11111111-1111-1111-1111-000000000001', '일간 책', '작가A', '출판사A', DATE '2024-01-01', 1, 4, TIMESTAMP '2025-07-22 00:00:00', TIMESTAMP '2025-07-22 00:00:00', FALSE, '일간용 도서'),
    ('11111111-1111-1111-1111-000000000002', '주간 책', '작가B', '출판사B', DATE '2024-01-01', 1, 5, TIMESTAMP '2025-07-16 00:00:00', TIMESTAMP '2025-07-16 00:00:00', FALSE, '주간용 도서'),
    ('11111111-1111-1111-1111-000000000003', '월간 책', '작가C', '출판사C', DATE '2024-01-01', 1, 4, TIMESTAMP '2025-06-25 00:00:00', TIMESTAMP '2025-06-25 00:00:00', FALSE, '월간용 도서'),
    ('11111111-1111-1111-1111-000000000004', '올타임 책', '작가D', '출판사D', DATE '2024-01-01', 1, 5, TIMESTAMP '2022-01-01 00:00:00', TIMESTAMP '2022-01-01 00:00:00', FALSE, '올타임용 도서');

-- users
INSERT INTO users (
    id, email, nickname, password, created_at, updated_at, is_deleted
)
VALUES
    ('22222222-2222-2222-2222-000000000001', 'user1@example.com', '일간유저', 'pw1234', TIMESTAMP '2025-07-22 00:00:00', TIMESTAMP '2025-07-22 00:00:00', FALSE),
    ('22222222-2222-2222-2222-000000000002', 'user2@example.com', '주간유저', 'pw1234', TIMESTAMP '2025-07-16 00:00:00', TIMESTAMP '2025-07-16 00:00:00', FALSE),
    ('22222222-2222-2222-2222-000000000003', 'user3@example.com', '월간유저', 'pw1234', TIMESTAMP '2025-06-25 00:00:00', TIMESTAMP '2025-06-25 00:00:00', FALSE),
    ('22222222-2222-2222-2222-000000000004', 'user4@example.com', '올타임유저', 'pw1234', TIMESTAMP '2022-01-01 00:00:00', TIMESTAMP '2022-01-01 00:00:00', FALSE);

-- reviews
INSERT INTO reviews (
    id, book_id, user_id,
    book_title, book_thumbnail_url, user_nickname,
    content, rating, like_count, comment_count,
    created_at, updated_at, is_deleted
)
VALUES
    -- DAILY
    ('33333333-3333-3333-3333-000000000001', '11111111-1111-1111-1111-000000000001', '22222222-2222-2222-2222-000000000001',
     '일간 책', 'http://image.com/1.jpg', '일간유저',
     '일간 리뷰', 4, 5, 2,
     TIMESTAMP '2025-07-22 09:00:00', TIMESTAMP '2025-07-22 09:00:00', FALSE),

    -- WEEKLY
    ('33333333-3333-3333-3333-000000000002', '11111111-1111-1111-1111-000000000002', '22222222-2222-2222-2222-000000000002',
     '주간 책', 'http://image.com/2.jpg', '주간유저',
     '주간 리뷰', 5, 3, 4,
     TIMESTAMP '2025-07-17 14:00:00', TIMESTAMP '2025-07-17 14:00:00', FALSE),

    -- MONTHLY
    ('33333333-3333-3333-3333-000000000003', '11111111-1111-1111-1111-000000000003', '22222222-2222-2222-2222-000000000003',
     '월간 책', 'http://image.com/3.jpg', '월간유저',
     '월간 리뷰', 4, 6, 1,
     TIMESTAMP '2025-06-27 09:00:00', TIMESTAMP '2025-06-27 09:00:00', FALSE),

    -- ALL_TIME
    ('33333333-3333-3333-3333-000000000004', '11111111-1111-1111-1111-000000000004', '22222222-2222-2222-2222-000000000004',
     '올타임 책', 'http://image.com/4.jpg', '올타임유저',
     '올타임 리뷰', 5, 10, 5,
     TIMESTAMP '2022-02-01 00:00:00', TIMESTAMP '2022-02-01 00:00:00', FALSE);