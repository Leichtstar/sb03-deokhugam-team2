-- 유저 생성
-- CREATE USER twogether WITH PASSWORD 'omega2503';
-- CREATE DATABASE deokhugam WITH OWNER = twogether;
-- GRANT ALL PRIVILEGES ON DATABASE deokhugam TO twogether;
-- create schema if not exists deokhugam authorization twogether;

-- 테이블 초기화
-- DROP TABLE IF EXISTS books CASCADE;
-- DROP TABLE IF EXISTS users CASCADE;
-- DROP TABLE IF EXISTS reviews CASCADE;
-- DROP TABLE IF EXISTS review_like CASCADE;
-- DROP TABLE IF EXISTS comments CASCADE;
-- DROP TABLE IF EXISTS popular_review_ranking CASCADE;
-- DROP TABLE IF EXISTS power_user_ranking CASCADE;
-- DROP TABLE IF EXISTS popular_book_ranking CASCADE;
-- DROP TYPE IF EXISTS ranking_period;
-- DROP TABLE IF EXISTS notification CASCADE;

-- 도서관리 테이블
CREATE TABLE books
(
    id UUID                             PRIMARY KEY,
    title VARCHAR(100)                  NOT NULL,
    author VARCHAR(50)                  NOT NULL,
    description TEXT                    NOT NULL,
    publisher VARCHAR(50)               NOT NULL,
    published_date DATE                 NOT NULL,
    isbn VARCHAR(13)                    UNIQUE,
    thumbnail_url VARCHAR(255),
    review_count INTEGER                NOT NULL,
    rating REAL                         NOT NULL,
    created_at TIMESTAMPTZ              NOT NULL,
    updated_at TIMESTAMPTZ,
    is_deleted BOOLEAN                  NOT NULL
);

-- 사용자 테이블
CREATE TABLE users
(
    id         uuid                     PRIMARY KEY,
    email      varchar(255) UNIQUE      NOT NULL,
    nickname   varchar(50)              NOT NULL,
    password   varchar(50)              NOT NULL,
    is_deleted boolean                  NOT NULL
);
-- 리뷰 테이블
CREATE TABLE reviews
(
    id  UUID                            PRIMARY KEY,
    book_id UUID                        NOT NULL,
    user_id UUID                        NOT NULL,

    book_title VARCHAR(100)             NOT NULL,
    book_thumbnail_url VARCHAR(255),
    user_nickname VARCHAR(50)           NOT NULL,

    content TEXT                        NOT NULL,
    rating REAL                         NOT NULL,
    like_count BIGINT,
    comment_count BIGINT,
    liked_by_me BOOLEAN                 NOT NULL,
    created_at TIMESTAMPTZ              NOT NULL,
    updated_at TIMESTAMPTZ,
    is_deleted BOOLEAN                  NOT NULL,

    CONSTRAINT fk_reviews_book_id FOREIGN KEY (book_id) REFERENCES books (id) ON DELETE CASCADE,
    CONSTRAINT fk_reviews_user_id FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

-- 리뷰_좋아요 테이블
CREATE TABLE review_like
(
    review_id UUID                      NOT NULL,
    user_id UUID                        NOT NULL,
    liked BOOLEAN                       NOT NULL,

    CONSTRAINT pk_review_like PRIMARY KEY (review_id, user_id),
    CONSTRAINT fk_review_like_review_id FOREIGN KEY (review_id) REFERENCES reviews (id) ON DELETE CASCADE,
    CONSTRAINT fk_review_like_user_id FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

-- 댓글 테이블
CREATE TABLE comments
(
    id UUID                             PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    review_id UUID NOT NULL REFERENCES reviews(id) ON DELETE CASCADE,

    content VARCHAR(200)                NOT NULL,
    created_at TIMESTAMPTZ              NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ              NOT NULL DEFAULT now(),
    is_deleted BOOLEAN                  NOT NULL DEFAULT FALSE
);

-- ENUM 타입 정의
CREATE TYPE ranking_period AS ENUM ('DAILY', 'WEEKLY', 'MONTHLY', 'ALL_TIME');

-- 알림 테이블 (ON DELETE CASCADE)
CREATE TABLE notification (
    id UUID                             PRIMARY KEY,
    content VARCHAR(255)                NOT NULL,
    confirmed BOOLEAN                   NOT NULL,
    created_at TIMESTAMPTZ              NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ              NOT NULL DEFAULT now(),
    review_id UUID,
    user_id UUID,

    FOREIGN KEY (review_id) REFERENCES reviews(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 인기 도서 랭킹 (ON DELETE SET NULL)
CREATE TABLE popular_book_ranking (
    id UUID                             PRIMARY KEY ,
    book_id UUID,
    period ranking_period               NOT NULL,
    score DOUBLE PRECISION              NOT NULL,
    review_count BIGINT                 NOT NULL,
    rating DOUBLE PRECISION             NOT NULL,
    rank INT                            NOT NULL,
    title VARCHAR(100)                  NOT NULL,
    author VARCHAR(50)                  NOT NULL,
    thumbnail_url VARCHAR(255),
    created_at TIMESTAMPTZ              NOT NULL DEFAULT now(),

    FOREIGN KEY (book_id) REFERENCES books(id) ON DELETE SET NULL
);

-- 인기 리뷰 랭킹 (ON DELETE SET NULL)
CREATE TABLE popular_review_ranking (
    id UUID                             PRIMARY KEY ,
    review_id UUID,
    period ranking_period               NOT NULL,
    score DOUBLE PRECISION              NOT NULL,
    like_count BIGINT                   NOT NULL,
    comment_count BIGINT                NOT NULL,
    rank INT                            NOT NULL,
    user_id UUID,
    user_nickname VARCHAR(50)           NOT NULL,
    content TEXT                        NOT NULL,
    rating DOUBLE PRECISION             NOT NULL,
    book_id UUID,
    book_title VARCHAR(100)             NOT NULL,
    book_thumbnail_url VARCHAR(255),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    FOREIGN KEY (review_id) REFERENCES reviews(id) ON DELETE SET NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
    FOREIGN KEY (book_id) REFERENCES books(id) ON DELETE SET NULL
);

-- 파워 유저 랭킹 (ON DELETE SET NULL)
CREATE TABLE power_user_ranking (
    id UUID                             PRIMARY KEY,
    user_id UUID,
    period ranking_period               NOT NULL,
    score DOUBLE PRECISION              NOT NULL,
    review_score_sum DOUBLE PRECISION   NOT NULL,
    like_count BIGINT                   NOT NULL,
    comment_count BIGINT                NOT NULL,
    rank INT                            NOT NULL,
    nickname VARCHAR(50)                NOT NULL,
    created_at TIMESTAMPTZ              NOT NULL DEFAULT now(),

    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
);