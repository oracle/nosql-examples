package graphql;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import graphql.Tutorial;
import graphql.TutorialRepository;
import com.coxautodev.graphql.tools.*;
import java.lang.Iterable;


@Component
public class Query implements GraphQLQueryResolver {
  @Autowired
  private TutorialRepository tutorialRepository;
  public Query(TutorialRepository tutorialRepository) {
    this.tutorialRepository = tutorialRepository;
  }
  public Iterable<Tutorial> findAllTutorials() {
    return tutorialRepository.findAll();
  }
  public Iterable<Tutorial> findByDescriptionRegex(String regexp) {
    return tutorialRepository.findByDescriptionRegex(regexp);
  }
  public Iterable<Tutorial> findByAuthorNameRegex(String regexp) {
    return tutorialRepository.findByAuthorNameRegex(regexp);
  }
  public Iterable<Tutorial> findByKeywords (String keyword) {
    return tutorialRepository.findByKeywords(keyword);

  }
}

