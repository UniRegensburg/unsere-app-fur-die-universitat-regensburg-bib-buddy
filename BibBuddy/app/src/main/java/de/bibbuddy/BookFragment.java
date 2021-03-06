package de.bibbuddy;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ShareCompat;
import androidx.core.content.ContextCompat;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.tsuryo.swipeablerv.SwipeLeftRightCallback;
import com.tsuryo.swipeablerv.SwipeableRecyclerView;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * The BookFragment is responsible for the current books of a shelf in the library.
 *
 * @author Claudia Schönherr, Silvia Ivanova, Luis Moßburger
 */
public class BookFragment extends BackStackFragment implements BookRecyclerViewAdapter.BookListener,
    BookFormFragment.ChangeBookListener, SwipeLeftRightCallback.Listener {

  private static final String TAG = BookFragment.class.getSimpleName();

  private final List<BookItem> selectedBookItems = new ArrayList<>();

  private final ActivityResultLauncher<String> requestStoragePermissionLauncher =
      registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
        if (isGranted) {
          filePicker();
        } else {
          Toast.makeText(requireContext(),
                  R.string.permission_denied, Toast.LENGTH_SHORT).show();
        }
      });

  private final ActivityResultLauncher<Intent> filePickerActivityResultLauncher =
      registerForActivityResult(
          new ActivityResultContracts.StartActivityForResult(),
          new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
              if (result.getResultCode() == Activity.RESULT_OK) {
                Intent data = result.getData();

                if (data != null) {
                  Uri uri = data.getData();

                  if (importBibTex.isBibFile(
                      Objects.requireNonNull(UriUtils.getFullUriPath(context, uri)))) {
                    handleImport(uri);
                  } else {
                    showDialogNonBibFile();
                  }
                }
              }
            }
          });

  private final ActivityResultLauncher<String> requestCameraPermissionLauncher =
          registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) {
              handleAddBookBarcodeFragment();
            } else {
              Toast.makeText(requireContext(),
                      R.string.permission_denied, Toast.LENGTH_SHORT).show();
            }
          });

  private View view;
  private Context context;
  private BookModel bookModel;
  private BookRecyclerViewAdapter adapter;

  private Long shelfId;
  private String shelfName;

  private ImportBibTex importBibTex;

  private SortTypeLut sortTypeLut;

  private void setupMainActivity() {
    MainActivity mainActivity = ((MainActivity) requireActivity());

    mainActivity.setVisibilityImportShareBtn(View.VISIBLE, View.VISIBLE);
    mainActivity.setVisibilitySortBtn(true);
    sortTypeLut = mainActivity.getSortTypeLut();

    mainActivity.findViewById(R.id.import_btn).setOnClickListener(
        view -> checkStoragePermission());
    mainActivity.findViewById(R.id.share_btn).setOnClickListener(view -> checkEmptyShelf());

    mainActivity.updateHeaderFragment(shelfName);
    mainActivity.updateNavigationFragment(R.id.navigation_library);
  }

  private void setupSortBtn() {
    ImageButton sortBtn = requireActivity().findViewById(R.id.sort_btn);
    sortBtn.setOnClickListener(v -> handleSortBook());
  }

  private void setupDefaultApp() {
    MainActivity mainActivity = ((MainActivity) requireActivity());
    Uri uri = mainActivity.getUriDefaultApp();

    if (mainActivity.isDefaultApp()) {
      handleImport(uri);
      updateBookList(bookModel.getCurrentBookList());
    }

    mainActivity.resetIsDefaultApp();
  }

  private void setupRecyclerView() {
    SwipeableRecyclerView recyclerView = view.findViewById(R.id.book_recycler_view);

    List<BookItem> bookList = bookModel.getSortedBookList(sortTypeLut,
                                                          bookModel.getBookList(shelfId));

    adapter = new BookRecyclerViewAdapter(bookList, this, requireContext());
    recyclerView.setAdapter(adapter);
    recyclerView.setListener(this);

    updateEmptyView(bookList);
  }

  private void handleChangeBookData() {
    if (selectedBookItems.isEmpty()) {
      return;
    }

    Bundle bundle = new Bundle();
    BookItem bookItem = selectedBookItems.get(0);
    bundle.putString(LibraryKeys.SHELF_NAME, shelfName);
    bundle.putLong(LibraryKeys.SHELF_ID, shelfId);
    bundle.putLong(LibraryKeys.BOOK_ID, bookItem.getId());

    BookFormFragment bookFormFragment = new BookFormFragment(this);
    bookFormFragment.setArguments(bundle);

    showFragment(bookFormFragment, LibraryKeys.FRAGMENT_BOOK);
  }

  private void updateBookList(List<BookItem> bookList) {
    bookList = bookModel.getSortedBookList(sortTypeLut, bookList);

    adapter.setBookList(bookList);
    adapter.notifyDataSetChanged();

    updateEmptyView(bookList);
  }

  private void handleDeleteBook() {
    AlertDialog.Builder alertDeleteBook = new AlertDialog.Builder(context);
    alertDeleteBook.setCancelable(false);

    if (selectedBookItems.size() > 1) {
      alertDeleteBook.setTitle(R.string.delete_books);
      alertDeleteBook.setMessage(
          getString(R.string.delete_books_message) + assembleAlertString());
    } else {
      alertDeleteBook.setTitle(R.string.delete_book);
      alertDeleteBook.setMessage(
          getString(R.string.delete_book_message) + assembleAlertString());
    }

    alertDeleteBook.setNegativeButton(R.string.cancel, (dialog, which) -> deselectBookItems());

    alertDeleteBook.setPositiveButton(R.string.delete, (dialog, which) -> {
      performDeleteBook();
      deselectBookItems();
    });

    alertDeleteBook.show();
  }

  private String assembleAlertString() {
    return convertBookListToString(selectedBookItems)
        + getString(R.string.delete_counter_msg)
        + getNotesToDeleteNumber(selectedBookItems) + " "
        + getString(R.string.finally_delete) + " "
        + getString(R.string.delete_warning);
  }

  private String convertBookListToString(List<BookItem> bookList) {
    StringBuilder books = new StringBuilder();

    int counter = 1;
    for (BookItem book : bookList) {
      books.append(" \"").append(book.getName()).append("\"");

      if (counter != bookList.size()) {
        books.append(",");
      }
    }

    books.append(" ");
    ++counter;

    return books.toString();
  }

  private String getNotesToDeleteNumber(List<BookItem> bookList) {
    int notesNumber = 0;
    for (BookItem book : bookList) {
      notesNumber += book.getNoteCount();
    }

    if (notesNumber == 1) {
      return " " + getString(R.string.one) + " " + getString(R.string.note);
    }

    return " " + notesNumber + " " + getString(R.string.notes);
  }

  private void performDeleteBook() {
    final int booksNumber = selectedBookItems.size();

    bookModel.deleteBooks(selectedBookItems, shelfId);
    updateBookList(bookModel.getCurrentBookList());

    if (booksNumber > 1) {
      Toast.makeText(context, getString(R.string.deleted_books), Toast.LENGTH_SHORT).show();
    } else {
      Toast.makeText(context, getString(R.string.deleted_book), Toast.LENGTH_SHORT).show();
    }
  }

  private void handleSortBook() {
    SortDialog sortDialog = new SortDialog(context, sortTypeLut,
        newSortCriteria -> {
          sortTypeLut = newSortCriteria;
          ((MainActivity) requireActivity())
          .setSortTypeLut(newSortCriteria);
          sortBookList();
        });

    sortDialog.show();
  }

  private void sortBookList() {
    List<BookItem> bookList = bookModel.getSortedBookList(sortTypeLut);
    adapter.setBookList(bookList);
    adapter.notifyDataSetChanged();
  }

  private void checkEmptyShelf() {
    if (bookModel.getAllBooksForShelf(shelfId).isEmpty()) {
      AlertDialog.Builder alertDialogEmptyShelf = new AlertDialog.Builder(requireContext());
      alertDialogEmptyShelf.setTitle(R.string.empty_shelf);
      alertDialogEmptyShelf.setMessage(R.string.empty_shelf_description);

      alertDialogEmptyShelf.setPositiveButton(R.string.ok,
          (dialog, which) -> {
        });

      alertDialogEmptyShelf.create().show();
    } else {
      shareShelfBibIntent();
    }
  }

  private void handleImport(Uri uri) {
    String bibText = readBibFile(uri);

    if (bibText != null) {
      List<String> nonRedundantBibItems
          = importBibTex.getNonRedundantBibItems(bibText);

      NoteModel noteModel = new NoteModel(context);
      for (int i = 0; i < nonRedundantBibItems.size(); i++) {

        if (nonRedundantBibItems.get(i).startsWith(BibTexKeys.BOOK_TAG)) {
          importBibTex.parseBibItem(nonRedundantBibItems.get(i));
          Book book = importBibTex.importBook();
          addImportedBook(book, importBibTex.parseAuthorNames());
          importBibTex.importBibNote(noteModel, book);
        }

      }

      Toast.makeText(context, getString(R.string.imported_file_name_is) + '\n'
          + UriUtils.getUriFileName(requireActivity(), uri), Toast.LENGTH_LONG).show();

    } else {
      Toast.makeText(context, getString(R.string.not_valid_bib_file),
                     Toast.LENGTH_LONG).show();
    }

  }

  private void addImportedBook(Book book, List<Author> authorList) {
    if (importBibTex.existsBibNote()) {
      bookModel.addBook(book, authorList);

    } else {
      bookModel.addImportedBook(book, authorList);
    }

    adapter.setBookList(bookModel.getCurrentBookList());
    adapter.notifyDataSetChanged();
    updateEmptyView(bookModel.getCurrentBookList());
  }

  private String readBibFile(Uri uri) {
    try {
      importBibTex.readTextFromUri(uri);
      return importBibTex.readTextFromUri(uri);
    } catch (IOException ex) {
      Log.e(TAG, ex.toString(), ex);
    }

    return null;
  }

  private void filePicker() {
    Intent filePickerIntent = new Intent(Intent.ACTION_GET_CONTENT);
    filePickerIntent.setType("*/*");
    filePickerActivityResultLauncher.launch(filePickerIntent);
  }

  private void showDialogNonBibFile() {
    AlertDialog.Builder nonBibFileAlertDialog = new AlertDialog.Builder(context);

    nonBibFileAlertDialog.setTitle(R.string.import_non_bib_file);
    nonBibFileAlertDialog.setMessage(R.string.import_non_bib_file_description);

    nonBibFileAlertDialog.setPositiveButton(R.string.ok,
        (dialog, which) -> filePicker());

    nonBibFileAlertDialog.setNegativeButton(R.string.cancel,
        (dialog, which) -> dialog.dismiss());

    nonBibFileAlertDialog.create().show();
  }

  private void checkStoragePermission() {
    if (ContextCompat.checkSelfPermission(requireContext(),
                                          Manifest.permission.READ_EXTERNAL_STORAGE)
        == PackageManager.PERMISSION_GRANTED) {

      filePicker();

    } else if (shouldShowRequestPermissionRationale(
        Manifest.permission.READ_EXTERNAL_STORAGE)) {

      showRequestStoragePermissionDialog();

    } else {

      requestStoragePermissionLauncher.launch(
          Manifest.permission.READ_EXTERNAL_STORAGE);
    }
  }

  private void checkCameraPermission() {
    if (ContextCompat.checkSelfPermission(
            context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {

      handleAddBookBarcodeFragment();
    } else if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
      showRequestCameraPermissionDialog();
    } else {
      requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA);
    }
  }

  private void showRequestStoragePermissionDialog() {
    AlertDialog.Builder reqAlertDialog = new AlertDialog.Builder(requireContext());

    reqAlertDialog.setTitle(R.string.storage_permission_needed);
    reqAlertDialog.setMessage(R.string.storage_permission_alert_msg);

    reqAlertDialog
        .setPositiveButton(R.string.ok,
          (dialog, which) -> ActivityCompat
                               .requestPermissions(requireActivity(),
                                                   new String[] {
                                                       Manifest.permission.READ_EXTERNAL_STORAGE},
                                                   StorageKeys.STORAGE_PERMISSION_CODE
                               ));

    reqAlertDialog.setNegativeButton(R.string.cancel,
        (dialog, which) -> dialog.dismiss());

    reqAlertDialog.create().show();
  }

  private void showRequestCameraPermissionDialog() {
    AlertDialog.Builder reqAlertDialog = new AlertDialog.Builder(context);
    reqAlertDialog.setTitle(R.string.camera_permission_needed);
    reqAlertDialog.setMessage(R.string.camera_permission_alert_msg);

    reqAlertDialog.setPositiveButton(R.string.ok,
        (dialog, which) -> requestCameraPermissionLauncher.launch(
                    Manifest.permission.CAMERA));

    reqAlertDialog.setNegativeButton(R.string.cancel,
        (dialog, which) -> dialog.dismiss());

    reqAlertDialog.create().show();
  }

  private void handleManualBook() {
    String htmlAsString = getString(R.string.book_help_text);

    Bundle bundle = new Bundle();
    bundle.putString(LibraryKeys.MANUAL_TEXT, htmlAsString);

    HelpFragment helpFragment = new HelpFragment();
    helpFragment.setArguments(bundle);

    helpFragment
        .show(requireActivity().getSupportFragmentManager(), LibraryKeys.FRAGMENT_HELP_VIEW);
  }

  private Bundle createBookBundle(LibraryItem item) {
    Bundle bundle = new Bundle();

    Long currentBookId = item.getId();
    String currentBookTitle = item.getName();

    bundle.putLong(LibraryKeys.SHELF_ID, shelfId);
    bundle.putString(LibraryKeys.SHELF_NAME, shelfName);

    bundle.putLong(LibraryKeys.BOOK_ID, currentBookId);
    bundle.putString(LibraryKeys.BOOK_TITLE, currentBookTitle);

    return bundle;
  }

  private Bundle createBookBundle() {
    Bundle bundle = new Bundle();
    bundle.putString(LibraryKeys.SHELF_NAME, shelfName);
    bundle.putLong(LibraryKeys.SHELF_ID, shelfId);

    return bundle;
  }

  private void updateEmptyView(List<BookItem> bookList) {
    TextView emptyView = view.findViewById(R.id.list_view_book_empty);
    if (bookList.isEmpty()) {
      emptyView.setVisibility(View.VISIBLE);
    } else {
      emptyView.setVisibility(View.GONE);
    }
  }

  private void createAddBookListener() {
    FloatingActionButton addBookBtn = view.findViewById(R.id.add_btn);
    PopupMenu popupMenu = new PopupMenu(requireContext(), addBookBtn);
    popupMenu.getMenuInflater().inflate(R.menu.add_book_menu, popupMenu.getMenu());

    popupMenu.setOnMenuItemClickListener(item -> {
      if (item.getItemId() == R.id.add_book_scan) {
        checkCameraPermission();
      } else if (item.getItemId() == R.id.add_book_online) {
        handleAddBookOnline();
      } else if (item.getItemId() == R.id.add_book_manually) {
        handleAddBookManually();
      }

      return true;
    });

    addBookBtn.setOnClickListener(v -> popupMenu.show());
  }

  private void handleAddBookOnline() {
    BookOnlineFragment bookOnlineFragment = new BookOnlineFragment();
    bookOnlineFragment.setArguments(createBookBundle());

    showFragment(bookOnlineFragment);
  }

  private void handleAddBookManually() {
    BookFormFragment bookFormFragment = new BookFormFragment(
        new BookFormFragment.ChangeBookListener() {
          @Override
          public void onBookAdded(Book book, List<Author> authorList) {
            addBook(book, authorList);
            Toast.makeText(requireContext(), getString(R.string.added_book), Toast.LENGTH_SHORT)
                .show();
          }
        });

    bookFormFragment.setArguments(createBookBundle());

    showFragment(bookFormFragment);
  }

  private void addBook(Book book, List<Author> authorList) {
    bookModel.addBook(book, authorList);
    Toast.makeText(requireContext(), getString(R.string.added_book), Toast.LENGTH_SHORT).show();
    updateBookList(bookModel.getCurrentBookList());
  }

  private void handleAddBookBarcodeFragment() {
    BookBarcodeScannerFragment bookBarcodeScannerFragment = new BookBarcodeScannerFragment();
    bookBarcodeScannerFragment.setArguments(createBookBundle());

    showFragment(bookBarcodeScannerFragment, LibraryKeys.FRAGMENT_BARCODE_SCANNER);
  }

  private void deselectBookItems() {
    SwipeableRecyclerView bookListView = requireView().findViewById(R.id.book_recycler_view);
    for (int i = 0; i < bookListView.getChildCount(); i++) {
      bookListView.getChildAt(i).setSelected(false);
    }

    selectedBookItems.clear();
  }

  private void shareShelfBibIntent() {
    NoteModel noteModel = new NoteModel(context);
    ShareBibTex shareBibTex = new ShareBibTex(shelfName);
    String bibContent =
        shareBibTex.getBibDataFromShelf(shelfId, bookModel, noteModel);

    Uri contentUri = shareBibTex.writeTemporaryBibFile(context, bibContent);

    Intent shareShelfIntent =
        ShareCompat.IntentBuilder.from(requireActivity())
            .setStream(contentUri)
            .setType("text/*")
            .getIntent()
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

    startActivity(Intent.createChooser(shareShelfIntent, "SEND"));
  }

  @Override
  protected void onBackPressed() {
    if (selectedBookItems.isEmpty()) {
      closeFragment();
    } else {
      deselectBookItems();
    }
  }

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {

    enableBackPressedHandler();

    view = inflater.inflate(R.layout.fragment_book, container, false);
    context = view.getContext();

    Bundle bundle = this.getArguments();
    assert bundle != null;
    shelfName = bundle.getString(LibraryKeys.SHELF_NAME);
    shelfId = bundle.getLong(LibraryKeys.SHELF_ID);

    setupMainActivity();

    bookModel = new BookModel(context, shelfId);

    importBibTex = new ImportBibTex(context);

    setupRecyclerView();

    createAddBookListener();
    setupSortBtn();
    setupDefaultApp();

    selectedBookItems.clear();

    setHasOptionsMenu(true);

    return view;
  }


  @Override
  public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
    inflater.inflate(R.menu.fragment_book_menu, menu);
    super.onCreateOptionsMenu(menu, inflater);
  }

  @SuppressLint("NonConstantResourceId")
  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.menu_change_book_data:
        handleChangeBookData();
        break;

      case R.id.menu_delete_book:
        handleDeleteBook();
        break;

      case R.id.menu_help_book:
        handleManualBook();
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
  public void onBookChanged(Book book, List<Author> authorList) {
    bookModel.updateBook(book, authorList);
    updateBookList(bookModel.getBookList(shelfId));
  }

  @Override
  public void onBookClicked(int position) {
    BookItem bookItem = bookModel.getSelectedBookItem(position);

    BookNotesFragment bookNotesFragment = new BookNotesFragment();
    bookNotesFragment.setArguments(createBookBundle(bookItem));

    showFragment(bookNotesFragment);
  }

  @Override
  public void onPrepareOptionsMenu(Menu menu) {
    MenuItem changeBookData = menu.findItem(R.id.menu_change_book_data);
    MenuItem deleteBook = menu.findItem(R.id.menu_delete_book);

    if (selectedBookItems == null || selectedBookItems.isEmpty()) {
      changeBookData.setVisible(false);
      deleteBook.setVisible(false);
    } else if (selectedBookItems.size() == 1) {
      changeBookData.setVisible(true);
      deleteBook.setVisible(true);
    } else {
      changeBookData.setVisible(false);
      deleteBook.setVisible(true);
    }
  }

  @Override
  public void onBookLongClicked(BookItem bookItem, View v) {
    if (v.isSelected()) {
      v.setSelected(false);
      selectedBookItems.remove(bookItem);
    } else {
      v.setSelected(true);
      selectedBookItems.add(bookItem);
    }
  }

  @Override
  public void onSwipedLeft(int position) {
    deselectBookItems();
    selectedBookItems.add(adapter.getBookItem(position));
    handleDeleteBook();
    adapter.notifyDataSetChanged();
  }

  @Override
  public void onSwipedRight(int position) {
    selectedBookItems.add(adapter.getBookItem(position));
    handleChangeBookData();
  }

}
