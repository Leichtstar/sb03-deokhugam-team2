-- 사용자 생성
INSERT INTO users (id, email, nickname, password, is_deleted, created_at, updated_at)
VALUES
    ('11111111-1111-1111-1111-111111111111', 'user1@example.com', 'user1', 'pw1', false, NOW(), NOW());

-- 도서 생성
INSERT INTO books (id, isbn, title, author, publisher, description, thumbnail_url, rating, review_count, published_date, is_deleted, created_at, updated_at)
VALUES
    ('22222222-2222-2222-2222-222222222222', '1234567890123', '테스트 책 제목', '테스트 저자', '테스트 출판사', '책 설명입니다.', 'http://example.com/image.jpg', 4.5, 1, CURRENT_DATE, false, NOW(), NOW());

-- 리뷰 생성 (DAILY 기준: 오늘 00:00 ~ 내일 00:00 사이)
INSERT INTO reviews (id, user_id, book_id, content, rating, like_count, comment_count, user_nickname, book_title, book_thumbnail_url, is_deleted, created_at, updated_at)
VALUES
    ('33333333-3333-3333-3333-333333333333',
     '11111111-1111-1111-1111-111111111111',
     '22222222-2222-2222-2222-222222222222',
     '정말 재미있는 책이에요!',
     5,
     10,
     3,
     'user1',
     '테스트 책 제목',
     'http://example.com/image.jpg',
     false,
     NOW(), NOW());