package graphql;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import graphql.Author;
import graphql.Tutorial;
import graphql.AuthorRepository;
import cgraphql.TutorialRepository;
import com.coxautodev.graphql.tools.GraphQLQueryResolver;
@Component
public class Query implements GraphQLQueryResolver {
  private AuthorRepository authorRepository;
  private TutorialRepository tutorialRepository;
  @Autowired
  public Query(AuthorRepository authorRepository, TutorialRepository tutorialRepository) {
    this.authorRepository = authorRepository;
    this.tutorialRepository = tutorialRepository;
  }
  public Iterable<Author> findAllAuthors() {
    return authorRepository.findAll();
  }
  public Iterable<Tutorial> findAllTutorials() {
    return tutorialRepository.findAll();
  }
  public long countAuthors() {
    return authorRepository.count();
  }
  public long countTutorials() {
    return tutorialRepository.count();
  }
}
