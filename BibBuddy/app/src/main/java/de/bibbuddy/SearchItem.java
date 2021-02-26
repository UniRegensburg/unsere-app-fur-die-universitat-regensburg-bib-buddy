package de.bibbuddy;

/**
 * The SearchItem is responsible for holding the information of the search view items.
 * It is a subclass of the LibraryItem class.
 *
 * @author Claudia Schönherr
 */
public class SearchItem extends LibraryItem {
  private Integer modDate;
  private SearchItemType itemType; // shelf, book, textNote

  public SearchItem(String name, int image, Long id) {
    super(name, image, id);
  }

  public SearchItem(String name, int image, Long id, Long parentId) {
    super(name, image, id, parentId);
  }

  /**
   * Constructor of a SearchItem.
   *
   * @param name     name of the SearchItem
   * @param image    image of the SearchItem
   * @param id       id of the SearchItem
   * @param modDate modification date of the SearchItem
   * @param itemType type of the SearchItem
   */
  public SearchItem(String name, int image, Long id, Integer modDate, SearchItemType itemType) {
    super(name, image, id);
    this.modDate = modDate;
    this.itemType = itemType;
  }

  public Integer getModDate() {
    return modDate;
  }

  public SearchItemType getItemType() {
    return itemType;
  }
}
