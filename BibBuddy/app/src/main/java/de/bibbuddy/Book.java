package de.bibbuddy;

/**
 * The Book class maps the data of the database from the table Book.
 *
 * @author Sarah Kurek
 */
public class Book {

  private Long id = 0L;
  private String isbn = "";
  private String title = "";
  private String subtitle = "";
  private Integer pubYear;
  private String publisher = "";
  private String volume = "";
  private String edition = "";
  private String addInfo = "";
  private Long createDate;
  private Long modDate;

  /**
   * Constructor with all its parameters.
   *
   * @param id         id of the book
   * @param isbn       isbn of the book
   * @param title      book title
   * @param subtitle   book subtitle
   * @param pubYear    publication year of the book
   * @param publisher  publisher of the book
   * @param volume     volume of the book
   * @param edition    edition of the book
   * @param addInfo    additional information to the book
   * @param createDate date on which book was added to the database
   * @param modDate    date on which book was last modified in the database
   */
  public Book(Long id, String isbn, String title, String subtitle, Integer pubYear,
              String publisher, String volume, String edition, String addInfo,
              Long createDate, Long modDate) {
    this.id = id;
    this.isbn = isbn;
    this.title = title;
    this.subtitle = subtitle;
    this.pubYear = pubYear;
    this.publisher = publisher;
    this.volume = volume;
    this.edition = edition;
    this.addInfo = addInfo;
    this.createDate = createDate;
    this.modDate = modDate;
  }


  /**
   * Constructor without the parameters id, creation_date and mod_date, because they are
   * automatically set in the database.
   *
   * @param isbn      isbn of the book
   * @param title     book title
   * @param subtitle  book subtitle
   * @param pubYear   publication year of the book
   * @param publisher publisher of the book
   * @param volume    volume of the book
   * @param edition   edition of the book
   * @param addInfo   additional information to the book
   */
  public Book(String isbn, String title, String subtitle, Integer pubYear,
              String publisher, String volume, String edition, String addInfo) {
    this.isbn = isbn;
    this.title = title;
    this.subtitle = subtitle;
    this.pubYear = pubYear;
    this.publisher = publisher;
    this.volume = volume;
    this.edition = edition;
    this.addInfo = addInfo;
  }

  public Book() {
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getIsbn() {
    return isbn;
  }

  public void setIsbn(String isbn) {
    this.isbn = isbn;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getSubtitle() {
    return subtitle;
  }

  public void setSubtitle(String subtitle) {
    this.subtitle = subtitle;
  }

  public Integer getPubYear() {
    return pubYear;
  }

  public void setPubYear(Integer pubYear) {
    this.pubYear = pubYear;
  }

  public String getPublisher() {
    return publisher;
  }

  public void setPublisher(String publisher) {
    this.publisher = publisher;
  }

  public String getVolume() {
    return volume;
  }

  public void setVolume(String volume) {
    this.volume = volume;
  }

  public String getEdition() {
    return edition;
  }

  public void setEdition(String edition) {
    this.edition = edition;
  }

  public String getAddInfo() {
    return addInfo;
  }

  public void setAddInfo(String addInfo) {
    this.addInfo = addInfo;
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

}
