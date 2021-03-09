package de.bibbuddy;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

/**
 * The SearchFragment is responsible for searching for shelves, books and notes.
 *
 * @author Claudia Schönherr
 */
public class SearchFragment extends Fragment implements SearchRecyclerViewAdapter.SearchListener {

  private View view;
  private Context context;
  private SearchModel searchModel;
  private SearchRecyclerViewAdapter adapter;

  private List<SearchItem> searchResultList;
  private EditText searchInput;

  private SearchSortCriteria sortCriteria;
  private boolean[] filterCriteria;

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
    view = inflater.inflate(R.layout.fragment_search, container, false);
    context = view.getContext();

    ((MainActivity) getActivity()).updateHeaderFragment(getString(R.string.navigation_search));

    requireActivity().getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
      @Override
      public void handleOnBackPressed() {

        FragmentManager fragmentManager = getParentFragmentManager();
        if (fragmentManager.getBackStackEntryCount() > 0) {
          fragmentManager.popBackStack();
        } else {
          requireActivity().onBackPressed();
        }
      }
    });

    setupSearchInput();
    setupRecyclerView();
    setupSearchButton();

    setHasOptionsMenu(true);

    sortCriteria = SearchSortCriteria.MOD_DATE_LATEST;
    filterCriteria = new boolean[] {true, true, true}; // search for shelves, books and notes

    return view;
  }

  @Override
  public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
    inflater.inflate(R.menu.fragment_search_menu, menu);
    super.onCreateOptionsMenu(menu, inflater);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.menu_search_sort:
        handleSearchSort();
        break;

      case R.id.menu_search_filter:
        handleSearchFilter();
        break;

      case R.id.menu_search_help:
        handleHelp();
        break;

      default:
        break;
    }

    return super.onOptionsItemSelected(item);
  }


  private void setupRecyclerView() {
    searchModel = new SearchModel(context);
    searchResultList = new ArrayList<>();

    // TODO String is always empty when this fragment is newly created even if the text is filled in
    if (!DataValidation.isStringEmpty(searchInput.getText().toString())) {
      searchItems();
    }

    adapter = new SearchRecyclerViewAdapter(searchResultList, this);

    RecyclerView searchRecyclerView = view.findViewById(R.id.search_recycler_view);
    searchRecyclerView.setAdapter(adapter);

    updateEmptyView(searchResultList);
  }

  private void updateEmptyView(List<SearchItem> searchResultList) {
    TextView emptyView = view.findViewById(R.id.list_view_search_empty);

    if (searchResultList.isEmpty()) {
      emptyView.setVisibility(View.VISIBLE);
    } else {
      emptyView.setVisibility(View.GONE);
    }
  }

  private void setupSearchButton() {
    ImageButton searchBtn = view.findViewById(R.id.search_btn);

    searchBtn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        searchItems();
      }
    });
  }

  private void setupSearchInput() {
    searchInput = view.findViewById(R.id.search_input);

    searchInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
      @Override
      public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        searchItems();
        return false;
      }
    });
  }

  private void searchItems() {
    String searchInputStr = searchInput.getText().toString();

    if (DataValidation.isStringEmpty(searchInputStr)) {
      Toast.makeText(context, R.string.search_invalid_name, Toast.LENGTH_SHORT).show();
      return;
    }

    Toast.makeText(context, R.string.search, Toast.LENGTH_SHORT).show();
    searchResultList =
        searchModel.getSearchResultList(searchInputStr, sortCriteria, filterCriteria);

    adapter.setSearchResultList(searchResultList);
    adapter.notifyDataSetChanged();

    updateEmptyView(searchResultList);
  }

  private void handleSearchSort() {
    AlertDialog.Builder selectSearchCriteria = new AlertDialog.Builder(context);
    selectSearchCriteria.setTitle(R.string.search_sort_to);

    int checkedItem = SearchSortCriteria.getCriteriaNum(sortCriteria);

    String[] sortChoices = {
        getString(R.string.sort_name_ascending),
        getString(R.string.sort_name_descending),
        getString(R.string.sort_mod_date_oldest),
        getString(R.string.sort_mod_date_latest)
    };

    selectSearchCriteria
        .setSingleChoiceItems(sortChoices, checkedItem, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int choice) {
            handleSelectedSortChoice(choice);
          }
        });

    selectSearchCriteria.setNegativeButton(R.string.ok, new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int choice) {
        if (!DataValidation.isStringEmpty(searchInput.getText().toString())) {
          sortResultList();
        }
      }
    });

    selectSearchCriteria.show();
  }

  private void handleSelectedSortChoice(int choice) {
    switch (choice) {
      case 0:
        sortCriteria = SearchSortCriteria.NAME_ASCENDING;
        break;

      case 1:
        sortCriteria = SearchSortCriteria.NAME_DESCENDING;
        break;

      case 2:
        sortCriteria = SearchSortCriteria.MOD_DATE_OLDEST;
        break;

      case 3:
        sortCriteria = SearchSortCriteria.MOD_DATE_LATEST;
        break;

      default:
        break;
    }
  }

  private void sortResultList() {
    searchResultList = searchModel.getSortedSearchResultList(sortCriteria);
    adapter.setSearchResultList(searchResultList);
    adapter.notifyDataSetChanged();
  }

  private void handleSearchFilter() {
    AlertDialog.Builder selectFilterCriteria = new AlertDialog.Builder(context);
    selectFilterCriteria.setTitle(R.string.filter_to);

    String[] filterChoices = {
        getString(R.string.filter_shelf),
        getString(R.string.filter_book),
        getString(R.string.filter_note)
    };

    selectFilterCriteria.setMultiChoiceItems(filterChoices, filterCriteria,
                                             new DialogInterface.OnMultiChoiceClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int choice, boolean isChecked) {
          filterCriteria[choice] = isChecked;
          }
        });

    selectFilterCriteria.setNegativeButton(R.string.ok, new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int choice) {
        if (!DataValidation.isStringEmpty(searchInput.getText().toString())) {
          searchItems();
        }
      }
    });

    selectFilterCriteria.show();
  }

  private void handleHelp() {
    HelpFragment helpFragment = new HelpFragment();
    String htmlAsString = getString(R.string.search_help_text);

    Bundle bundle = new Bundle();
    bundle.putString(LibraryKeys.MANUAL_TEXT, htmlAsString);

    helpFragment.setArguments(bundle);

    getActivity().getSupportFragmentManager().beginTransaction()
        .replace(R.id.fragment_container_view, helpFragment,
                 LibraryKeys.FRAGMENT_HELP_VIEW)
        .addToBackStack(null)
        .commit();
  }


  @Override
  public void onItemClicked(int position) {
    SearchItem searchItem = searchModel.getSelectedSearchItem(position);

    ((MainActivity) getActivity()).updateHeaderFragment(searchItem.getName());

    SearchItemType searchItemType = searchItem.getItemType();

    if (searchItemType == SearchItemType.SEARCH_SHELF) {
      openShelf(searchItem);
    } else if (searchItemType == SearchItemType.SEARCH_BOOK) {
      openBook(searchItem);
    } else if (searchItemType == SearchItemType.SEARCH_TEXT_NOTE) {
      openTextNote(searchItem);
    }

  }

  private Bundle createShelfBundle(SearchItem searchItem) {
    Bundle bundle = new Bundle();

    bundle.putLong(LibraryKeys.SHELF_ID, searchItem.getId());
    bundle.putString(LibraryKeys.SHELF_NAME, searchItem.getName());

    return bundle;
  }

  private void openShelf(SearchItem searchItem) {
    BookFragment fragment = new BookFragment();
    fragment.setArguments(createShelfBundle(searchItem));

    getActivity().getSupportFragmentManager().beginTransaction()
        .replace(R.id.fragment_container_view, fragment)
        .setReorderingAllowed(true)
        .addToBackStack(null)
        .commit();
  }

  private void openBook(SearchItem searchItem) {
    BookNotesView fragment = new BookNotesView();
    fragment.setArguments(createShelfBundle(searchItem));

    getActivity().getSupportFragmentManager().beginTransaction()
        .replace(R.id.fragment_container_view, fragment)
        .setReorderingAllowed(true)
        .addToBackStack(null)
        .commit();
  }

  private Bundle createNoteBundle(SearchItem searchItem) {
    Long noteId = searchItem.getId();

    Bundle bundle = new Bundle();
    bundle.putLong(LibraryKeys.BOOK_ID, searchModel.getBookIdByNoteId(noteId));
    bundle.putLong(LibraryKeys.NOTE_ID, noteId);

    return bundle;
  }

  private void openTextNote(SearchItem searchItem) {
    TextNoteEditorFragment fragment = new TextNoteEditorFragment();
    fragment.setArguments(createNoteBundle(searchItem));

    getActivity().getSupportFragmentManager().beginTransaction()
        .replace(R.id.fragment_container_view, fragment)
        .addToBackStack(null)
        .commit();
  }

}
