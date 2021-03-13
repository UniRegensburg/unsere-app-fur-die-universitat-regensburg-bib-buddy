package de.bibbuddy;

/**
 * SearchSortCriteria enum for the sorting criteria for the search.
 *
 * @author Claudia Schönherr
 */
public enum SortCriteria {
  MOD_DATE_LATEST,
  MOD_DATE_OLDEST,
  NAME_ASCENDING,
  NAME_DESCENDING;

  /**
   * Gets the int value of the sortingCriteria.
   *
   * @param sortCriteria the selected sortingCriteria
   * @return Returns the int of the enum.
   */
  public static int getCriteriaNum(SortCriteria sortCriteria) {
    if (sortCriteria == MOD_DATE_LATEST) {
      return 0;
    } else if (sortCriteria == MOD_DATE_OLDEST) {
      return 1;
    } else if (sortCriteria == NAME_ASCENDING) {
      return 2;
    } else {
      return 3; // NAME_DESCENDING
    }
  }

}
