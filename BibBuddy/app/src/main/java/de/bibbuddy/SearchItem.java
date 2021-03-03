package de.bibbuddy;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * The SearchItem is responsible for holding the information of the search view items.
 * It is a subclass of the LibraryItem class.
 *
 * @author Claudia Schönherr
 */
public class SearchItem extends LibraryItem {
  private Long modDate;
  private String modDateStr;
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
   * @param modDate  modification date of the SearchItem
   * @param itemType type of the SearchItem
   */
  public SearchItem(String name, int image, Long id, Long modDate, SearchItemType itemType) {
    super(name, image, id);
    this.modDate = modDate;
    this.modDateStr = setModDateStr(modDate);
    this.itemType = itemType;
  }

  private String setModDateStr(Long date) {
    Date d = new Date(date);
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");

    String dateStr = simpleDateFormat.format(d);
    String day = dateStr.substring(8, 10);
    String month = dateStr.substring(5, 7);
    String year = dateStr.substring(0, 4);
    String time = dateStr.substring(11, 16);

    dateStr = day + "." + month + "." + year + " " + time + " Uhr";

    return dateStr;
  }

  public Long getModDate() {
    return modDate;
  }

  public SearchItemType getItemType() {
    return itemType;
  }

  public String getModDateStr() {
    return modDateStr;
  }
}
