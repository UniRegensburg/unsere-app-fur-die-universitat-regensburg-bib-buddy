package de.bibbuddy;

/**
 * The AuthorItem is responsible for holding the information of the author view items.
 * It is a child of LibraryItem.
 *
 * @author Sarah Kurek
 */
public class AuthorItem {

  private final int image;
  private final Author author;

  /**
   * Constructor of an AuthorItem.
   *
   * @param author the Author
   */
  public AuthorItem(Author author) {
    this.image = R.drawable.ic_person;
    this.author = author;
  }

  public String getFirstName() {
    return author.getFirstName();
  }

  public String getLastName() {
    return author.getLastName();
  }

  public Long getId() {
    return author.getId();
  }

  public Long getModDate() {
    return author.getModDate();
  }

  public int getImage() {
    return image;
  }

  public String getTitle() {
    return author.getTitle();
  }

  public Author getAuthor() {
    return author;
  }

}
