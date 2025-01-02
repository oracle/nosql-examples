package expert.os.demos.books.domain;

import jakarta.nosql.Column;
import jakarta.nosql.Entity;
import jakarta.nosql.Id;

import java.util.List;

@Entity
public class Book {

    @Id
    private String id = java.util.UUID.randomUUID().toString();

    @Column
    private String title;

    @Column
    private BookGenre genre;

    @Column
    private int publicationYear;

    @Column
    private String author;

    @Column
    private List<String> tags;

    public Book() {
    }

    private Book(Builder builder) {
        this.id = builder.id != null ? builder.id : java.util.UUID.randomUUID().toString();
        this.title = builder.title;
        this.genre = builder.bookGenre;
        this.publicationYear = builder.publicationYear;
        this.author = builder.author;
        this.tags = builder.tags;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public BookGenre getGenre() {
        return genre;
    }

    public int getPublicationYear() {
        return publicationYear;
    }

    public String getAuthor() {
        return author;
    }

    public List<String> getTags() {
        return tags;
    }

    public void update(Book book) {
        this.title = book.title;
        this.genre = book.genre;
        this.publicationYear = book.publicationYear;
        this.author = book.author;
        this.tags = book.tags;
    }

    public static class Builder {
        private String id;
        private String title;
        private BookGenre bookGenre;
        private int publicationYear;
        private String author;
        private List<String> tags;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder genre(BookGenre bookGenre) {
            this.bookGenre = bookGenre;
            return this;
        }

        public Builder publicationYear(int releaseYear) {
            this.publicationYear = releaseYear;
            return this;
        }

        public Builder author(String author) {
            this.author = author;
            return this;
        }

        public Builder tags(List<String> tags) {
            this.tags = tags;
            return this;
        }

        public Book build() {
            return new Book(this);
        }
    }

    public static Builder builder() {
        return new Builder();
    }
}
