package expert.os.demos.books.application;

import expert.os.demos.books.api.BookRequest;
import expert.os.demos.books.api.BookResponse;
import expert.os.demos.books.domain.Book;
import expert.os.demos.books.domain.BookRepository;
import jakarta.data.Order;
import jakarta.data.Sort;
import jakarta.data.page.Page;
import jakarta.data.page.PageRequest;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.jnosql.mapping.Database;
import org.eclipse.jnosql.mapping.DatabaseType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@ApplicationScoped
public class BookService {

    private static final Logger LOGGER = Logger.getLogger(BookService.class.getName());
    public static final Order<Book> ORDER = Order.by(Sort.asc("title"));
    private final BookRepository bookRepository;

    private final BookMapper bookMapper;

    @Inject
    public BookService(@Database(DatabaseType.DOCUMENT) BookRepository bookRepository, BookMapper bookMapper) {
        this.bookRepository = bookRepository;
        this.bookMapper = bookMapper;
    }

    public BookService() {
        this(null, null);
    }

    public List<BookResponse> getAllBooks(int page, int size) {
        LOGGER.log(Level.INFO, "Fetching all books with page: {0} and size: {1}", new Object[]{page, size});
        PageRequest pageable = PageRequest.ofPage(page).size(size);

        Page<Book> bookPage = bookRepository.findAll(pageable, ORDER);
        List<BookResponse> books = bookPage.stream()
                .map(bookMapper::toResponse)
                .collect(Collectors.toList());
        LOGGER.log(Level.INFO, "Retrieved {0} books", books.size());
        return books;
    }

    public Optional<BookResponse> findById(String id) {
        LOGGER.log(Level.INFO, "Fetching book with ID: {0}", id);
        Optional<BookResponse> bookResponse = bookRepository.findById(id)
                .map(bookMapper::toResponse);
        if (bookResponse.isPresent()) {
            LOGGER.log(Level.INFO, "Book found with ID: {0}", id);
        } else {
            LOGGER.log(Level.WARNING, "Book not found with ID: {0}", id);
        }
        return bookResponse;
    }

    public BookResponse create(BookRequest bookRequest) {
        LOGGER.log(Level.INFO, "Creating new book with title: {0}", bookRequest.getTitle());
        Book book = bookMapper.toEntity(bookRequest, UUID.randomUUID().toString());
        Book savedBook = bookRepository.save(book);
        LOGGER.log(Level.INFO, "Book created with ID: {0}", savedBook.getId());
        return bookMapper.toResponse(savedBook);
    }

    public Optional<BookResponse> update(String id, BookRequest bookRequest) {
        LOGGER.log(Level.INFO, "Updating book with ID: {0}", id);
        return bookRepository.findById(id)
                .map(existingBook -> {
                    Book updatedBook = bookMapper.toEntity(bookRequest);
                    existingBook.update(updatedBook);
                    Book savedBook = bookRepository.save(updatedBook);
                    LOGGER.log(Level.INFO, "Book updated with ID: {0}", id);
                    return bookMapper.toResponse(savedBook);
                });
    }

    public void delete(String id) {
        LOGGER.log(Level.INFO, "Deleting book with ID: {0}", id);
        bookRepository.deleteById(id);
    }
}
