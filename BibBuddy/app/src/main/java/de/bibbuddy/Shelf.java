package de.bibbuddy;

/**
 * The Shelf class maps the data of the database from the table Shelf.
 *
 * @author Sarah Kurek
 */
public class Shelf {
  private Long id;
  private String name;
  private Long createDate;
  private Long modDate;
  private Long shelfId;

  /**
   * Constructor with all its variables.
   *
   * @param id id of the shelf
   * @param name name of the shelf
   * @param createDate date on which shelf was added to the database
   * @param modDate date on which shelf was last modified in the database
   * @param shelfId id of parent shelf
   */
  public Shelf(Long id, String name, Long createDate, Long modDate, Long shelfId) {
    this.id = id;
    this.name = name;
    this.createDate = createDate;
    this.modDate = modDate;
    this.shelfId = shelfId;
  }

  public Shelf() {
  }

  /**
   * Constructor without id, create_date and mod_date, because its automatically set in the
   * database.
   *
   * @param name name of the shelf
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

  public void setName(String name) {
    this.name = name;
  }

  public Long getCreateDate() {
    return createDate;
  }

  public void setCreateDate(Long createDate) {
    this.createDate = createDate;
  }

  public Long getModDate() {
    return modDate;
  }

  public void setModDate(Long modDate) {
    this.modDate = modDate;
  }

  public Long getShelfId() {
    return shelfId;
  }

  public void setShelfId(Long shelfId) {
    this.shelfId = shelfId;
  }
}
