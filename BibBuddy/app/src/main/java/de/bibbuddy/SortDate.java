package de.bibbuddy;

import java.util.Comparator;

public class SortDate implements Comparator<LibraryItem> {

  @Override
  public int compare(LibraryItem libraryItem, LibraryItem libraryItemToCompare) {
    if (libraryItem.getModDate() == null || libraryItemToCompare.getModDate() == null) {
      return 0;
    }

    return libraryItem.getModDate().compareTo(libraryItemToCompare.getModDate());
  }

}
