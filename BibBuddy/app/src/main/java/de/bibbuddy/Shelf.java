package de.bibbuddy;

/**
 * The Shelf class maps the data of the database from the table Shelf.
 *
 * @author Sarah Kurek
 */
public class Shelf {
  private Long id;
  private String name;
  private Integer createDate;
  private Integer modDate;
  private Long shelfId;

  /**
   * Constructor with all its variables.
   *
   * @param id test
   * @param name test
   * @param createDate test
   * @param modDate test
   * @param shelfId test
   */
  public Shelf(Long id, String name, Integer createDate, Integer modDate, Long shelfId) {
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
   * @param name test
   * @param shelfId test
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

  public Integer getCreateDate() {
    return createDate;
  }

  public void setCreateDate(Integer createDate) {
    this.createDate = createDate;
  }

  public Integer getModDate() {
    return modDate;
  }

  public void setModDate(Integer modDate) {
    this.modDate = modDate;
  }

  public Long getShelfId() {
    return shelfId;
  }

  public void setShelfId(Long shelfId) {
    this.shelfId = shelfId;
  }
}
