package de.bibbuddy;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
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
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.io.BufferedWriter;
import java.io.File;
import java.util.ArrayList;
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
        checkEmptyShelf();
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

  private void checkStoragePermission() {
    if (ContextCompat.checkSelfPermission(getContext(),
        Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
      requestStoragePermission();
    } else {
      // if the user has already allowed access to device external storage
      exportBibTex.createBibFile();
      exportBibTex.writeBibFile(exportBibTex.getBibDataFromShelf(shelfId, bookDao, noteDao));

      Toast.makeText(getContext(),
          getString(R.string.exported_file_stored_in) + '\n'
              + File.separator + StorageKeys.DOWNLOAD_FOLDER + File.separator
              + shelfName + StorageKeys.BIB_FILE_TYPE, Toast.LENGTH_LONG).show();
    }
  }

  private void requestStoragePermission() {
    if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
        Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
      showRequestPermissionDialog();
    } else {
      requestPermissions(new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE},
          StorageKeys.STORAGE_PERMISSION_CODE);
    }
  }

  private void showRequestPermissionDialog() {
    AlertDialog.Builder reqAlertDialog = new AlertDialog.Builder(getContext());
    reqAlertDialog.setTitle(R.string.storage_permission_needed);
    reqAlertDialog.setMessage(R.string.storage_permission_alert_msg);

    reqAlertDialog.setPositiveButton(R.string.ok,
        (dialog, which) -> ActivityCompat.requestPermissions(getActivity(),
            new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE},
            StorageKeys.STORAGE_PERMISSION_CODE));
    reqAlertDialog.setNegativeButton(R.string.cancel,
        (dialog, which) -> dialog.dismiss());

    reqAlertDialog.create().show();
  }

  /**
   * Callback method, that checks the result from requesting permissions.
   *
   * @param requestCode unique integer value for the requested permission
   *                    This value is given by the programmer.
   * @param permissions array of requested name(s)
   *                    of the permission(s)
   * @param grantResults grant results for the corresponding permissions
   *                     which is either PackageManager.PERMISSION_GRANTED
   *                     or PackageManager.PERMISSION_DENIED.
   */
  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                         @NonNull int[] grantResults) {

    if (requestCode == StorageKeys.STORAGE_PERMISSION_CODE) {

      if (grantResults.length > 0
          && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
        exportBibTex.createBibFile();
        exportBibTex.writeBibFile(exportBibTex.getBibDataFromShelf(shelfId, bookDao, noteDao));

        Toast.makeText(getContext(),
            getString(R.string.exported_file_stored_in) + '\n'
                + File.separator + StorageKeys.DOWNLOAD_FOLDER + File.separator
                + shelfName + StorageKeys.BIB_FILE_TYPE, Toast.LENGTH_LONG).show();

      } else {
        Toast.makeText(getContext(), R.string.storage_permission_denied,
            Toast.LENGTH_SHORT).show();
      }
    }
  }

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

    BookNotesViewFragment fragment = new BookNotesViewFragment();
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
