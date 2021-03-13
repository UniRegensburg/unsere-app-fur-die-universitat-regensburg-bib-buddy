package de.bibbuddy;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

/**
 * The SortDialog class is the UI for sorting items.
 *
 * @author Claudia Schönherr
 */
public class SortDialog extends AlertDialog.Builder {

  private final SortDialogListener listener;
  private Context context;

  private SortCriteria sortCriteria;

  protected SortDialog(Context context, SortCriteria sortCriteria,
                       SortDialogListener listener) {
    super(context);

    this.context = context;
    this.listener = listener;

    this.sortCriteria = sortCriteria;

    setTitle(R.string.search_sort_to);
    setupDialog();
  }

  protected SortDialog(Context context, int themeResId,
                       SortDialogListener listener) {
    super(context, themeResId);
    this.listener = listener;
  }

  private void setupDialog() {
    int checkedItem = SortCriteria.getCriteriaNum(sortCriteria);

    String[] sortChoices = {
        context.getString(R.string.sort_mod_date_latest),
        context.getString(R.string.sort_mod_date_oldest),
        context.getString(R.string.sort_name_ascending),
        context.getString(R.string.sort_name_descending)
    };

    setSingleChoiceItems(sortChoices, checkedItem, new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int choice) {
        handleSelectedSortChoice(choice);
      }
    });

    setNegativeButton(R.string.ok, new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int choice) {
        listener.onSortedSelected(sortCriteria);
      }
    });
  }

  private void handleSelectedSortChoice(int choice) {
    switch (choice) {
      case 0:
        sortCriteria = SortCriteria.MOD_DATE_LATEST;
        break;

      case 1:
        sortCriteria = SortCriteria.MOD_DATE_OLDEST;
        break;

      case 2:
        sortCriteria = SortCriteria.NAME_ASCENDING;

        break;

      case 3:
        sortCriteria = SortCriteria.NAME_DESCENDING;
        break;

      default:
        break;
    }
  }

  public interface SortDialogListener {
    void onSortedSelected(SortCriteria sortCriteria);
  }

}