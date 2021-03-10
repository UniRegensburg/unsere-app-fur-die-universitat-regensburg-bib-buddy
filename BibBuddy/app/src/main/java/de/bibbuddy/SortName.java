package de.bibbuddy;

import java.util.Comparator;

public class SortName implements Comparator<LibraryItem> {

  @Override
  public int compare(LibraryItem libraryItem, LibraryItem libraryItemToCompare) {
    if (libraryItem.getName() == null || libraryItemToCompare.getName() == null) {
      return 0;
    }

    return libraryItem.getName().compareToIgnoreCase(libraryItemToCompare.getName());
  }

}
