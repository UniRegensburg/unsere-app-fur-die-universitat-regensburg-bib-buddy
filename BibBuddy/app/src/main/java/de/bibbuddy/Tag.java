package de.bibbuddy;

/**
 * The Tag class maps the data of the database from the table Tag.
 *
 * @author Sarah Kurek
 */
@SuppressWarnings("ALL")
// Can be removed when it is used in the future
public class Tag {

  private Long id;
  private String name;

  public Tag(Long id, String name) {
    this.id = id;
    this.name = name;
  }

  public Tag() {
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

}
