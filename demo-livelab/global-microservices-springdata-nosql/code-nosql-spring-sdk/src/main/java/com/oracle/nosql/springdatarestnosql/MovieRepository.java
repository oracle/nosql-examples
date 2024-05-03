package com.oracle.nosql.springdatarestnosql;

import com.oracle.nosql.spring.data.repository.NosqlRepository;
import org.springframework.data.repository.query.Param;
import com.oracle.nosql.spring.data.repository.Query;

import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;

@RepositoryRestResource(collectionResourceRel = "movie", path = "movie")
interface MovieRepository extends NosqlRepository<Movie, String> {
    List<Movie> findByTitle(@Param("title") String title);
    List<Movie> findByTitleRegex(@Param("title") String title);
    List<Movie> findByTitleIgnoreCase(@Param("title") String title);
    List<Movie> findByTitleBetween(@Param("title") String title, @Param("title2") String title2);
    List<Movie> findByTitleIn(List<Object> titles);

    //@Query(value = "DECLARE $genre STRING; SELECT * FROM movie AS t WHERE t.kv_json_.genre[] =any $genre")
    @Query(value = "DECLARE $genre STRING; SELECT * FROM movie AS t WHERE exists t.kv_json_.genre[$element = $genre] ")
    List<Movie> findByGenreDV(@Param("$genre") String $genre);
    @Query(value = "DECLARE $genre ARRAY(STRING); SELECT * FROM movie AS t WHERE t.kv_json_.genre =any $genre")
    List<Movie> findByGenreDV2(@Param("$genre") List<String> $genre);
    //@Query(value = "DECLARE $genre STRING; SELECT * FROM movie AS t WHERE t.kv_json_.genre = $genre")
    List<Movie> findByGenre  (@Param("genre")  String  genre);
    List<Movie> findByGenreRegex  (@Param("genre")  String  genre);
    List<Movie> findByGenreIgnoreCase  (@Param("genre")  String  genre);
    List<Movie> findByGenreIn  (@Param("genre")  List<Object>  genres);


    @Query(value = "DECLARE $name STRING; SELECT * FROM Movie AS t WHERE t.kv_json_.crew.names[] =any $name")
    List<Movie> findByCrewNamesDV(@Param("$name") String name);

    List<Movie> findByCrewNames(@Param("name") String name);
    List<Movie> findByCrewNamesIn(@Param("$p_crew_names") List<Object> $p_crew_names);
    List<Movie> findByCrewJobAndCrewNamesIn(@Param("job") String job, @Param("names") List<String> names);
    @Query(value = "declare $p_crew_job String; $p_crew_names ARRAY(String); select * from Movie as t where exists t.kv_json_.crew[$element.job =  $p_crew_job AND exists $element.names[$element in ($p_crew_names[])] ]")
    List<Movie> findByCrewJobAndCrewNamesInDV(@Param("$p_crew_job") String job, @Param("$p_crew_names") List<String> names);

    @Query(value = "DECLARE $views Integer; SELECT * FROM movie AS t WHERE t.kv_json_.data.views =$views")
    List<Movie> findByDataViewsDV(@Param("$views") int $views);
/*
    List<Movie> findByDataViews(@Param("views") int views);
*/
}
