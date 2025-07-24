package com.twogether.deokhugam.book.entity;

import com.twogether.deokhugam.book.dto.BookDto;
import com.twogether.deokhugam.book.dto.request.BookCreateRequest;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name= "books")
@Getter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Book {

    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Setter
    @Column(name = "title", nullable = false, length = 255)
    private String title;
    @Setter
    @Column(name = "author", nullable = false, length = 100)
    private String author;
    @Setter
    @Column(name = "description", columnDefinition = "TEXT", nullable = false, length = Integer.MAX_VALUE)
    private String description;
    @Setter
    @Column(name = "publisher", nullable = false, length = 100)
    private String publisher;
    @Setter
    @Column(name = "published_date", nullable = false)
    private LocalDate publishedDate;

    @Setter
    @Column(name = "isbn", length = 13)
    private String isbn;

    @Setter
    @Column(name = "thumbnail_url", columnDefinition = "TEXT")
    private String thumbnailUrl;

    @Setter
    @Column(name = "review_count", nullable = false)
    private Integer reviewCount;

    @Setter
    @Column(name = "rating", nullable = false)
    private Float rating;

    @Column(name = "created_at", nullable = false)
    @CreatedDate
    private Instant createdAt;

    @Column(name = "updated_at")
    @LastModifiedDate
    private Instant updatedAt;

    @Setter
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    public Book(UUID id, String title, String author, String description, String publisher,
        LocalDate publishedDate) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.description = description;
        this.publisher = publisher;
        this.publishedDate = publishedDate;
        this.rating = 0F;
        this.reviewCount = 0;
    }

    public Book(String title, String author, String description, String publisher, LocalDate publishedDate) {
        this.title = title;
        this.author = author;
        this.description = description;
        this.publisher = publisher;
        this.publishedDate = publishedDate;
        this.rating = 0F;
        this.reviewCount = 0;
    }
    public static Book of(BookCreateRequest request) {
        Book book = new Book(
            request.title(),
            request.author(),
            request.description(),
            request.publisher(),
            request.publishedDate()
        );
        if (request.isbn() != null && !request.isbn().isBlank()) {
            book.setIsbn(request.isbn());
        }
        return book;
    }
    public BookDto toDto(){
        return new BookDto(
            this.id,
            this.title,
            this.author,
            this.description,
            this.publisher,
            this.publishedDate,
            this.isbn,
            this.thumbnailUrl,
            this.reviewCount,
            this.rating,
            this.createdAt,
            this.updatedAt
        );
    }
}
