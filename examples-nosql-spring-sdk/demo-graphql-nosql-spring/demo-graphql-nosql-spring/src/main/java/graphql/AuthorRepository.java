package graphql
import org.springframework.data.jpa.repository.JpaRepository;
import graphql.Author;
public interface AuthorRepository extends JpaRepository<Author, Long> {
}
