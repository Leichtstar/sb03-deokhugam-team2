-- 유저 생성
-- CREATE DATABASE deokhugam WITH OWNER = twogether;
-- CREATE USER twogether WITH PASSWORD 'omega2503';
-- GRANT ALL PRIVILEGES ON DATABASE deokhugam TO twogether;
-- create schema if not exists deokhugam authorization twogether;
-- ALTER ROLE twogether SET search_path TO deokhugam;

-- 테이블 초기화
DROP TABLE IF EXISTS books CASCADE;
DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS reviews CASCADE;
DROP TABLE IF EXISTS review_like CASCADE;
DROP TABLE IF EXISTS comments CASCADE;
DROP TABLE IF EXISTS popular_review_ranking CASCADE;
DROP TABLE IF EXISTS power_user_ranking CASCADE;
DROP TABLE IF EXISTS popular_book_ranking CASCADE;
DROP TABLE IF EXISTS notifications CASCADE;

-- 도서관리 테이블
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
                         is_deleted BOOLEAN NOT NULL
);

CREATE UNIQUE INDEX uq_reviews_book_user_partial
    ON reviews (book_id, user_id);

-- 리뷰 좋아요
CREATE TABLE review_like (
                             review_id UUID NOT NULL,
                             user_id UUID NOT NULL,
                             liked BOOLEAN NOT NULL,
                             PRIMARY KEY (review_id, user_id)
);

-- 댓글
CREATE TABLE comments (
                          id UUID PRIMARY KEY,
                          user_id UUID NOT NULL,
                          review_id UUID NOT NULL,
                          content VARCHAR(200) NOT NULL,
                          created_at TIMESTAMP NOT NULL,
                          updated_at TIMESTAMP NOT NULL,
                          is_deleted BOOLEAN NOT NULL
);

-- 알림
CREATE TABLE notifications (
                               id UUID PRIMARY KEY,
                               content VARCHAR(300) NOT NULL,
                               confirmed BOOLEAN NOT NULL,
                               created_at TIMESTAMP NOT NULL,
                               updated_at TIMESTAMP NOT NULL,
                               review_id UUID NOT NULL,
                               user_id UUID NOT NULL
);

-- 인기 도서 랭킹
CREATE TABLE popular_book_ranking (
                                      id UUID PRIMARY KEY,
                                      book_id UUID NOT NULL,
                                      period VARCHAR(20) NOT NULL,
                                      score DOUBLE PRECISION NOT NULL,
                                      review_count BIGINT NOT NULL,
                                      rating DOUBLE PRECISION NOT NULL,
                                      rank INT NOT NULL,
                                      title VARCHAR(255) NOT NULL,
                                      author VARCHAR(100) NOT NULL,
                                      thumbnail_url TEXT,
                                      created_at TIMESTAMP NOT NULL
);

-- 인기 리뷰 랭킹
CREATE TABLE popular_review_ranking (
                                        id UUID PRIMARY KEY,
                                        review_id UUID NOT NULL,
                                        period VARCHAR(20) NOT NULL,
                                        score DOUBLE PRECISION NOT NULL,
                                        like_count BIGINT NOT NULL,
                                        comment_count BIGINT NOT NULL,
                                        rank INT NOT NULL,
                                        user_id UUID NOT NULL,
                                        user_nickname VARCHAR(50) NOT NULL,
                                        content TEXT NOT NULL,
                                        rating DOUBLE PRECISION NOT NULL,
                                        book_id UUID NOT NULL,
                                        book_title VARCHAR(255) NOT NULL,
                                        book_thumbnail_url TEXT,
                                        created_at TIMESTAMP NOT NULL
);

-- 파워 유저 랭킹
CREATE TABLE power_user_ranking (
                                    id UUID PRIMARY KEY,
                                    user_id UUID NOT NULL,
                                    period VARCHAR(20) NOT NULL,
                                    score DOUBLE PRECISION NOT NULL,
                                    review_score_sum DOUBLE PRECISION NOT NULL,
                                    like_count BIGINT NOT NULL,
                                    comment_count BIGINT NOT NULL,
                                    rank INT NOT NULL,
                                    nickname VARCHAR(50) NOT NULL,
                                    created_at TIMESTAMP NOT NULL
);