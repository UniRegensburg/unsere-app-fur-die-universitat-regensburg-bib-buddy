package de.bibbuddy;

import org.jsoup.Jsoup;

/**
 * The SearchItem is responsible for holding the information of the search view items.
 * It is a subclass of the LibraryItem class.
 *
 * @author Claudia Schönherr
 */
public class SearchItem extends LibraryItem {

  private final SearchItemType itemType;
  private final String displayName;

  /**
   * Constructor of a SearchItem.
   *
   * @param name     name of the SearchItem
   * @param image    image of the SearchItem
   * @param id       id of the SearchItem
   * @param modDate  modification date of the SearchItem
   * @param itemType type of the SearchItem
   */
  public SearchItem(String name, int image, Long id, Long modDate, SearchItemType itemType) {
    super(name, image, id, null, modDate);

    this.itemType = itemType;

    String itemName = Jsoup.parse(name).text();
    if (itemName.length() > 25) {
      itemName = itemName.substring(0, 25) + " ...";
    }

    this.displayName = itemName;
  }

  public SearchItemType getItemType() {
    return itemType;
  }

  public String getDisplayName() {
    return displayName;
  }

}
