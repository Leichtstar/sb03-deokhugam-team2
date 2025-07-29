-- 유저 생성
-- CREATE DATABASE deokhugam WITH OWNER = twogether;
-- CREATE USER twogether WITH PASSWORD 'omega2503';
-- GRANT ALL PRIVILEGES ON DATABASE deokhugam TO twogether;
-- create schema if not exists deokhugam authorization twogether;
-- ALTER ROLE twogether SET search_path TO deokhugam;

-- 테이블 초기화
DROP TABLE IF EXISTS notifications;
DROP TABLE IF EXISTS comments;
DROP TABLE IF EXISTS review_like;
DROP TABLE IF EXISTS power_user_ranking;
DROP TABLE IF EXISTS popular_review_ranking;
DROP TABLE IF EXISTS popular_book_ranking;
DROP TABLE IF EXISTS reviews;
DROP TABLE IF EXISTS books;
DROP TABLE IF EXISTS users;

-- 도서 테이블
CREATE TABLE books (
                       id UUID PRIMARY KEY,
                       title VARCHAR(255) NOT NULL,
                       author VARCHAR(100) NOT NULL,
                       description TEXT NOT NULL,
                       publisher VARCHAR(100) NOT NULL,
                       published_date DATE NOT NULL,
                       isbn VARCHAR(13),
                       thumbnail_url TEXT,
                       review_count INTEGER NOT NULL,
                       rating REAL NOT NULL,
                       created_at TIMESTAMP NOT NULL,
                       updated_at TIMESTAMP,
                       is_deleted BOOLEAN NOT NULL
);

-- 사용자 테이블
CREATE TABLE users (
                       id UUID PRIMARY KEY,
                       email VARCHAR(100) UNIQUE NOT NULL,
                       nickname VARCHAR(20) UNIQUE NOT NULL,
                       password VARCHAR(20) NOT NULL,
                       created_at TIMESTAMP NOT NULL,
                       updated_at TIMESTAMP,
                       is_deleted BOOLEAN NOT NULL
);

-- 리뷰 테이블
CREATE TABLE reviews (
                         id UUID PRIMARY KEY,
                         book_id UUID NOT NULL,
                         user_id UUID NOT NULL,

                         book_title VARCHAR(255) NOT NULL,
                         book_thumbnail_url TEXT,
                         user_nickname VARCHAR(50) NOT NULL,

                         content TEXT NOT NULL,
                         rating REAL NOT NULL,
                         like_count BIGINT,
                         comment_count BIGINT,
                         created_at TIMESTAMP NOT NULL,
                         updated_at TIMESTAMP,
                         is_deleted BOOLEAN NOT NULL,

                         CONSTRAINT fk_reviews_book_id FOREIGN KEY (book_id) REFERENCES books (id),
                         CONSTRAINT fk_reviews_user_id FOREIGN KEY (user_id) REFERENCES users (id)
);

-- 논리 삭제 컬럼을 포함해 Unique 제약을 유지하면서 중복 허용
CREATE UNIQUE INDEX uq_reviews_book_user_deleted_comp
    ON reviews (book_id, user_id, is_deleted);

-- 리뷰 좋아요 테이블
CREATE TABLE review_like (
                             review_id UUID NOT NULL,
                             user_id UUID NOT NULL,
                             liked BOOLEAN NOT NULL,

                             PRIMARY KEY (review_id, user_id),
                             CONSTRAINT fk_review_like_review_id FOREIGN KEY (review_id) REFERENCES reviews (id),
                             CONSTRAINT fk_review_like_user_id FOREIGN KEY (user_id) REFERENCES users (id)
);

-- 댓글 테이블
CREATE TABLE comments (
                          id UUID PRIMARY KEY,
                          user_id UUID NOT NULL,
                          review_id UUID NOT NULL,
                          content VARCHAR(200) NOT NULL,
                          created_at TIMESTAMP NOT NULL,
                          updated_at TIMESTAMP NOT NULL,
                          is_deleted BOOLEAN NOT NULL,

                          CONSTRAINT fk_comments_user_id FOREIGN KEY (user_id) REFERENCES users (id),
                          CONSTRAINT fk_comments_review_id FOREIGN KEY (review_id) REFERENCES reviews (id)
);

-- 알림 테이블
CREATE TABLE notifications (
                               id UUID PRIMARY KEY,
                               content VARCHAR(300) NOT NULL,
                               confirmed BOOLEAN NOT NULL,
                               created_at TIMESTAMP NOT NULL,
                               updated_at TIMESTAMP NOT NULL,
                               review_id UUID NOT NULL,
                               user_id UUID NOT NULL,

                               CONSTRAINT fk_notifications_review_id FOREIGN KEY (review_id) REFERENCES reviews (id),
                               CONSTRAINT fk_notifications_user_id FOREIGN KEY (user_id) REFERENCES users (id)
);

-- 인기 도서 랭킹 테이블
CREATE TABLE popular_book_ranking (
                                      id UUID PRIMARY KEY,
                                      book_id UUID,
                                      period VARCHAR(20) NOT NULL,
                                      score DOUBLE PRECISION NOT NULL,
                                      review_count BIGINT NOT NULL,
                                      rating DOUBLE PRECISION NOT NULL,
                                      rank INT NOT NULL,
                                      title VARCHAR(255) NOT NULL,
                                      author VARCHAR(100) NOT NULL,
                                      thumbnail_url TEXT,
                                      created_at TIMESTAMP NOT NULL,

                                      CONSTRAINT fk_popular_book_book_id FOREIGN KEY (book_id) REFERENCES books (id)
);

-- 인기 리뷰 랭킹 테이블
CREATE TABLE popular_review_ranking (
                                        id UUID PRIMARY KEY,
                                        review_id UUID,
                                        period VARCHAR(20) NOT NULL,
                                        score DOUBLE PRECISION NOT NULL,
                                        like_count BIGINT NOT NULL,
                                        comment_count BIGINT NOT NULL,
                                        rank INT NOT NULL,
                                        user_id UUID,
                                        user_nickname VARCHAR(50) NOT NULL,
                                        content TEXT NOT NULL,
                                        rating DOUBLE PRECISION NOT NULL,
                                        book_id UUID,
                                        book_title VARCHAR(255) NOT NULL,
                                        book_thumbnail_url TEXT,
                                        created_at TIMESTAMP NOT NULL,

                                        CONSTRAINT fk_popular_review_review_id FOREIGN KEY (review_id) REFERENCES reviews (id),
                                        CONSTRAINT fk_popular_review_user_id FOREIGN KEY (user_id) REFERENCES users (id),
                                        CONSTRAINT fk_popular_review_book_id FOREIGN KEY (book_id) REFERENCES books (id)
);

-- 파워 유저 랭킹 테이블
CREATE TABLE power_user_ranking (
                                    id UUID PRIMARY KEY,
                                    user_id UUID,
                                    period VARCHAR(20) NOT NULL,
                                    score DOUBLE PRECISION NOT NULL,
                                    review_score_sum DOUBLE PRECISION NOT NULL,
                                    like_count BIGINT NOT NULL,
                                    comment_count BIGINT NOT NULL,
                                    rank INT NOT NULL,
                                    nickname VARCHAR(50) NOT NULL,
                                    created_at TIMESTAMP NOT NULL,

                                    CONSTRAINT fk_power_user_user_id FOREIGN KEY (user_id) REFERENCES users (id)
);