package de.bibbuddy;

import java.util.Comparator;

/**
 * The SortDate class is responsible for sorting the list by name.
 *
 * @author Claudia Schönherr
 */
public class SortDate implements Comparator<SortableItem> {

  @Override
  public int compare(SortableItem lhs, SortableItem rhs) {
    if (lhs.getModDate() == null || rhs.getModDate() == null) {
      return 0;
    }

    return rhs.getModDate().compareTo(lhs.getModDate());
  }

}
