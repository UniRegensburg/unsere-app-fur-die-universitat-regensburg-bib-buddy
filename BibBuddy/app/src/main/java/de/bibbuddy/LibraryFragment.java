package de.bibbuddy;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ShareCompat;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.tsuryo.swipeablerv.SwipeLeftRightCallback;
import com.tsuryo.swipeablerv.SwipeableRecyclerView;
import java.util.ArrayList;
import java.util.List;

/**
 * The LibraryFragment is responsible for the shelves in the library.
 *
 * @author Claudia Schönherr, Silvia Ivanova, Luis Moßburger
 */
public class LibraryFragment extends BackStackFragment
    implements LibraryRecyclerViewAdapter.LibraryListener, SwipeLeftRightCallback.Listener {

  private View view;
  private Context context;
  private LibraryModel libraryModel;
  private LibraryRecyclerViewAdapter adapter;
  private List<ShelfItem> selectedShelfItems = new ArrayList<>();

  private BookModel bookModel;
  private NoteModel noteModel;

  private ExportBibTex exportBibTex;
  private SortCriteria sortCriteria;

  @Override
  protected void onBackPressed() {
    if (selectedShelfItems.isEmpty()) {
      closeFragment();
    } else {
      deselectLibraryItems();
    }
  }

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {

    enableBackPressedHandler();

    view = inflater.inflate(R.layout.fragment_library, container, false);
    context = view.getContext();

    MainActivity mainActivity = (MainActivity) requireActivity();
    sortCriteria = mainActivity.getSortCriteria();

    setupRecyclerView();
    setupAddShelfBtn();

    mainActivity.updateHeaderFragment(getString(R.string.navigation_library));
    mainActivity.updateNavigationFragment(R.id.navigation_library);
    mainActivity.setVisibilityImportShareButton(View.GONE, View.VISIBLE);

    setupSortBtn();
    setFunctionsToolbar();
    setHasOptionsMenu(true);

    selectedShelfItems.clear();
    bookModel = new BookModel(requireContext(), libraryModel.getShelfId());
    noteModel = new NoteModel(requireContext());

    String fileName = "library_export_BibBuddy";
    exportBibTex = new ExportBibTex(fileName);

    return view;
  }

  private void setupSortBtn() {
    MainActivity mainActivity = (MainActivity) requireActivity();

    ImageButton sortBtn = mainActivity.findViewById(R.id.sort_btn);
    mainActivity.setVisibilitySortButton(true);

    sortBtn.setOnClickListener(v -> handleSortShelf());
  }

  private void setFunctionsToolbar() {
    ((MainActivity) requireActivity()).shareBtn.setOnClickListener(view -> checkEmptyLibrary());

  }

  @Override
  public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
    inflater.inflate(R.menu.fragment_library_menu, menu);
    super.onCreateOptionsMenu(menu, inflater);
  }

  @SuppressLint("NonConstantResourceId")
  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.menu_rename_shelf:
        handleRenameShelf();
        break;

      case R.id.menu_delete_shelf:
        handleDeleteShelf();
        break;

      case R.id.menu_help_library:
        handleManualLibrary();
        break;

      case R.id.menu_imprint:
        ((MainActivity) requireActivity()).openImprint();
        break;

      default:
        break;
    }

    return super.onOptionsItemSelected(item);
  }

  private void checkEmptyLibrary() {
    if (libraryModel.getCurrentLibraryList().isEmpty() || bookModel.getAllBooks().isEmpty()) {
      AlertDialog.Builder alertDialogEmptyLib = new AlertDialog.Builder(requireContext());
      alertDialogEmptyLib.setTitle(R.string.empty_library);
      alertDialogEmptyLib.setMessage(R.string.empty_library_description);

      alertDialogEmptyLib.setPositiveButton(R.string.ok,
                                            (dialog, which) -> {
                                            });

      alertDialogEmptyLib.create().show();

    } else {
      shareLibraryBibIntent();
    }

  }

  private void handleManualLibrary() {
    String htmlAsString = getString(R.string.library_help_text);

    Bundle bundle = new Bundle();
    bundle.putString(LibraryKeys.MANUAL_TEXT, htmlAsString);

    HelpFragment helpFragment = new HelpFragment();
    helpFragment.setArguments(bundle);

    helpFragment
        .show(requireActivity().getSupportFragmentManager(), LibraryKeys.FRAGMENT_HELP_VIEW);
  }

  @Override
  public void onPrepareOptionsMenu(Menu menu) {
    MenuItem renameShelf = menu.findItem(R.id.menu_rename_shelf);
    MenuItem deleteShelf = menu.findItem(R.id.menu_delete_shelf);

    if (selectedShelfItems == null || selectedShelfItems.isEmpty()) {
      renameShelf.setVisible(false);
      deleteShelf.setVisible(false);
    } else if (selectedShelfItems.size() != 1) {
      renameShelf.setVisible(false);
      deleteShelf.setVisible(true);
    } else {
      renameShelf.setVisible(true);
      deleteShelf.setVisible(true);
    }

  }

  private void handleDeleteShelf() {
    AlertDialog.Builder alertDeleteShelf = new AlertDialog.Builder(context);
    alertDeleteShelf.setCancelable(false);
    setAlertMessage(alertDeleteShelf);

    if (selectedShelfItems.size() > 1) {
      alertDeleteShelf.setTitle(R.string.delete_shelves);
    } else {
      alertDeleteShelf.setTitle(R.string.delete_shelf);
    }

    alertDeleteShelf.setNegativeButton(R.string.cancel, (dialog, which) -> deselectLibraryItems());

    alertDeleteShelf.setPositiveButton(R.string.delete, (dialog, which) -> {
      performDeleteShelf();
      deselectLibraryItems();
    });

    alertDeleteShelf.show();
  }

  private void setAlertMessage(AlertDialog.Builder alertDeleteShelf) {
    alertDeleteShelf.setMessage(
        getString(R.string.delete_shelf_message)
            + convertShelfListToString(selectedShelfItems)
            + getString(R.string.delete_counter_msg)
            + getBooksToDeleteNumber(selectedShelfItems)
            + getString(R.string.and) + getNotesToDeleteNumber(selectedShelfItems) + " "
            + getString(R.string.finally_delete) + " "
            + getString(R.string.delete_warning));
  }

  private String convertShelfListToString(List<ShelfItem> shelfList) {
    StringBuilder shelfs = new StringBuilder();

    int counter = 1;
    for (ShelfItem shelf : shelfList) {
      shelfs.append(" \"").append(shelf.getName()).append("\"");

      if (counter != shelfList.size()) {
        shelfs.append(",");
      }

      shelfs.append(" ");
      ++counter;
    }

    return shelfs.toString();
  }

  private String getBooksToDeleteNumber(List<ShelfItem> shelfList) {
    int booksNumber = 0;
    for (ShelfItem shelf : shelfList) {
      booksNumber += shelf.getBookCount();
    }

    if (booksNumber == 1) {
      return getString(R.string.of_one) + getString(R.string.book);
    }

    return " " + booksNumber + " " + getString(R.string.books) + "n";
  }

  private String getNotesToDeleteNumber(List<ShelfItem> shelfList) {
    int notesNumber = 0;
    for (ShelfItem shelf : shelfList) {
      notesNumber += shelf.getNoteCount();
    }

    if (notesNumber == 1) {
      return getString(R.string.one) + getString(R.string.note);
    }

    return " " + notesNumber + " " + getString(R.string.notes);
  }

  private void performDeleteShelf() {
    final int shelvesNumber = selectedShelfItems.size();

    libraryModel.deleteShelves(selectedShelfItems);
    updateLibraryListView(libraryModel.getCurrentLibraryList());

    if (shelvesNumber > 1) {
      Toast.makeText(context, getString(R.string.deleted_shelves), Toast.LENGTH_SHORT).show();
    } else {
      Toast.makeText(context, getString(R.string.deleted_shelf), Toast.LENGTH_SHORT).show();
    }
  }

  private Bundle createRenameShelfBundle() {
    Bundle bundle = new Bundle();

    bundle.putStringArray(LibraryKeys.SHELF_NAMES, getAllShelfNames());
    bundle.putString(LibraryKeys.SHELF_NAME, selectedShelfItems.get(0).getName());

    Long currentShelfId = selectedShelfItems.get(0).getId();
    bundle.putLong(LibraryKeys.SHELF_ID, currentShelfId);

    return bundle;
  }

  private void handleRenameShelf() {
    LibraryFormFragment libraryFormFragment =
        new LibraryFormFragment(new LibraryFormFragment.ChangeShelfListener() {
          @Override
          public void onShelfRenamed(String shelfName) {
            libraryModel.renameShelf(selectedShelfItems.get(0), shelfName);
            adapter.notifyDataSetChanged();
            Toast.makeText(context, getString(R.string.renamed_shelf), Toast.LENGTH_SHORT).show();
          }
        });

    libraryFormFragment.setArguments(createRenameShelfBundle());

    showFragment(libraryFormFragment, LibraryKeys.FRAGMENT_LIBRARY_FORM);
  }

  private void deselectLibraryItems() {
    SwipeableRecyclerView shelfListView = requireView().findViewById(R.id.library_recycler_view);
    for (int i = 0; i < shelfListView.getChildCount(); i++) {
      shelfListView.getChildAt(i).setSelected(false);
    }

    selectedShelfItems.clear();
  }

  private void handleSortShelf() {
    SortDialog sortDialog = new SortDialog(context, sortCriteria,
                                           newSortCriteria -> {
                                             sortCriteria = newSortCriteria;
                                             ((MainActivity) requireActivity())
                                                 .setSortCriteria(newSortCriteria);
                                             sortLibraryList();
                                           });

    sortDialog.show();
  }

  private void sortLibraryList() {
    List<ShelfItem> libraryList = libraryModel.getSortedLibraryList(sortCriteria);
    adapter.setLibraryList(libraryList);
    adapter.notifyDataSetChanged();
  }

  private void setupRecyclerView() {
    libraryModel = new LibraryModel(requireContext());
    List<ShelfItem> libraryList = libraryModel
        .getSortedLibraryList(sortCriteria, libraryModel.getLibraryList(null));

    SwipeableRecyclerView libraryRecyclerView = view.findViewById(R.id.library_recycler_view);
    adapter = new LibraryRecyclerViewAdapter(libraryList, this, context);
    libraryRecyclerView.setAdapter(adapter);
    libraryRecyclerView.setListener(this);

    updateEmptyView(libraryList);
  }

  private void setupAddShelfBtn() {
    FloatingActionButton addShelfBtn = view.findViewById(R.id.add_btn);
    createAddShelfListener(addShelfBtn);
  }

  private void createAddShelfListener(FloatingActionButton addShelfBtn) {
    addShelfBtn.setOnClickListener(v -> handleAddShelf());
  }

  private Bundle createAddShelfBundle() {
    Bundle bundle = new Bundle();

    Long currentShelfId = libraryModel.getShelfId();
    if (currentShelfId == null) {
      bundle.putLong(LibraryKeys.SHELF_ID, 0L);
    } else {
      bundle.putLong(LibraryKeys.SHELF_ID, currentShelfId);
    }

    bundle.putStringArray(LibraryKeys.SHELF_NAMES, getAllShelfNames());

    return bundle;
  }

  private String[] getAllShelfNames() {
    List<ShelfItem> currentLibraryList = libraryModel.getCurrentLibraryList();
    String[] shelfNames = new String[currentLibraryList.size()];
    for (int i = 0; i < currentLibraryList.size(); i++) {
      shelfNames[i] = currentLibraryList.get(i).getName();
    }

    return shelfNames;
  }

  private void handleAddShelf() {
    LibraryFormFragment libraryFormFragment =
        new LibraryFormFragment(new LibraryFormFragment.ChangeShelfListener() {
          @Override
          public void onShelfAdded(String name, Long shelfId) {
            libraryModel.addShelf(name, libraryModel.getShelfId());
            updateLibraryListView(libraryModel.getCurrentLibraryList());
            Toast.makeText(context, getString(R.string.shelf_added), Toast.LENGTH_SHORT).show();
          }
        });

    libraryFormFragment.setArguments(createAddShelfBundle());

    showFragment(libraryFormFragment, LibraryKeys.FRAGMENT_LIBRARY_FORM);
  }

  private void updateEmptyView(List<ShelfItem> libraryList) {
    TextView emptyView = view.findViewById(R.id.list_view_library_empty);

    if (libraryList.isEmpty()) {
      emptyView.setVisibility(View.VISIBLE);
    } else {
      emptyView.setVisibility(View.GONE);
    }
  }

  private void updateLibraryListView(List<ShelfItem> libraryList) {
    libraryList = libraryModel.getSortedLibraryList(sortCriteria, libraryList);
    adapter.notifyDataSetChanged();
    updateEmptyView(libraryList);
  }

  private void updateBookListView(LibraryItem libraryItem) {
    BookFragment bookFragment = new BookFragment();
    bookFragment.setArguments(createShelfBundle(libraryItem));

    showFragment(bookFragment);
  }

  private Bundle createShelfBundle(LibraryItem libraryItem) {
    Bundle bundle = new Bundle();

    Long shelfId = libraryItem.getId();
    String shelfName = libraryItem.getName();

    bundle.putLong(LibraryKeys.SHELF_ID, shelfId);
    bundle.putString(LibraryKeys.SHELF_NAME, shelfName);

    return bundle;
  }

  @Override
  public void onShelfClicked(int position) {
    LibraryItem libraryItem = libraryModel.getSelectedLibraryItem(position);
    ((MainActivity) requireActivity()).updateHeaderFragment(libraryItem.getName());
    updateBookListView(libraryItem);
  }

  @Override
  public void onShelfLongClicked(int position, ShelfItem shelfItem, View v) {
    if (v.isSelected()) {
      v.setSelected(false);
      selectedShelfItems.remove(shelfItem);
    } else {
      v.setSelected(true);
      selectedShelfItems.add(shelfItem);
    }
  }

  private void shareLibraryBibIntent() {
    String content = exportBibTex.getBibDataLibrary(libraryModel, bookModel, noteModel);
    Uri contentUri = exportBibTex.writeTemporaryBibFile(context, content);

    Intent shareLibraryIntent =
        ShareCompat.IntentBuilder.from(requireActivity())
            .setStream(contentUri)
            .setType("text/*")
            .getIntent()
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

    startActivity(Intent.createChooser(shareLibraryIntent, "SEND"));
  }

  @Override
  public void onSwipedLeft(int position) {
    deselectLibraryItems();
    selectedShelfItems.add(adapter.getLibraryItem(position));
    handleDeleteShelf();
    adapter.notifyDataSetChanged();
  }

  @Override
  public void onSwipedRight(int position) {
    selectedShelfItems.add(adapter.getLibraryItem(position));
    handleRenameShelf();
    adapter.notifyDataSetChanged();
  }

}
