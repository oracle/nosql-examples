package expert.os.demos.books.api;


import expert.os.demos.books.application.BookService;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.List;
import java.util.Optional;


@RequestScoped
@Path("/books")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Books", description = "Operations related to books")
public class BookResource {


    private final BookService bookService;

    @Inject
    public BookResource(BookService bookService) {
        this.bookService = bookService;
    }

    public BookResource() {
        this(null);
    }


    @GET
    @Operation(summary = "Retrieve all books", description = "Returns a paginated list of all books.")
    @APIResponse(responseCode = "200", description = "List of books", content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = BookResponse.class)))
    public Response getAllBooks(
            @Parameter(description = "Page number for pagination", example = "1")
            @QueryParam("page") @DefaultValue("1") int page,
            @Parameter(description = "Number of items per page", example = "10")
            @QueryParam("size") @DefaultValue("10") int size) {
        List<BookResponse> books = bookService.getAllBooks(page, size);
        return Response.ok(books).build();
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Retrieve a book by ID", description = "Returns a book for the specified ID.")
    @APIResponse(responseCode = "200", description = "Book details", content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = BookResponse.class)))
    @APIResponse(responseCode = "404", description = "Book not found")
    public Response getBookById(
            @Parameter(description = "ID of the book to retrieve", required = true)
            @PathParam("id") String id) {
        Optional<BookResponse> bookResponse = bookService.findById(id);
        return bookResponse.map(Response::ok)
                .orElse(Response.status(Response.Status.NOT_FOUND))
                .build();
    }

    @POST
    @Operation(summary = "Create a new book", description = "Creates a new book in the system.")
    @APIResponse(responseCode = "201", description = "Book created", content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = BookResponse.class)))
    public Response createBook(
            @Parameter(description = "Book data to be created", required = true)
            BookRequest bookRequest) {
        BookResponse bookResponse = bookService.create(bookRequest);
        return Response.status(Response.Status.CREATED).entity(bookResponse).build();
    }

    @PUT
    @Path("/{id}")
    @Operation(summary = "Update a book", description = "Updates an existing book by ID.")
    @APIResponse(responseCode = "200", description = "Book updated", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BookResponse.class)))
    @APIResponse(responseCode = "404", description = "Book not found")
    public Response updateBook(
            @Parameter(description = "ID of the book to update", required = true)
            @PathParam("id") String id,
            @Parameter(description = "Updated book data", required = true)
            BookRequest bookRequest) {
        Optional<BookResponse> updatedBook = bookService.update(id, bookRequest);
        return updatedBook.map(Response::ok)
                .orElse(Response.status(Response.Status.NOT_FOUND))
                .build();
    }

    @DELETE
    @Path("/{id}")
    @Operation(summary = "Delete a book", description = "Deletes a book by ID.")
    @APIResponse(responseCode = "204", description = "Book deleted")
    @APIResponse(responseCode = "404", description = "Book not found")
    public Response deleteBook(
            @Parameter(description = "ID of the book to delete", required = true)
            @PathParam("id") String id) {
        bookService.delete(id);
        return Response.noContent().build();
    }
}
