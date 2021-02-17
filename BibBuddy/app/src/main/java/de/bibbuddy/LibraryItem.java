package de.bibbuddy;

/**
 * The LibraryItem is responsible for holding the information of the book view items.
 * It is the parent of the other .*Item classes.
 *
 * @author Claudia Schönherr
 */
public class LibraryItem {

  private final String name;
  private final int image;
  private final Long id;
  private final Long parentId;

  public LibraryItem(String name, int image, Long id) {
    this(name, image, id, null);
  }

  /**
   * Constructor of a LibraryItem.
   *
   * @param name     name of the LibraryItem
   * @param image    image of the LibraryItem
   * @param id       id of the LibraryItem
   * @param parentId parentId of the LibraryItem
   * @author Claudia Schönherr
   */
  public LibraryItem(String name, int image, Long id, Long parentId) {
    this.name = name;
    this.image = image;
    this.id = id;
    this.parentId = parentId;
  }

  public String getName() {
    return name;
  }

  public int getImage() {
    return image;
  }

  public Long getId() {
    return id;
  }

  public Long getParentId() {
    return parentId;
  }

}
