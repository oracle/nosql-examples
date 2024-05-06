package com.oracle.nosql.springdatarestnosql;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Objects;
import java.util.HashMap;
import java.util.ArrayList;


@Configuration
class LoadDatabase {

  private static final Logger log = LoggerFactory.getLogger(LoadDatabase.class);

  @Bean
  CommandLineRunner initDatabase(MovieRepository repository) {

    ArrayList<String> genre = new ArrayList<String>();
    genre.add("Fiction");
    genre.add("Action");
    genre.add("Thriller");

    ArrayList<String> cast = new ArrayList<String>();
    cast.add("Martin Freeman");
    cast.add("Ian McKellen");

    ArrayList<Crew> crew = new ArrayList<Crew>();
    crew.add(new Crew( new ArrayList<String>() {{
      add("Peter Jackson");
    }}, "director") );
    crew.add(new Crew( new ArrayList<String>() {{
      add("J.R.R. Tolkien");
      add("Fran Walsh");
      add("Philippa Boyens");
    }}, "writter") );

    HashMap data = new HashMap<>();
    data.put("cast", cast);
    data.put("theCrewClone", crew);
    data.put("views", 0);
    data.put("price", null);

    repository.deleteAll();
    
    return args -> {
      log.info("Preloading " + repository.save(new Movie("Lord of Rings", genre, crew , data)));
      log.info("Preloading " + repository.save(new Movie("Hobbit", genre, crew, data)));
    };
  }
}
