package de.bibbuddy;

import android.app.AlertDialog;
import android.content.Context;
import java.util.EnumSet;

/**
 * The SortDialog class is the UI for sorting items.
 *
 * @author Claudia Schönherr
 */
public class SortDialog extends AlertDialog.Builder {

  private final SortDialogListener listener;
  private final Context context;

  private SortTypeLut sortTypeLut;

  protected SortDialog(Context context, SortTypeLut sortTypeLut,
                       SortDialogListener listener) {
    super(context);

    this.context = context;
    this.listener = listener;

    this.sortTypeLut = sortTypeLut;

    setTitle(R.string.search_sort_to);
    setupDialog();
  }

  // TODO
  private String getSortCriteriaDisplayText(SortTypeLut sortTypeLut) {
    switch (sortTypeLut) {
      case MOD_DATE_LATEST:
        return context.getString(R.string.sort_mod_date_latest);
      case MOD_DATE_OLDEST:
        return context.getString(R.string.sort_mod_date_oldest);
      case NAME_ASCENDING:
        return context.getString(R.string.sort_name_ascending);
      case NAME_DESCENDING:
        return context.getString(R.string.sort_name_descending);
    }

    throw new IllegalArgumentException();
  }

  private void setupDialog() {
    int checkedItem = sortTypeLut.getId();

    EnumSet<SortTypeLut> sortTypes = EnumSet.allOf(SortTypeLut.class);
    String[] sortChoices = sortTypes.stream()
        .map(e -> getSortCriteriaDisplayText(e))
        .toArray(String[]::new);

    setSingleChoiceItems(sortChoices, checkedItem,
                         (dialog, choice) -> handleSelectedSortChoice(choice));

    setNegativeButton(R.string.ok, (dialog, choice) -> listener.onSortedSelected(sortTypeLut));
  }

  private void handleSelectedSortChoice(int choice) {
    sortTypeLut = SortTypeLut.valueOf(choice);
  }

  public interface SortDialogListener {
    void onSortedSelected(SortTypeLut sortTypeLut);
  }

}
