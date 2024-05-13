package com.oracle.nosql.springdatarestnosql;

import java.util.Objects;
import java.util.Map;
import java.util.List;

import com.oracle.nosql.spring.data.core.mapping.NosqlId;
import com.oracle.nosql.spring.data.core.mapping.NosqlTable;
import com.oracle.nosql.spring.data.core.mapping.NosqlCapacityMode;

@NosqlTable(autoCreateTable=true, capacityMode=NosqlCapacityMode.PROVISIONED , writeUnits = 10, readUnits = 10, storageGB = 1)
// https://docs.oracle.com/en/database/other-databases/nosql-database/23.3/springsdk/persistence-model.html
// tableName: If empty, then the entity class name will be used. 
// ttl/ttlUnit: Sets the default table level Time to Live (TTL) when the table is created. The TTL allows the automatic expiration of table rows after the elapse of the specified duration. 
// capacityMode: A table is created with either Provisioned Capacity or On-Demand Capacity. 
// Set capacityMode to ON_DEMAND and storageGB to a value greater than 0. 
// Set capacityMode to PROVISIONED and all three TableLimits: readUnits, writeUnits, and storageGB to values greater than 0.
//
class Movie {

  @NosqlId(generated = true)
  public String   movie_id;
  public String title;
  public List<String> genre;
  public List<Crew> crew;
  public Map<String, Object> data;

  Movie() {}

  Movie(String title,  List<String> genre, List<Crew> crew,  Map<String, Object> data) {
    this.title = title;
    this.genre = genre;
    this.crew = crew;
    this.data = data;
  }

  @Override
  public boolean equals(Object o) {

    if (this == o)
      return true;
    if (!(o instanceof Movie))
      return false;
    Movie movie = (Movie) o;
    return Objects.equals(this.movie_id, movie.movie_id) && Objects.equals(this.title, movie.title) ;
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.movie_id, this.title);
  }

  @Override
  public String toString() {
    return "Movie{" + "id=" + this.movie_id + ", name='" + this.title +  '}';
  }
}
