package expert.os.demos.books.application;

import expert.os.demos.books.api.BookRequest;
import expert.os.demos.books.api.BookResponse;
import expert.os.demos.books.domain.Book;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "cdi")
public interface BookMapper {

    BookResponse toResponse(Book book);

    Book toEntity(BookRequest bookRequest);

    @Mapping(target = "id", source = "id")
    Book toEntity(BookRequest bookRequest, String id);

}
