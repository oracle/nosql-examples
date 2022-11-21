package graphql;

import java.util.Objects;
import java.util.List;

import com.oracle.nosql.spring.data.core.mapping.NosqlId;
import com.oracle.nosql.spring.data.core.mapping.NosqlTable;

@NosqlTable(storageGB = 1, writeUnits = 10, readUnits = 10)
class Tutorial  {

  @NosqlId(generated = true)
  private Long id;
  private String title;
  private String description;
  private List<String> keywords;
  private Author author ;

  public Tutorial() {
  }
  public Tutorial(String title, String description, List<String> keywords, Author author) {
    this.title = title;
    this.description = description;
    this.keywords = keywords;
    this.author = author;
  }
  public Long getId() {
    return id;
  }
  public String getTitle() {
    return title;
  }
  public void setTitle(String title) {
    this.title = title;
  }
  public String getDescription() {
    return description;
  }
  public void setDescription(String description) {
    this.description = description;
  }
  public List<String> getKeywords() {
    return keywords;
  }
  public void setKeywords(List<String> keywords) {
    this.keywords = keywords;
  }
  public Author getAuthor() {
    return author;
  }
  public void setAuthor(Author author) {
    this.author = author;
  }
  @Override
  public String toString() {
    return "Tutorial [id=" + id + ", title=" + title + ", description=" + description + "keywords=" + keywords + ", author=" + author + "]";
  }

}
