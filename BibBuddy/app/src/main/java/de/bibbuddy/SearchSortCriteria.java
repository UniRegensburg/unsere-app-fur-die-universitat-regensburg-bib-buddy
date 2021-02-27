package de.bibbuddy;

public enum SearchSortCriteria {
  NAME_ASCENDING,
  NAME_DESCENDING,
  MOD_DATE_OLDEST,
  MOD_DATE_LATEST;

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
