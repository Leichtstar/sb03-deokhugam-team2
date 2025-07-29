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
	//키워드 없을 시
	@Query("""
    SELECT COUNT(b) FROM Book b
    WHERE b.isDeleted = false
      AND (:keyword IS NULL OR
           LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
           LOWER(b.author) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
           LOWER(b.isbn) LIKE LOWER(CONCAT('%', :keyword, '%')))
""")
	long countByKeyword(@Param("keyword") String keyword);

	//title기준 DESC 정렬 쿼리
	@Query("""
    SELECT b FROM Book b
    WHERE b.isDeleted = false
      AND (:keyword IS NULL OR
           LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
           LOWER(b.author) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
           LOWER(b.isbn) LIKE LOWER(CONCAT('%', :keyword, '%')))
      AND (:cursorTitle IS NULL OR
            (b.title < :cursorTitle OR (b.title = :cursorTitle AND b.createdAt < :after)))
    ORDER BY b.title DESC, b.createdAt DESC
""")
	List<Book> findPageByTitleDesc(
		@Param("keyword") String keyword,
		@Param("cursorTitle") String cursorTitle,
		@Param("after") Instant after,
		Pageable pageable
	);
	//title기준 ASC정렬 쿼리
	@Query("""
    SELECT b FROM Book b
    WHERE b.isDeleted = false
      AND (:keyword IS NULL OR
           LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
           LOWER(b.author) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
           LOWER(b.isbn) LIKE LOWER(CONCAT('%', :keyword, '%')))
      AND (:cursorTitle IS NULL OR
            (b.title > :cursorTitle OR (b.title = :cursorTitle AND b.createdAt > :after)))
    ORDER BY b.title ASC, b.createdAt ASC
""")
	List<Book> findPageByTitleAsc(
		@Param("keyword") String keyword,
		@Param("cursorTitle") String cursorTitle,
		@Param("after") Instant after,
		Pageable pageable
	);
	//publishedDate기준 DESC 정렬 쿼리
	@Query("""
    SELECT b FROM Book b
    WHERE b.isDeleted = false
      AND (:keyword IS NULL OR
           LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
           LOWER(b.author) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
           LOWER(b.isbn) LIKE LOWER(CONCAT('%', :keyword, '%')))
      AND (:cursorDate IS NULL OR
            (b.publishedDate < :cursorDate OR (b.publishedDate = :cursorDate AND b.createdAt < :after)))
    ORDER BY b.publishedDate DESC, b.createdAt DESC
""")
	List<Book> findPageByPublishedDateDesc(
		@Param("keyword") String keyword,
		@Param("cursorDate") LocalDate cursorDate,
		@Param("after") Instant after,
		Pageable pageable
	);
	//publishedDate기준 ASC정렬 쿼리
	@Query("""
    SELECT b FROM Book b
    WHERE b.isDeleted = false
      AND (:keyword IS NULL OR
           LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
           LOWER(b.author) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
           LOWER(b.isbn) LIKE LOWER(CONCAT('%', :keyword, '%')))
      AND (:cursorDate IS NULL OR
            (b.publishedDate > :cursorDate OR (b.publishedDate = :cursorDate AND b.createdAt > :after)))
    ORDER BY b.publishedDate ASC, b.createdAt ASC
""")
	List<Book> findPageByPublishedDateAsc(
		@Param("keyword") String keyword,
		@Param("cursorDate") LocalDate cursorDate,
		@Param("after") Instant after,
		Pageable pageable
	);
	//rating기준 DESC 정렬 쿼리
	@Query("""
    SELECT b FROM Book b
    WHERE b.isDeleted = false
      AND (:keyword IS NULL OR
           LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
           LOWER(b.author) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
           LOWER(b.isbn) LIKE LOWER(CONCAT('%', :keyword, '%')))
      AND (:cursorRating IS NULL OR
            (b.rating < :cursorRating OR (b.rating = :cursorRating AND b.createdAt < :after)))
    ORDER BY b.rating DESC, b.createdAt DESC
""")
	List<Book> findPageByRatingDesc(
		@Param("keyword") String keyword,
		@Param("cursorRating") Float cursorRating,
		@Param("after") Instant after,
		Pageable pageable
	);
	//rating기준 ASC정렬 쿼리
	@Query("""
    SELECT b FROM Book b
    WHERE b.isDeleted = false
      AND (:keyword IS NULL OR
           LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
           LOWER(b.author) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
           LOWER(b.isbn) LIKE LOWER(CONCAT('%', :keyword, '%')))
      AND (:cursorRating IS NULL OR
            (b.rating > :cursorRating OR (b.rating = :cursorRating AND b.createdAt > :after)))
    ORDER BY b.rating ASC, b.createdAt ASC
""")
	List<Book> findPageByRatingAsc(
		@Param("keyword") String keyword,
		@Param("cursorRating") Float cursorRating,
		@Param("after") Instant after,
		Pageable pageable
	);
	//reviewCount기준 DESC 정렬 쿼리
	@Query("""
    SELECT b FROM Book b
    WHERE b.isDeleted = false
      AND (:keyword IS NULL OR
           LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
           LOWER(b.author) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
           LOWER(b.isbn) LIKE LOWER(CONCAT('%', :keyword, '%')))
      AND (:cursorReviewCount IS NULL OR
            (b.reviewCount < :cursorReviewCount OR (b.reviewCount = :cursorReviewCount AND b.createdAt < :after)))
    ORDER BY b.reviewCount DESC, b.createdAt DESC
""")
	List<Book> findPageByReviewCountDesc(
		@Param("keyword") String keyword,
		@Param("cursorReviewCount") Integer cursorReviewCount,
		@Param("after") Instant after,
		Pageable pageable
	);
	//reviewCount기준 ASC정렬 쿼리
	@Query("""
    SELECT b FROM Book b
    WHERE b.isDeleted = false
      AND (:keyword IS NULL OR
           LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
           LOWER(b.author) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
           LOWER(b.isbn) LIKE LOWER(CONCAT('%', :keyword, '%')))
      AND (:cursorReviewCount IS NULL OR
            (b.reviewCount > :cursorReviewCount OR (b.reviewCount = :cursorReviewCount AND b.createdAt > :after)))
    ORDER BY b.reviewCount ASC, b.createdAt ASC
""")
	List<Book> findPageByReviewCountAsc(
		@Param("keyword") String keyword,
		@Param("cursorReviewCount") Integer cursorReviewCount,
		@Param("after") Instant after,
		Pageable pageable
	);
	//기본 createdAt기준 DESC정렬 쿼리
	@Query("""
    SELECT b FROM Book b
    WHERE b.isDeleted = false
      AND (:keyword IS NULL OR
           LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
           LOWER(b.author) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
           LOWER(b.isbn) LIKE LOWER(CONCAT('%', :keyword, '%')))
      AND (:after IS NULL OR b.createdAt < :after)
    ORDER BY b.createdAt DESC
""")
	List<Book> findBooksByKeywordAndAfter(
		@Param("keyword") String keyword,
		@Param("after") Instant after,
		Pageable pageable
	);
	//기본 createdAt기준 ASC정렬 쿼리
	@Query("""
    SELECT b FROM Book b
    WHERE b.isDeleted = false
      AND (:keyword IS NULL OR
           LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
           LOWER(b.author) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
           LOWER(b.isbn) LIKE LOWER(CONCAT('%', :keyword, '%')))
      AND (:after IS NULL OR b.createdAt > :after)
    ORDER BY b.createdAt ASC
""")
	List<Book> findBooksByKeywordAndAfterAsc(
		@Param("keyword") String keyword,
		@Param("after") Instant after,
		Pageable pageable
	);

	@Modifying
	@Query("""
    UPDATE Book b SET 
    	b.reviewCount = (SELECT COUNT(r) FROM Review r WHERE r.book.id = :bookId AND r.isDeleted = false),
        b.rating = (SELECT COALESCE(AVG(r.rating * 1.0), 0) FROM Review r WHERE r.book.id = :bookId AND r.isDeleted = false)
    WHERE b.id = :bookId
""")
	void updateBookReviewStats(@Param("bookId") UUID bookId);

}
