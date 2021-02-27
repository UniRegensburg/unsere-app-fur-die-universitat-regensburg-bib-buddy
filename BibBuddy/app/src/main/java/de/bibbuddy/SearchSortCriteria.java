package de.bibbuddy;

/**
 * SearchSortCriteria enum for the sorting criteria for the search.
 *
 * @author Claudia Schönherr
 */
public enum SearchSortCriteria {
  NAME_ASCENDING,
  NAME_DESCENDING,
  MOD_DATE_OLDEST,
  MOD_DATE_LATEST;

  /**
   * Gets the int value of the sortingCriteria.
   *
   * @param sortCriteria the selected sortingCriteria
   * @return Returns the int of the enum.
   */
  public static int getCriteriaNum(SearchSortCriteria sortCriteria) {
    if (sortCriteria == NAME_ASCENDING) {
      return 0;
    } else if (sortCriteria == NAME_DESCENDING) {
      return 1;
    } else if (sortCriteria == MOD_DATE_OLDEST) {
      return 2;
    } else {
      return 3; // MOD_DATE_LATEST
    }
  }
}
