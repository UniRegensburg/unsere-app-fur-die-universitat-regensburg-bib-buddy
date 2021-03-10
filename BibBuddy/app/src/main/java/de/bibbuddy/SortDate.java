package de.bibbuddy;

import java.util.Comparator;

/**
 * The SortDate class is responsible for sorting the list by name.
 *
 * @author Claudia Schönherr
 */
public class SortDate implements Comparator<LibraryItem> {

  @Override
  public int compare(LibraryItem libraryItem, LibraryItem libraryItemToCompare) {
    if (libraryItem.getModDate() == null || libraryItemToCompare.getModDate() == null) {
      return 0;
    }

    return libraryItem.getModDate().compareTo(libraryItemToCompare.getModDate());
  }

}
