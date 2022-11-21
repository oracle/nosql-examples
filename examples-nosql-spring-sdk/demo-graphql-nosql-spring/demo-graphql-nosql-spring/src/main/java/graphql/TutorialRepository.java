package graphql;

import com.oracle.nosql.spring.data.repository.NosqlRepository;
import com.oracle.nosql.spring.data.repository.Query;
import org.springframework.data.repository.query.Param;


interface TutorialRepository  extends NosqlRepository<Tutorial, Long> {
   Iterable<Tutorial> findByDescriptionRegex (String regexp);
   Iterable<Tutorial> findByAuthorNameRegex (String regexp);

    @Query("declare $p_keywords String; " +
        "SELECT * FROM Tutorial as t " +
        "WHERE t.kv_json_.keywords[] =any $p_keywords ")
   Iterable<Tutorial> findByKeywords (
    @Param("$p_keywords") String keyword
   );
}
