package expert.os.demos.books.api;

import expert.os.demos.books.infrastructure.FieldAccessStrategy;
import jakarta.json.bind.annotation.JsonbVisibility;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.List;


@Schema(description = "Book representation for API responses")
@JsonbVisibility(FieldAccessStrategy.class)
public class BookResponse {

    @Schema(description = "Unique identifier of the book", example = "123e4567-e89b-12d3-a456-426614174000")
    private String id;

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

    public String getId() {
        return id;
    }

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

    public void setId(String id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public int getPublicationYear() {
        return publicationYear;
    }

    public void setPublicationYear(int publicationYear) {
        this.publicationYear = publicationYear;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }
}
