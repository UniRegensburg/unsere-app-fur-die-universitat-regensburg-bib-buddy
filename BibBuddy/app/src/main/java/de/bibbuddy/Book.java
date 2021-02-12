package de.bibbuddy;

public class Book {

  private Long id;
  private String isbn;
  private String title;
  private String subtitle;
  private Integer pubYear;
  private String publisher;
  private String volume;
  private String edition;
  private String addInfo;
  private Integer createDate;
  private Integer modDate;

  /**
   * DUMMY COMMENT - PLEASE ADJUST.
   *
   * @param id         test
   * @param isbn       test
   * @param title      test
   * @param subtitle   test
   * @param pubYear    test
   * @param publisher  test
   * @param volume     test
   * @param edition    test
   * @param addInfo    test
   * @param createDate test
   * @param modDate    test
   */
  public Book(Long id, String isbn, String title, String subtitle, Integer pubYear,
              String publisher, String volume, String edition, String addInfo,
              Integer createDate, Integer modDate) {
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
   * DUMMY COMMENT - PLEASE ADJUST.
   * // id, creation_date and mod_date are set in the database
   *
   * @param isbn      test
   * @param title     test
   * @param subtitle  test
   * @param pubYear   test
   * @param publisher test
   * @param volume    test
   * @param edition   test
   * @param addInfo   test
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
