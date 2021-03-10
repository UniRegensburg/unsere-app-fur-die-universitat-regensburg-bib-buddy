package de.bibbuddy;

/**
 * The LibraryItem is responsible for holding the information of the book view items.
 * It is the parent of the other .*Item classes.
 *
 * @author Claudia Schönherr
 */
public class LibraryItem {

  private final int image;
  private final Long id;
  private final Long parentId;
  private String name;

  private Long modDate;
  private String modDateStr;

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
   */
  public LibraryItem(String name, int image, Long id, Long parentId) {
    this.name = name;
    this.image = image;
    this.id = id;
    this.parentId = parentId;
  }

  /**
   * Constructor of a LibraryItem.
   *
   * @param name    name of the LibraryItem
   * @param image   image of the LibraryItem
   * @param id      id of the LibraryItem
   * @param modDate modification date of the LibraryItem
   */
  public LibraryItem(String name, int image, Long id, Long parentId, Long modDate) {
    this.name = name;
    this.image = image;
    this.id = id;
    this.parentId = parentId;

    this.modDate = modDate;
    this.modDateStr = DateConverter.convertDateToString(modDate);
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
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

  public Long getModDate() {
    return modDate;
  }

  public String getModDateStr() {
    return modDateStr;
  }

}
