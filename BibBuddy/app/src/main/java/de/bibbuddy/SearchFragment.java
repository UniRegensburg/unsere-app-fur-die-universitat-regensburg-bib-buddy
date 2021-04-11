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
import com.google.android.material.bottomnavigation.BottomNavigationView;
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

  private SortCriteria sortCriteria;
  private boolean[] filterCriteria;

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
    requireActivity().getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
      @Override
      public void handleOnBackPressed() {
        closeFragment();
      }
    });

    view = inflater.inflate(R.layout.fragment_search, container, false);
    context = view.getContext();

    ((MainActivity) requireActivity()).updateHeaderFragment(getString(R.string.navigation_search));
    ((MainActivity) requireActivity()).updateNavigationFragment(R.id.navigation_search);

    requireActivity().getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
      @Override
      public void handleOnBackPressed() {
        closeFragment();
      }
    });

    setupSearchInput();
    setupRecyclerView();
    setupFilterButton();

    setHasOptionsMenu(true);

    sortCriteria = ((MainActivity) requireActivity()).getSortCriteria();
    filterCriteria = ((MainActivity) requireActivity()).getFilterCriteria();

    ((MainActivity) getActivity()).setVisibilityImportShareButton(View.GONE, View.GONE);
    setupSortBtn();

    return view;
  }

  private void setupSortBtn() {
    ImageButton sortBtn = getActivity().findViewById(R.id.sort_btn);
    ((MainActivity) getActivity()).setVisibilitySortButton(true);

    sortBtn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        handleSortSearch();
      }
    });
  }

  /**
   * Closes the SearchFragment.
   */
  public void closeFragment() {
    FragmentManager fragmentManager = getParentFragmentManager();
    if (fragmentManager.getBackStackEntryCount() > 0) {
      fragmentManager.popBackStack();
    } else {
      requireActivity().onBackPressed();
    }
  }

  @Override
  public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
    inflater.inflate(R.menu.fragment_search_menu, menu);
    super.onCreateOptionsMenu(menu, inflater);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {

      case R.id.menu_search_help:
        handleHelp();
        break;

      case R.id.menu_imprint:
        ((MainActivity) getActivity()).openImprint();
        break;

      default:
        break;
    }

    return super.onOptionsItemSelected(item);
  }


  private void setupRecyclerView() {
    searchModel = new SearchModel(context);
    searchResultList = new ArrayList<>();

    String searchText = ((MainActivity) requireActivity()).getSearchText();
    if (!DataValidation.isStringEmpty(searchText)) {
      searchItems(searchText);
    }

    adapter = new SearchRecyclerViewAdapter(searchResultList, this);

    RecyclerView searchRecyclerView = view.findViewById(R.id.search_recycler_view);
    searchRecyclerView.setAdapter(adapter);

    updateEmptyView(searchResultList);

    if (DataValidation.isStringEmpty(searchText)) {
      view.findViewById(R.id.list_view_search_empty).setVisibility(View.GONE);
    }
  }

  private void updateEmptyView(List<SearchItem> searchResultList) {
    TextView emptyView = view.findViewById(R.id.list_view_search_empty);

    if (searchResultList.isEmpty()) {
      emptyView.setVisibility(View.VISIBLE);
    } else {
      emptyView.setVisibility(View.GONE);
    }
  }

  private void setupFilterButton() {
    ImageButton filterBtn = view.findViewById(R.id.filter_btn);

    filterBtn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        handleSearchFilter();
      }
    });

  }

  private void setupSearchInput() {
    searchInput = view.findViewById(R.id.search_input);

    searchInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
      @Override
      public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        String searchText = searchInput.getText().toString();
        ((MainActivity) requireActivity()).setSearchText(searchText);
        searchItems(searchText);

        return false;
      }
    });
  }

  private void searchItems(String searchText) {
    if (DataValidation.isStringEmpty(searchText)) {
      Toast.makeText(context, R.string.search_invalid_name, Toast.LENGTH_SHORT).show();
      return;
    }

    Toast.makeText(context, R.string.search, Toast.LENGTH_SHORT).show();
    updateSearchResultList(searchText);
  }

  private void updateSearchResultList(String searchInputStr) {
    searchResultList =
        searchModel.getSearchResultList(searchInputStr, sortCriteria, filterCriteria);

    adapter.setSearchResultList(searchResultList);
    adapter.notifyDataSetChanged();

    updateEmptyView(searchResultList);
  }

  private void handleSortSearch() {
    SortDialog sortDialog = new SortDialog(context, sortCriteria,
        new SortDialog.SortDialogListener() {
          @Override
          public void onSortedSelected(SortCriteria newSortCriteria) {
            sortCriteria = newSortCriteria;
            ((MainActivity) getActivity()).setSortCriteria(newSortCriteria);
            sortResultList();
          }
        });

    sortDialog.show();
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
        getString(R.string.filter_note) };

    selectFilterCriteria.setMultiChoiceItems(filterChoices, filterCriteria,
        new DialogInterface.OnMultiChoiceClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int choice, boolean isChecked) {
            filterCriteria[choice] = isChecked;
            ((MainActivity) requireActivity()).setFilterCriteria(choice, isChecked);
          }
        });

    selectFilterCriteria.setNegativeButton(R.string.ok, new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int choice) {
        if (!DataValidation.isStringEmpty(searchInput.getText().toString())) {
          filterItems();
        }
      }
    });

    selectFilterCriteria.show();
  }

  private void filterItems() {
    Toast.makeText(context, R.string.filter, Toast.LENGTH_SHORT).show();
    String searchInputStr = searchInput.getText().toString();

    if (DataValidation.isStringEmpty(searchInputStr)) {
      return;
    }

    updateSearchResultList(searchInputStr);
  }

  private void handleHelp() {
    HelpFragment helpFragment = new HelpFragment();
    String htmlAsString = getString(R.string.search_help_text);

    Bundle bundle = new Bundle();
    bundle.putString(LibraryKeys.MANUAL_TEXT, htmlAsString);

    helpFragment.setArguments(bundle);

    helpFragment.show(requireActivity().getSupportFragmentManager(), LibraryKeys.FRAGMENT_HELP_VIEW);
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

  private Bundle createBookBundle(SearchItem searchItem) {
    Bundle bundle = new Bundle();

    Long searchItemId = searchItem.getId();
    bundle.putLong(LibraryKeys.SHELF_ID, searchModel.getShelfIdByBook(searchItemId));
    bundle.putString(LibraryKeys.BOOK_TITLE, searchItem.getName());
    bundle.putLong(LibraryKeys.BOOK_ID, searchItemId);

    return bundle;
  }

  private void openBook(SearchItem searchItem) {
    BookNotesView fragment = new BookNotesView();
    fragment.setArguments(createBookBundle(searchItem));

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
