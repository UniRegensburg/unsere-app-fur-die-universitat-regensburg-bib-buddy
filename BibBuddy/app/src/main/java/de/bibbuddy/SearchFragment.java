package de.bibbuddy;

import android.content.Context;
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
import android.widget.ToggleButton;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
  private RecyclerView searchRecyclerView;
  private List<SearchItem> searchResultList;
  private EditText searchInput;
  private SearchSortCriteria sortCriteria;

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

        FragmentManager fm = getParentFragmentManager();
        if (fm.getBackStackEntryCount() > 0) {
          fm.popBackStack();
        } else {
          requireActivity().onBackPressed();
        }
      }
    });

    setupSearchInput();
    setupRecyclerView();
    setupSearchButton();
    setupSortBar();

    setHasOptionsMenu(true);
    sortCriteria = SearchSortCriteria.MOD_DATE_LATEST;

    return view;
  }

  @Override
  public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
    inflater.inflate(R.menu.fragment_search_menu, menu);
    super.onCreateOptionsMenu(menu, inflater);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == R.id.menu_help_search) {
      handleHelp();
    }

    return super.onOptionsItemSelected(item);
  }

  private void setupRecyclerView() {
    searchRecyclerView = view.findViewById(R.id.search_recycler_view);
    String searchInputStr = searchInput.getText().toString();

    searchModel = new SearchModel(context);
    searchResultList = new ArrayList<>();

    if (!DataValidation.isStringEmpty(searchInputStr)) {
      searchItems(); // TODO find out why search Input String is always empty when this fragment
      // is newly created even if the text is filled in
    }

    adapter = new SearchRecyclerViewAdapter(searchResultList, this);
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

    // TODO get filter criterias from view

    searchResultList = searchModel.getSearchResultList(searchInputStr, sortCriteria);

    adapter.setSearchResultList(searchResultList);
    adapter.notifyDataSetChanged();

    updateEmptyView(searchResultList);
  }

  private void setupSortBar() {
    ToggleButton sortNameBtn = view.findViewById(R.id.sort_name);

    sortNameBtn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        handleSortName(sortNameBtn);
      }
    });

    ToggleButton sortDateBtn = view.findViewById(R.id.sort_date);

    sortDateBtn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        handleSortDate();
      }
    });
  }

  private void handleSortName(ToggleButton sortNameBtn) {
    if (sortNameBtn.isChecked()) {
      sortNameBtn.setChecked(true);
      sortCriteria = SearchSortCriteria.NAME_ASCENDING;
    } else {
      sortNameBtn.setChecked(false);
      sortCriteria = SearchSortCriteria.NAME_DESCENDING;
    }

    sortResultList();
  }

  private void handleSortDate() {
    ToggleButton sortDateBtn = view.findViewById(R.id.sort_date);

    if (sortDateBtn.isChecked()) {
      sortDateBtn.setChecked(true);
      sortCriteria = SearchSortCriteria.MOD_DATE_OLDEST;
    } else {
      sortDateBtn.setChecked(false);
      sortCriteria = SearchSortCriteria.MOD_DATE_LATEST;
    }

    sortResultList();
  }

  private void sortResultList() {
    searchResultList = searchModel.getSortedSearchResultList(sortCriteria);
    adapter.setSearchResultList(searchResultList);
    adapter.notifyDataSetChanged();
  }

  private void handleSearchFilter() { // TODO implement filter
    Toast.makeText(context, R.string.search_filter, Toast.LENGTH_SHORT).show();
    // TODO filter search results
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
