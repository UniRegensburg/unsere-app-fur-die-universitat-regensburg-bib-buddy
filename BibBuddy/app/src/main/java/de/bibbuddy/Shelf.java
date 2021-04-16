package de.bibbuddy;

/**
 * The Shelf class maps the data of the database from the table Shelf.
 *
 * @author Sarah Kurek
 */
public class Shelf {

  private Long id;
  private final String name;

  @SuppressWarnings({"unused", "FieldCanBeLocal"})
  // Can be used in the future and is here only to check for possible bugs
  private Long createDate;

  private Long modDate;
  private Long shelfId;

  /**
   * Constructor with all its variables.
   *
   * @param id         id of the shelf
   * @param name       name of the shelf
   * @param createDate date on which shelf was added to the database
   * @param modDate    date on which shelf was last modified in the database
   * @param shelfId    id of parent shelf
   */
  public Shelf(Long id, String name, Long createDate, Long modDate, Long shelfId) {
    this.id = id;
    this.name = name;
    this.createDate = createDate;
    this.modDate = modDate;
    this.shelfId = shelfId;
  }

  /**
   * Constructor without id, create_date and mod_date, because it's automatically set in the
   * database.
   *
   * @param name    name of the shelf
   * @param shelfId id of parent shelf
   */
  public Shelf(String name, Long shelfId) {
    this.name = name;
    this.shelfId = shelfId;
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

  public Long getModDate() {
    return modDate;
  }

  public Long getShelfId() {
    return shelfId;
  }

  public void setShelfId(Long shelfId) {
    this.shelfId = shelfId;
  }

}
