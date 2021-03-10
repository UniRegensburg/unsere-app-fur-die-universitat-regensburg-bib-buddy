package de.bibbuddy;

import java.util.Comparator;

/**
 * The SortName class is responsible for sorting the list by name.
 *
 * @author Claudia Schönherr
 */
public class SortName implements Comparator<LibraryItem> {

  @Override
  public int compare(LibraryItem libraryItem, LibraryItem libraryItemToCompare) {
    if (libraryItem.getName() == null || libraryItemToCompare.getName() == null) {
      return 0;
    }

    return libraryItem.getName().compareToIgnoreCase(libraryItemToCompare.getName());
  }

}
