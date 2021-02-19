package de.bibbuddy;

public class Author {
  private Long id;
  private String firstName;
  private String lastName;
  private String title;
  private Integer createDate;
  private Integer modDate;


  /**
   * The Author class maps the data of the database from the table Author.
   *
   * @author Sarah Kurek
   *
   * @param id         id of the author
   * @param firstName  first name of the author of the book
   * @param lastName   last name of the author of the book
   * @param title      book title
   * @param createDate date on which author was added to the database
   * @param modDate    date on which author was last modified in the database
   */
  public Author(Long id, String firstName, String lastName, String title, Integer createDate,
                Integer modDate) {
    this.id = id;
    this.firstName = firstName;
    this.lastName = lastName;
    this.title = title;
    this.createDate = createDate;
    this.modDate = modDate;
  }

  public Author() {
  }

  public Author(String firstName, String lastName) {
    this.firstName = firstName;
    this.lastName = lastName;
  }

  /**
   * Constructor for optional title.
   *
   * @param firstName first name of the author of the book
   * @param lastName last name of the author of the book
   * @param title book title
   */
  public Author(String firstName, String lastName, String title) {
    this.firstName = firstName;
    this.lastName = lastName;
    this.title = title;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
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
}
