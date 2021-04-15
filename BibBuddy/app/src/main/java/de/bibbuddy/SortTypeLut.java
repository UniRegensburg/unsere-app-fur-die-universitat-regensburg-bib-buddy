package de.bibbuddy;

import java.util.stream.Stream;

/**
 * SortTypeLut enum for sorting lists.
 *
 * @author Claudia Schönherr
 */
public enum SortTypeLut {

  MOD_DATE_LATEST(0),
  MOD_DATE_OLDEST(1),
  NAME_ASCENDING(2),
  NAME_DESCENDING(3);

  private final int id;

  SortTypeLut(int id) {
    this.id = id;
  }

  static SortTypeLut valueOf(int id) {
    return Stream.of(SortTypeLut.values())
        .filter(e -> e.getId() == id)
        .findFirst()
        .orElseThrow(IllegalArgumentException::new);
  }

  /**
   * Gets the int value of the sortingCriteria.
   *
   * @return returns the int of the enum
   */
  public int getId() {
    return id;
  }

}
