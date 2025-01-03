package expert.os.demos.books.application;

import expert.os.demos.books.api.BookRequest;
import expert.os.demos.books.domain.BookGenre;
import expert.os.demos.books.domain.Book;
import org.assertj.core.api.SoftAssertions;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.Arrays;
import java.util.UUID;

import static org.instancio.Select.field;

class BookMapperTest {

    private BookMapper bookMapper;

    @BeforeEach
    void setUp() {
        bookMapper = Mappers.getMapper(BookMapper.class);
    }


    @Test
    void shouldMapMovieToMovieResponse() {

        var movie = Instancio.of(Book.class)
                .set(field("id"), UUID.randomUUID().toString())
                .set(field("title"), "Inception")
                .set(field("genre"), BookGenre.SCIENCE_FICTION)
                .set(field("publicationYear"), 2010)
                .set(field("author"), "Christopher Nolan")
                .set(field("tags"), Arrays.asList("Leonardo DiCaprio", "Joseph Gordon-Levitt"))
                .create();

        var movieResponse = bookMapper.toResponse(movie);

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(movieResponse).isNotNull();
            soft.assertThat(movieResponse.getId()).isEqualTo(movie.getId());
            soft.assertThat(movieResponse.getTitle()).isEqualTo(movie.getTitle());
            soft.assertThat(movieResponse.getGenre()).isEqualTo(movie.getGenre().toString());
            soft.assertThat(movieResponse.getPublicationYear()).isEqualTo(movie.getPublicationYear());
            soft.assertThat(movieResponse.getAuthor()).isEqualTo(movie.getAuthor());
            soft.assertThat(movieResponse.getTags()).isEqualTo(movie.getTags());
        });
    }

    @Test
    void shouldMapMovieRequestToMovieWithNewUUID() {

        var movieRequest = new BookRequest();
        movieRequest.setTitle("Inception");
        movieRequest.setGenre("SCIENCE_FICTION");
        movieRequest.setPublicationYear(2010);
        movieRequest.setAuthor("Christopher Nolan");
        movieRequest.setTags(Arrays.asList("Leonardo DiCaprio", "Joseph Gordon-Levitt"));

        var movie = bookMapper.toEntity(movieRequest);

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(movie).isNotNull();
            soft.assertThat(movie.getId()).isNotNull();
            soft.assertThat(movie.getTitle()).isEqualTo(movieRequest.getTitle());
            soft.assertThat(movie.getGenre()).isEqualTo(BookGenre.valueOf(movieRequest.getGenre()));
            soft.assertThat(movie.getPublicationYear()).isEqualTo(movieRequest.getPublicationYear());
            soft.assertThat(movie.getAuthor()).isEqualTo(movieRequest.getAuthor());
            soft.assertThat(movie.getTags()).isEqualTo(movieRequest.getTags());
        });
    }
}
