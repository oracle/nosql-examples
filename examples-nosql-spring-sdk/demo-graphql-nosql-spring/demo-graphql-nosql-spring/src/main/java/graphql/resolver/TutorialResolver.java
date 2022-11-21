package graphql;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import graphql.Tutorial;
import com.coxautodev.graphql.tools.GraphQLResolver;
@Component
public class TutorialResolver implements GraphQLResolver<Tutorial> {
  @Autowired
  public TutorialResolver() {
  }
}

