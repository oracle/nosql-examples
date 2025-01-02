package expert.os.demos.books.api;

import expert.os.demos.books.infrastructure.FieldAccessStrategy;
import jakarta.json.bind.annotation.JsonbVisibility;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.List;

@JsonbVisibility(FieldAccessStrategy.class)
@Schema(description = "Book request payload for creating or updating a book")
public class BookRequest {


    @Schema(description = "Title of the book", example = "The Catcher in the Rye")
    private String title;

    @Schema(description = "Genre of the book", example = "FICTION")
    private String genre;

    @Schema(description = "Year the book was published", example = "1951")
    private int publicationYear;

    @Schema(description = "Author of the book", example = "J.D. Salinger")
    private String author;

    @Schema(description = "List of tags associated with the book", example = "[\"Classic\", \"Literature\", \"American\"]")
    private List<String> tags;

    public String getTitle() {
        return title;
    }

    public String getGenre() {
        return genre;
    }

    public String getAuthor() {
        return author;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getPublicationYear() {
        return publicationYear;
    }

    public void setPublicationYear(int publicationYear) {
        this.publicationYear = publicationYear;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }
}
