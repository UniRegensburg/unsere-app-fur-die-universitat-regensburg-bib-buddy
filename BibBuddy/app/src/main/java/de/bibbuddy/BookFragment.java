package de.bibbuddy;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * The BookFragment is responsible for the current books of a shelf in the library.
 *
 * @author Claudia Schönherr, Silvia Ivanova
 */
public class BookFragment extends Fragment implements BookRecyclerViewAdapter.BookListener,
    BookFormFragment.ChangeBookListener {
  private Long shelfId;
  private String shelfName;
  private View view;
  private Context context;

  private BookModel bookModel;
  private BookRecyclerViewAdapter adapter;
  private List<BookItem> selectedBookItems;

  private BookDao bookDao;
  private NoteDao noteDao;

  private ExportBibTex exportBibTex;
  private ImportBibTex importBibTex;

  private String[] storageManifestPermissions = {
      Manifest.permission.WRITE_EXTERNAL_STORAGE,
      Manifest.permission.READ_EXTERNAL_STORAGE };

  private String [] bibTags = { BibTexKeys.ISBN, BibTexKeys.AUTHOR, BibTexKeys.BOOK_TITLE,
      BibTexKeys.SUBTITLE, BibTexKeys.VOLUME, BibTexKeys.PUBLISHER,
      BibTexKeys.EDITION, BibTexKeys.ANNOTE, BibTexKeys.YEAR };

  private HashMap<String, String> bibTagValue;

  private Intent filePickerIntent;

  private boolean isImport = false; // it is either import or export


  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {

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

    view = inflater.inflate(R.layout.fragment_book, container, false);
    context = view.getContext();

    Bundle bundle = this.getArguments();
    shelfName = bundle.getString(LibraryKeys.SHELF_NAME);
    shelfId = bundle.getLong(LibraryKeys.SHELF_ID);
    bookModel = new BookModel(getContext(), shelfId);

    List<BookItem> bookList;
    bookList = bookModel.getBookList(shelfId);

    bookDao = bookModel.getBookDao();
    noteDao = bookModel.getNoteDao();

    exportBibTex = new ExportBibTex(StorageKeys.DOWNLOAD_FOLDER, shelfName);

    bibTagValue = new HashMap<>();
    importBibTex = new ImportBibTex(context, bibTags, bibTagValue);

    RecyclerView recyclerView = view.findViewById(R.id.book_recycler_view);
    adapter = new BookRecyclerViewAdapter(bookList, this, getContext());
    recyclerView.setAdapter(adapter);

    setHasOptionsMenu(true);
    createAddBookListener();
    updateEmptyView(bookList);
    ((MainActivity) getActivity()).updateHeaderFragment(shelfName);
    selectedBookItems = new ArrayList<>();

    return view;
  }

  @Override
  public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
    inflater.inflate(R.menu.fragment_book_menu, menu);
    super.onCreateOptionsMenu(menu, inflater);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {

    switch (item.getItemId()) {
      case R.id.menu_change_book_data:
        handleChangeBookData();
        break;

      case R.id.menu_delete_book:
        handleDeleteBook();
        break;

      case R.id.menu_export_shelf:
        isImport = false;
        checkEmptyShelf();
        break;

      case R.id.menu_import_in_shelf:
        isImport = true;
        checkStoragePermission();
        break;

      case R.id.menu_help_book:
        handleManualBook();
        break;

      default:
        Toast.makeText(getContext(), "Fehler", Toast.LENGTH_SHORT).show();
    }

    return super.onOptionsItemSelected(item);
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
    getActivity().getSupportFragmentManager().beginTransaction()
        .replace(R.id.fragment_container_view, bookFormFragment, LibraryKeys.FRAGMENT_BOOK)
        .addToBackStack(null)
        .commit();
  }

  @Override
  public void onBookChanged(Book book, List<Author> authorList) {
    bookModel.updateBook(book, authorList);

    adapter.setBookList(bookModel.getBookList(shelfId));
    adapter.notifyDataSetChanged();
  }

  private void handleDeleteBook() {
    AlertDialog.Builder alertDeleteBook = new AlertDialog.Builder(context);

    alertDeleteBook.setCancelable(false);
    alertDeleteBook.setTitle(R.string.delete_book);
    alertDeleteBook.setMessage(R.string.delete_book_message);

    alertDeleteBook.setNegativeButton(R.string.back, new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
      }
    });

    alertDeleteBook.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        bookModel.deleteBooks(selectedBookItems, shelfId);

        adapter.setBookList(bookModel.getCurrentBookList());
        adapter.notifyDataSetChanged();

        updateEmptyView(bookModel.getCurrentBookList());
        Toast.makeText(context, getString(R.string.deleted_book), Toast.LENGTH_SHORT).show();
        unselectBookItems();
      }
    });

    alertDeleteBook.show();
  }

  private void checkEmptyShelf() {
    if (bookDao.getAllBooksForShelf(shelfId).isEmpty()) {
      AlertDialog.Builder alertDialogEmptyShelf = new AlertDialog.Builder(getContext());
      alertDialogEmptyShelf.setTitle(R.string.empty_shelf);
      alertDialogEmptyShelf.setMessage(R.string.empty_shelf_description);

      alertDialogEmptyShelf.setPositiveButton(R.string.ok,
          new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
          });

      alertDialogEmptyShelf.create().show();
    } else {
      checkStoragePermission();
    }
  }

  ActivityResultLauncher<Intent> filePickerActivityResultLauncher = registerForActivityResult(
      new ActivityResultContracts.StartActivityForResult(),
      new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
          if (result.getResultCode() == Activity.RESULT_OK) {
            Intent data = result.getData();

            if (data != null) {

              Uri uri = data.getData();
              if (importBibTex.isBibFile(UriUtils.getFullUriPath(context, uri))) {

                handleImport(uri);

                Toast.makeText(context, getString(R.string.imported_file_name_is) + '\n'
                    + UriUtils.getUriFileName(getActivity(), uri), Toast.LENGTH_LONG).show();

              } else {
                showDialogNonBibFile();
              }
            }
          }
        }
      });

  private void handleImport(Uri uri) {
    String bibText = readBibFile(uri);

    List<String> nonRedundantBibItems
        = importBibTex.getNonRedundantBibItems(bibText);

    Book book;
    for (int i = 0; i < nonRedundantBibItems.size(); i++) {

      importBibTex.parseBibItem(nonRedundantBibItems.get(i));
      book = importBibTex.importBook();
      addImportedBook(book, importBibTex.parseAuthorNames());
      importBibTex.importBibNote(noteDao, book);

    }

  }

  private void addImportedBook(Book book, List<Author> authorList) {

    if (bibTagValue.get(BibTexKeys.ANNOTE).isEmpty()) {
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
      return importBibTex.readTextFromUri(uri);

    } catch (IOException e) {
      e.printStackTrace();
    }

    return null;
  }

  private void filePicker()  {
    filePickerIntent = new Intent(Intent.ACTION_GET_CONTENT);
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
    // if the permissions are granted
    if (ContextCompat.checkSelfPermission(getContext(), storageManifestPermissions[0])
        +  ContextCompat.checkSelfPermission(getContext(),
        storageManifestPermissions[1]) == PackageManager.PERMISSION_GRANTED) {

      // if the Export is selected
      if (!isImport) {
        exportBibTex.createBibFile();
        exportBibTex.writeBibFile(exportBibTex.getBibDataFromShelf(shelfId, bookDao, noteDao));

        Toast.makeText(getContext(),
            getString(R.string.exported_file_stored_in) + '\n'
                + File.separator + StorageKeys.DOWNLOAD_FOLDER + File.separator
                + shelfName + StorageKeys.BIB_FILE_TYPE, Toast.LENGTH_LONG).show();

      } else {
        // if the Import is selected
        filePicker();
      }
      // if the permissions are not granted
    } else if (shouldShowRequestPermissionRationale(storageManifestPermissions[0])
        || shouldShowRequestPermissionRationale(storageManifestPermissions[1])) {
      showRequestPermissionDialog();

    } else {
      requestPermissionLauncher.launch(storageManifestPermissions[0]);
      requestPermissionLauncher.launch(storageManifestPermissions[1]);
    }
  }

  private void showRequestPermissionDialog() {
    AlertDialog.Builder reqAlertDialog = new AlertDialog.Builder(getContext());

    reqAlertDialog.setTitle(R.string.storage_permission_needed);
    reqAlertDialog.setMessage(R.string.storage_permission_alert_msg);

    reqAlertDialog.setPositiveButton(R.string.ok,
        (dialog, which) -> ActivityCompat.requestPermissions(getActivity(),
            new String[] { storageManifestPermissions[0], storageManifestPermissions[1] },
            StorageKeys.STORAGE_PERMISSION_CODE));

    reqAlertDialog.setNegativeButton(R.string.cancel,
        (dialog, which) -> dialog.dismiss());

    reqAlertDialog.create().show();
  }

  private final ActivityResultLauncher<String> requestPermissionLauncher =
      registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
        if (isGranted) {
          if (!isImport) {

            exportBibTex.createBibFile();
            exportBibTex.writeBibFile(exportBibTex.getBibDataFromShelf(shelfId, bookDao, noteDao));

            Toast.makeText(getContext(),
                getString(R.string.exported_file_stored_in) + '\n'
                    + File.separator + StorageKeys.DOWNLOAD_FOLDER + File.separator
                    + shelfName + StorageKeys.BIB_FILE_TYPE, Toast.LENGTH_LONG).show();

          } else {
            filePicker();
          }

        }  else {
          Toast.makeText(getContext(), R.string.storage_permission_denied,
              Toast.LENGTH_SHORT).show();
        }
      });

  private void handleManualBook() {
    HelpFragment helpFragment = new HelpFragment();
    String htmlAsString = getString(R.string.book_help_text);

    Bundle bundle = new Bundle();

    bundle.putString(LibraryKeys.MANUAL_TEXT, htmlAsString);
    helpFragment.setArguments(bundle);

    getActivity().getSupportFragmentManager().beginTransaction()
        .replace(R.id.fragment_container_view, helpFragment,
            LibraryKeys.FRAGMENT_HELP_VIEW)
        .addToBackStack(null)
        .commit();
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

  @Override
  public void onItemClicked(int position) {
    BookItem bookItem = bookModel.getSelectedBookItem(position);
    ((MainActivity) getActivity()).updateHeaderFragment(bookItem.getName());

    BookNotesView fragment = new BookNotesView();
    getActivity().getSupportFragmentManager().beginTransaction()
        .replace(R.id.fragment_container_view, fragment)
        .setReorderingAllowed(true)
        .addToBackStack(null)
        .commit();

    fragment.setArguments(createBookBundle(bookItem));
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
    FloatingActionButton addBookBtn = view.findViewById(R.id.btn_add_book);
    PopupMenu pm = new PopupMenu(getContext(), addBookBtn);
    pm.getMenuInflater().inflate(R.menu.add_book_menu, pm.getMenu());

    pm.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
      @Override
      public boolean onMenuItemClick(MenuItem item) {
        if (item.getItemId() == R.id.add_book_scan) {
          handleAddBookBarcodeFragment();
        } else if (item.getItemId() == R.id.add_book_online) {
          handleAddBookOnline();
        } else if (item.getItemId() == R.id.add_book_manually) {
          handleAddBookManually();
        }

        return true;
      }
    });


    addBookBtn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        pm.show();
      }
    });
  }

  private void handleAddBookOnline() {
    BookOnlineFragment fragment = new BookOnlineFragment();

    getActivity().getSupportFragmentManager().beginTransaction()
        .replace(R.id.fragment_container_view, fragment)
        .setReorderingAllowed(true)
        .addToBackStack(null)
        .commit();

    fragment.setArguments(createBookBundle());
  }

  private void handleAddBookManually() {
    BookFormFragment fragment = new BookFormFragment(
        new BookFormFragment.ChangeBookListener() {
          @Override
          public void onBookAdded(Book book, List<Author> authorList) {
            addBook(book, authorList);
            Toast.makeText(getContext(), getString(R.string.added_book), Toast.LENGTH_SHORT).show();
          }
        });

    getActivity().getSupportFragmentManager().beginTransaction()
        .replace(R.id.fragment_container_view, fragment)
        .setReorderingAllowed(true)
        .addToBackStack(null)
        .commit();

    fragment.setArguments(createBookBundle());
  }

  private void addBook(Book book, List<Author> authorList) {
    bookModel.addBook(book, authorList);
    Toast.makeText(getContext(), getString(R.string.added_book), Toast.LENGTH_SHORT).show();
    adapter.setBookList(bookModel.getCurrentBookList());
    adapter.notifyDataSetChanged();
    updateEmptyView(bookModel.getCurrentBookList());
  }

  private void handleAddBookBarcodeFragment() {
    BookBarcodeScannerFragment fragment = new BookBarcodeScannerFragment();
    getActivity().getSupportFragmentManager().beginTransaction()
        .replace(R.id.fragment_container_view, fragment, LibraryKeys.FRAGMENT_BARCODE_SCANNER)
        .addToBackStack(null)
        .commit();

    fragment.setArguments(createBookBundle());
  }

  private void unselectBookItems() {
    RecyclerView bookListView = getView().findViewById(R.id.book_recycler_view);
    for (int i = 0; i < bookListView.getChildCount(); i++) {
      bookListView.getChildAt(i).setSelected(false);
    }

    selectedBookItems.clear();
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
  public void onLongItemClicked(int position, BookItem bookItem, View v) {
    if (v.isSelected()) {
      v.setSelected(false);
      selectedBookItems.remove(bookItem);
    } else {
      v.setSelected(true);
      selectedBookItems.add(bookItem);
    }
  }

}