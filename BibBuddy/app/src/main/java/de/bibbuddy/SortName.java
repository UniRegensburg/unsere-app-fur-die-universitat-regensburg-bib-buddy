package de.bibbuddy;

import java.util.Comparator;

/**
 * The SortName class is responsible for sorting the list by name.
 *
 * @author Claudia Schönherr
 */
public class SortName implements Comparator<SortableItem> {

  @Override
  public int compare(SortableItem lhs, SortableItem rhs) {
    if (lhs.getName() == null || rhs.getName() == null) {
      return 0;
    }

    return lhs.getName().compareToIgnoreCase(rhs.getName());
  }

}
