package de.bibbuddy;

/**
 * The AuthorItem is responsible for holding the information of the author view items.
 * It is a subclass of the LibraryItem class.
 *
 * @author Sarah Kurek
 */
public class AuthorItem {
  private final String firstName;
  private final String lastName;
  private final Long id;
  private final Long modDate;
  private final int image;
  private final String title;


  /**
   * Constructor of a AuthorItem.
   *
   * @param firstName  first name of the Author
   * @param lastName   last name of the Author
   * @param id          id of the AuthorItem
   * @param modDate     modification date of the AuthorItem
   * @param title       title of the Auhor
   */
  public AuthorItem(String firstName, String lastName, Long id,
                    Long modDate, String title) {

    this.firstName = firstName;
    this.lastName = lastName;
    this.id = id;
    this.modDate = modDate;
    this.image = R.drawable.ic_person;
    this.title = title;
  }

  public String getFirstName() {
    return firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public Long getId() {
    return id;
  }

  public Long getModDate() {
    return modDate;
  }

  public int getImage() {
    return image;
  }

  public String getTitle() {
    return title;
  }
}
