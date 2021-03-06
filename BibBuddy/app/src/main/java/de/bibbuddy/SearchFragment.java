package de.bibbuddy;

import android.annotation.SuppressLint;
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
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

/**
 * The SearchFragment is responsible for searching for shelves, books and notes.
 *
 * @author Claudia Schönherr
 */
public class SearchFragment extends BackStackFragment
    implements SearchRecyclerViewAdapter.SearchListener {

  private View view;
  private Context context;
  private SearchModel searchModel;
  private SearchRecyclerViewAdapter adapter;

  private List<SearchItem> searchResultList;
  private EditText searchInput;

  private SortTypeLut sortTypeLut;
  private boolean[] filterCriteria;

  private void setupMainActivity() {
    MainActivity mainActivity = (MainActivity) requireActivity();

    mainActivity.setVisibilityImportShareBtn(View.GONE, View.GONE);
    mainActivity.setVisibilitySortBtn(true);
    sortTypeLut = mainActivity.getSortTypeLut();

    filterCriteria = mainActivity.getFilterCriteria();

    mainActivity.updateHeaderFragment(getString(R.string.navigation_search));
    mainActivity.updateNavigationFragment(R.id.navigation_search);
  }

  private void setupSortBtn() {
    ImageButton sortBtn = requireActivity().findViewById(R.id.sort_btn);
    sortBtn.setOnClickListener(v -> handleSortSearch());
  }

  private void setupFilterBtn() {
    ImageButton filterBtn = view.findViewById(R.id.filter_btn);
    filterBtn.setOnClickListener(v -> handleSearchFilter());
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

  private void setupSearchInput() {
    searchInput = view.findViewById(R.id.search_input);

    searchInput.setOnEditorActionListener((v, actionId, event) -> {

      if (!(event == null || event.getAction() != KeyEvent.ACTION_DOWN)) {
        String searchText = searchInput.getText().toString();
        ((MainActivity) requireActivity()).setSearchText(searchText);
        searchItems(searchText);
      }

      return false;
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
        searchModel.getSearchResultList(searchInputStr, sortTypeLut, filterCriteria);

    adapter.setSearchResultList(searchResultList);
    adapter.notifyDataSetChanged();

    updateEmptyView(searchResultList);
  }

  private void handleSortSearch() {
    SortDialog sortDialog = new SortDialog(context, sortTypeLut,
        newSortCriteria -> {
          sortTypeLut = newSortCriteria;
          ((MainActivity) requireActivity())
          .setSortTypeLut(newSortCriteria);
          sortResultList();
        });

    sortDialog.show();
  }

  private void sortResultList() {
    searchResultList = searchModel.getSortedSearchResultList(sortTypeLut);
    adapter.setSearchResultList(searchResultList);
    adapter.notifyDataSetChanged();
  }

  private void handleSearchFilter() {
    AlertDialog.Builder selectFilterCriteria = new AlertDialog.Builder(context);
    selectFilterCriteria.setTitle(R.string.filter_to);

    String[] filterChoices = {
        getString(R.string.filter_shelf),
        getString(R.string.filter_book),
        getString(R.string.filter_text_note)};

    selectFilterCriteria.setMultiChoiceItems(filterChoices, filterCriteria,
      (dialog, choice, isChecked) -> {
        filterCriteria[choice] = isChecked;
        ((MainActivity) requireActivity())
                                                   .setFilterCriteria(choice, isChecked);
      });

    selectFilterCriteria.setNegativeButton(R.string.ok, (dialog, choice) -> {
      if (!DataValidation.isStringEmpty(searchInput.getText().toString())) {
        filterItems();
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
    String htmlAsString = getString(R.string.search_help_text);

    Bundle bundle = new Bundle();
    bundle.putString(LibraryKeys.MANUAL_TEXT, htmlAsString);

    HelpFragment helpFragment = new HelpFragment();
    helpFragment.setArguments(bundle);

    helpFragment
        .show(requireActivity().getSupportFragmentManager(), LibraryKeys.FRAGMENT_HELP_VIEW);
  }

  private Bundle createShelfBundle(SearchItem searchItem) {
    Bundle bundle = new Bundle();

    bundle.putLong(LibraryKeys.SHELF_ID, searchItem.getId());
    bundle.putString(LibraryKeys.SHELF_NAME, searchItem.getName());

    return bundle;
  }

  private void openShelf(SearchItem searchItem) {
    BookFragment bookFragment = new BookFragment();
    bookFragment.setArguments(createShelfBundle(searchItem));

    showFragment(bookFragment);
  }

  private Bundle createBookBundle(SearchItem searchItem) {
    Bundle bundle = new Bundle();

    Long searchItemId = searchItem.getId();
    bundle.putLong(LibraryKeys.SHELF_ID, searchModel.getShelfIdByBook(searchItemId));
    bundle.putString(LibraryKeys.SHELF_NAME, searchModel.getShelfNameByBook(searchItemId));
    bundle.putLong(LibraryKeys.BOOK_ID, searchItemId);

    return bundle;
  }

  private void openBook(SearchItem searchItem) {
    BookNotesFragment bookNotesFragment = new BookNotesFragment();
    bookNotesFragment.setArguments(createBookBundle(searchItem));

    showFragment(bookNotesFragment);
  }

  private Bundle createNoteBundle(SearchItem searchItem) {
    Long noteId = searchItem.getId();

    Bundle bundle = new Bundle();
    bundle.putLong(LibraryKeys.BOOK_ID, searchModel.getBookIdByNoteId(noteId));
    bundle.putLong(LibraryKeys.NOTE_ID, noteId);

    return bundle;
  }

  private void openTextNote(SearchItem searchItem) {
    TextNoteEditorFragment textNoteEditorFragment = new TextNoteEditorFragment();
    textNoteEditorFragment.setArguments(createNoteBundle(searchItem));

    showFragment(textNoteEditorFragment);
  }

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {

    view = inflater.inflate(R.layout.fragment_search, container, false);
    context = view.getContext();

    setupMainActivity();

    setupSearchInput();
    setupRecyclerView();
    setupSortBtn();
    setupFilterBtn();

    setHasOptionsMenu(true);

    return view;
  }

  @Override
  public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
    inflater.inflate(R.menu.fragment_search_menu, menu);
    super.onCreateOptionsMenu(menu, inflater);
  }

  @SuppressLint("NonConstantResourceId")
  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {

      case R.id.menu_search_help:
        handleHelp();
        break;

      case R.id.menu_imprint:
        ((MainActivity) requireActivity()).openImprint();
        break;

      default:
        throw new IllegalArgumentException();
    }

    return super.onOptionsItemSelected(item);
  }

  @Override
  public void onItemClicked(int position) {
    SearchItem searchItem = searchModel.getSelectedSearchItem(position);
    SearchTypeLut searchTypeLut = searchItem.getItemType();

    switch (searchTypeLut) {
      case SEARCH_SHELF:
        openShelf(searchItem);
        break;

      case SEARCH_BOOK:
        openBook(searchItem);
        break;

      case SEARCH_TEXT_NOTE:
        openTextNote(searchItem);
        break;

      default:
        throw new IllegalArgumentException();
    }

  }

}
