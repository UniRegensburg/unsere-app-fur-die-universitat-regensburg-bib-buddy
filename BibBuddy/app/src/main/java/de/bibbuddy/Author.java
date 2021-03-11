package de.bibbuddy;

import java.util.Objects;

public class Author {
  private Long id;
  private String firstName;
  private String lastName;
  private String title;
  private Long createDate;
  private Long modDate;
  private Author cache;

  private static Long copyLong(Long number) {
    if (number == null) {
      return null;
    }

    return Long.valueOf(number);
  }

  public Author() {
  }

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
  public Author(Long id, String firstName, String lastName, String title,
                Long createDate, Long modDate) {
    this(firstName, lastName);

    this.id = id;
    this.title = title;
    this.createDate = createDate;
    this.modDate = modDate;
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

  /**
   * Method to check if author object is completely empty.
   */
  public boolean isEmpty() {
    return firstName == null
        && lastName == null
        && title == null;
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

  public Long getModDate() {
    return modDate;
  }

  public void setModDate(Long modDate) {
    this.modDate = modDate;
  }

  public Author clone() {

    return new Author(copyLong(id), firstName, lastName, title, copyLong(createDate), copyLong(modDate));
  }

  public Author getCache() {
    return cache;
  }

  public void setCache() {
    cache = clone();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    Author author = (Author) obj;
    return Objects.equals(id, author.id)
        && Objects.equals(firstName, author.firstName)
        && Objects.equals(lastName, author.lastName)
        && Objects.equals(title, author.title)
        && Objects.equals(createDate, author.createDate)
        && Objects.equals(modDate, author.modDate);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, firstName, lastName, title, createDate, modDate);
  }

}
