package graphql;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import graphql.Author;
import java.util.List;
import java.util.ArrayList;


@Configuration
class LoadDatabase {

  private static final Logger log = LoggerFactory.getLogger(LoadDatabase.class);

  @Bean
  CommandLineRunner initDatabase(TutorialRepository repository) {

    String[] keys =  {"spring", "tutorial", "nosql", "oracle", "cloud"};
    List<String> keywords=new ArrayList<String>();  
    for(String key:keys){  
      keywords.add(key);  
    }  
    Author author = new Author("Dario VEGA");

    repository.deleteAll();
    return args -> {
      log.info("Preloading " + repository.save(new Tutorial("Spring REST", "This tutorial will focus on how you can use Oracle NoSQL Spring Data for a flexible and elastic database that scales with your application. We start with the spring boot's standard tutorial - the in memory H2 database, and then move over to Oracle NoSQL Cloud Services.", keywords, author)));
      log.info("Preloading " + repository.save(new Tutorial("Spring GraphQL", "This tutorial will focus on how you can use Oracle NoSQL Spring Data for a flexible and elastic database that scales with your GraphQL application.", keywords, author)));
    };
  }
}

