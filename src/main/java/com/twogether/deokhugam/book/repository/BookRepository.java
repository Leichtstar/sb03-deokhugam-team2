package com.twogether.deokhugam.book.repository;

import com.twogether.deokhugam.book.entity.Book;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BookRepository extends JpaRepository<Book, UUID> {

	boolean existsByIsbn(String isbn);

	@Query(value = """
        SELECT COUNT(*)
        FROM books b
        WHERE b.is_deleted = false
          AND (
            :keyword IS NULL
            OR LOWER(b.title) LIKE LOWER('%' || :keyword || '%')
            OR LOWER(b.author) LIKE LOWER('%' || :keyword || '%')
            OR LOWER(b.isbn) LIKE LOWER('%' || :keyword || '%')
          )
        """, nativeQuery = true)
	long countByKeyword(@Param("keyword") String keyword);

	// Title DESC + Collate
	@Query(value = """
        SELECT * FROM books b
        WHERE b.is_deleted = false
          AND (
            :keyword IS NULL
            OR LOWER(b.title) LIKE LOWER('%' || :keyword || '%')
            OR LOWER(b.author) LIKE LOWER('%' || :keyword || '%')
            OR LOWER(b.isbn) LIKE LOWER('%' || :keyword || '%')
          )
          AND (
            :cursorTitle IS NULL
            OR (b.title < :cursorTitle OR (b.title = :cursorTitle AND b.created_at < :after))
          )
        ORDER BY b.title COLLATE "ko-KR-x-icu" DESC, b.created_at DESC
        """, nativeQuery = true)
	List<Book> findPageByTitleDesc(
		@Param("keyword") String keyword,
		@Param("cursorTitle") String cursorTitle,
		@Param("after") Instant after,
		Pageable pageable);

	// Title ASC + Collate
	@Query(value = """
        SELECT * FROM books b
        WHERE b.is_deleted = false
          AND (
            :keyword IS NULL
            OR LOWER(b.title) LIKE LOWER('%' || :keyword || '%')
            OR LOWER(b.author) LIKE LOWER('%' || :keyword || '%')
            OR LOWER(b.isbn) LIKE LOWER('%' || :keyword || '%')
          )
          AND (
            :cursorTitle IS NULL
            OR (b.title > :cursorTitle OR (b.title = :cursorTitle AND b.created_at > :after))
          )
        ORDER BY b.title COLLATE "ko-KR-x-icu" ASC, b.created_at ASC
        """, nativeQuery = true)
	List<Book> findPageByTitleAsc(
		@Param("keyword") String keyword,
		@Param("cursorTitle") String cursorTitle,
		@Param("after") Instant after,
		Pageable pageable);

	// PublishedDate DESC
	@Query(value = """
        SELECT * FROM books b
        WHERE b.is_deleted = false
          AND (
            :keyword IS NULL
            OR LOWER(b.title) LIKE LOWER('%' || :keyword || '%')
            OR LOWER(b.author) LIKE LOWER('%' || :keyword || '%')
            OR LOWER(b.isbn) LIKE LOWER('%' || :keyword || '%')
          )
          AND (
            :cursorDate IS NULL
            OR (b.published_date < :cursorDate OR (b.published_date = :cursorDate AND b.created_at < :after))
          )
        ORDER BY b.published_date DESC, b.created_at DESC
        """, nativeQuery = true)
	List<Book> findPageByPublishedDateDesc(
		@Param("keyword") String keyword,
		@Param("cursorDate") LocalDate cursorDate,
		@Param("after") Instant after,
		Pageable pageable);

	// PublishedDate ASC
	@Query(value = """
        SELECT * FROM books b
        WHERE b.is_deleted = false
          AND (
            :keyword IS NULL
            OR LOWER(b.title) LIKE LOWER('%' || :keyword || '%')
            OR LOWER(b.author) LIKE LOWER('%' || :keyword || '%')
            OR LOWER(b.isbn) LIKE LOWER('%' || :keyword || '%')
          )
          AND (
            :cursorDate IS NULL
            OR (b.published_date > :cursorDate OR (b.published_date = :cursorDate AND b.created_at > :after))
          )
        ORDER BY b.published_date ASC, b.created_at ASC
        """, nativeQuery = true)
	List<Book> findPageByPublishedDateAsc(
		@Param("keyword") String keyword,
		@Param("cursorDate") LocalDate cursorDate,
		@Param("after") Instant after,
		Pageable pageable);

	// Rating DESC
	@Query(value = """
        SELECT * FROM books b
        WHERE b.is_deleted = false
          AND (
            :keyword IS NULL
            OR LOWER(b.title) LIKE LOWER('%' || :keyword || '%')
            OR LOWER(b.author) LIKE LOWER('%' || :keyword || '%')
            OR LOWER(b.isbn) LIKE LOWER('%' || :keyword || '%')
          )
          AND (
            :cursorRating IS NULL
            OR (b.rating < :cursorRating OR (b.rating = :cursorRating AND b.created_at < :after))
          )
        ORDER BY b.rating DESC, b.created_at DESC
        """, nativeQuery = true)
	List<Book> findPageByRatingDesc(
		@Param("keyword") String keyword,
		@Param("cursorRating") Float cursorRating,
		@Param("after") Instant after,
		Pageable pageable);

	// Rating ASC
	@Query(value = """
        SELECT * FROM books b
        WHERE b.is_deleted = false
          AND (
            :keyword IS NULL
            OR LOWER(b.title) LIKE LOWER('%' || :keyword || '%')
            OR LOWER(b.author) LIKE LOWER('%' || :keyword || '%')
            OR LOWER(b.isbn) LIKE LOWER('%' || :keyword || '%')
          )
          AND (
            :cursorRating IS NULL
            OR (b.rating > :cursorRating OR (b.rating = :cursorRating AND b.created_at > :after))
          )
        ORDER BY b.rating ASC, b.created_at ASC
        """, nativeQuery = true)
	List<Book> findPageByRatingAsc(
		@Param("keyword") String keyword,
		@Param("cursorRating") Float cursorRating,
		@Param("after") Instant after,
		Pageable pageable);

	// ReviewCount DESC
	@Query(value = """
        SELECT * FROM books b
        WHERE b.is_deleted = false
          AND (
            :keyword IS NULL
            OR LOWER(b.title) LIKE LOWER('%' || :keyword || '%')
            OR LOWER(b.author) LIKE LOWER('%' || :keyword || '%')
            OR LOWER(b.isbn) LIKE LOWER('%' || :keyword || '%')
          )
          AND (
            :cursorReviewCount IS NULL
            OR (b.review_count < :cursorReviewCount OR (b.review_count = :cursorReviewCount AND b.created_at < :after))
          )
        ORDER BY b.review_count DESC, b.created_at DESC
        """, nativeQuery = true)
	List<Book> findPageByReviewCountDesc(
		@Param("keyword") String keyword,
		@Param("cursorReviewCount") Integer cursorReviewCount,
		@Param("after") Instant after,
		Pageable pageable);

	// ReviewCount ASC
	@Query(value = """
        SELECT * FROM books b
        WHERE b.is_deleted = false
          AND (
            :keyword IS NULL
            OR LOWER(b.title) LIKE LOWER('%' || :keyword || '%')
            OR LOWER(b.author) LIKE LOWER('%' || :keyword || '%')
            OR LOWER(b.isbn) LIKE LOWER('%' || :keyword || '%')
          )
          AND (
            :cursorReviewCount IS NULL
            OR (b.review_count > :cursorReviewCount OR (b.review_count = :cursorReviewCount AND b.created_at > :after))
          )
        ORDER BY b.review_count ASC, b.created_at ASC
        """, nativeQuery = true)
	List<Book> findPageByReviewCountAsc(
		@Param("keyword") String keyword,
		@Param("cursorReviewCount") Integer cursorReviewCount,
		@Param("after") Instant after,
		Pageable pageable);

	// CreatedAt DESC
	@Query(value = """
        SELECT * FROM books b
        WHERE b.is_deleted = false
          AND (
            :keyword IS NULL
            OR LOWER(b.title) LIKE LOWER('%' || :keyword || '%')
            OR LOWER(b.author) LIKE LOWER('%' || :keyword || '%')
            OR LOWER(b.isbn) LIKE LOWER('%' || :keyword || '%')
          )
          AND (:after IS NULL OR b.created_at < :after)
        ORDER BY b.created_at DESC
        """, nativeQuery = true)
	List<Book> findBooksByKeywordAndAfter(
		@Param("keyword") String keyword,
		@Param("after") Instant after,
		Pageable pageable);

	// CreatedAt ASC
	@Query(value = """
        SELECT * FROM books b
        WHERE b.is_deleted = false
          AND (
            :keyword IS NULL
            OR LOWER(b.title) LIKE LOWER('%' || :keyword || '%')
            OR LOWER(b.author) LIKE LOWER('%' || :keyword || '%')
            OR LOWER(b.isbn) LIKE LOWER('%' || :keyword || '%')
          )
          AND (:after IS NULL OR b.created_at > :after)
        ORDER BY b.created_at ASC
        """, nativeQuery = true)
	List<Book> findBooksByKeywordAndAfterAsc(
		@Param("keyword") String keyword,
		@Param("after") Instant after,
		Pageable pageable);

	@Modifying
	@Query(value = """
        UPDATE books
        SET review_count = (
            SELECT COUNT(*) FROM reviews r WHERE r.book_id = :bookId AND r.is_deleted = false
        ),
        rating = (
            SELECT COALESCE(AVG(r.rating * 1.0), 0) FROM reviews r WHERE r.book_id = :bookId AND r.is_deleted = false
        )
        WHERE id = :bookId
        """, nativeQuery = true)
	void updateBookReviewStats(@Param("bookId") UUID bookId);
}
