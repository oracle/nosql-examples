package graphql;

import org.springframework.data.jpa.repository.JpaRepository;
import graphql.Tutorial;
public interface TutorialRepository extends JpaRepository<Tutorial, Long> {
}
