package graphql;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import graphql.Tutorial;
import graphql.TutorialRepository;
import com.coxautodev.graphql.tools.*;

@Component
public class Mutation implements GraphQLMutationResolver {
  @Autowired
  private TutorialRepository tutorialRepository;
  public Mutation(TutorialRepository tutorialRepository) {
    this.tutorialRepository = tutorialRepository;
  }
  public Tutorial createTutorial(String title, String description, List<String> keywords, Author author) {
    Tutorial tutorial = new Tutorial();
    tutorial.setTitle(title);
    tutorial.setDescription(description);
    tutorial.setKeywords(keywords);
    tutorial.setAuthor(author);
    tutorialRepository.save(tutorial);
    return tutorial;
  }
}

