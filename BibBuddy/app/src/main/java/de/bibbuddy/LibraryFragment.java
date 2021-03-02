package de.bibbuddy;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

/**
 * The LibraryFragment is responsible for the shelves in the library.
 *
 * @author Claudia Schönherr, Silvia Ivanova
 */
public class LibraryFragment extends Fragment
    implements LibraryRecyclerViewAdapter.LibraryListener {

  private View view;
  private Context context;
  private LibraryModel libraryModel;
  private LibraryRecyclerViewAdapter adapter;
  private List<ShelfItem> selectedShelfItems;

  private String bibLibraryExportContent = "";
  private static final int STORAGE_PERMISSION_CODE = 1;

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {

    requireActivity().getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
      @Override
      public void handleOnBackPressed() {
          requireActivity().finish();
          requireActivity().moveTaskToBack(true);
      }
    });

    // Called to have the fragment instantiate its user interface view.
    view = inflater.inflate(R.layout.fragment_library, container, false);
    context = view.getContext();

    setupRecyclerView();
    setupAddShelfBtn();
    ((MainActivity) getActivity()).updateHeaderFragment(getString(R.string.navigation_library));

    setHasOptionsMenu(true);
    selectedShelfItems = new ArrayList<ShelfItem>();

    return view;
  }

  @Override
  public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
    inflater.inflate(R.menu.fragment_library_menu, menu);
    super.onCreateOptionsMenu(menu, inflater);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.menu_export_library:
        checkEmptyShelfList();
        break;

      case R.id.menu_rename_shelf:
        if (selectedShelfItems.size() != 1) {
          return true;
        }
        handleRenameShelf();
        break;

      case R.id.menu_delete_shelf:
        handleDeleteShelf();
        break;

      case R.id.menu_help_library:
        handleManualLibrary();
        break;

      default:
        Toast.makeText(getContext(), "??? wurde geklickt", Toast.LENGTH_SHORT).show();
    }

    return super.onOptionsItemSelected(item);
  }

  private void checkEmptyShelfList() {
    if (libraryModel.getCurrentLibraryList().isEmpty()) {

      AlertDialog.Builder alertDialogEmptyLib = new AlertDialog.Builder(getContext());
      alertDialogEmptyLib.setTitle(R.string.empty_library);
      alertDialogEmptyLib.setMessage(R.string.empty_library_description);
      alertDialogEmptyLib.setPositiveButton(R.string.ok,
          new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
          });
      alertDialogEmptyLib.create().show();

    } else {
      checkStoragePermission();
    }
  }

  private void checkStoragePermission() {
    if (ContextCompat.checkSelfPermission(getContext(),
        Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
      requestStoragePermission();
    } else {
      //if the user has already allowed access to device external storage
      createBibFile("Download", "library_export_BibBuddy");
      retrieveBibContent();
      writeBibFile("Download", "library_export_BibBuddy",
          bibLibraryExportContent);
    }
  }

  private void requestStoragePermission() {
    if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
        Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
      showRequestPermissionDialog();
    } else {
      requestPermissions(new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE},
          STORAGE_PERMISSION_CODE);
    }
  }

  private void showRequestPermissionDialog() {
    AlertDialog.Builder reqAlertDialog = new AlertDialog.Builder(getContext());
    reqAlertDialog.setTitle(R.string.storage_permission_needed);
    reqAlertDialog.setMessage(R.string.storage_permission_alert_msg);
    reqAlertDialog.setPositiveButton(R.string.ok,
        (dialog, which) -> ActivityCompat.requestPermissions(getActivity(),
            new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE},
            STORAGE_PERMISSION_CODE));
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
  public void onRequestPermissionsResult(int requestCode,
                                         String[] permissions, int[] grantResults) {

    switch (requestCode) {

      case STORAGE_PERMISSION_CODE:
        if (grantResults.length > 0
            && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
          createBibFile("Download", "library_export_BibBuddy");
          retrieveBibContent();
          writeBibFile("Download", "library_export_BibBuddy",
              bibLibraryExportContent);
        } else {
          Toast.makeText(getContext(), R.string.storage_permission_denied,
              Toast.LENGTH_SHORT).show();
        }
        break;

      default:
    }
  }

  private void createBibFile(String folderName, String fileName) {
    try {
      String rootPath = Environment.getExternalStorageDirectory() + "/" + folderName + "/";
      File root = new File(rootPath);

      if (!root.exists()) {
        root.mkdirs();
      }

      File file = new File(rootPath + fileName + ".bib");
      if (file.exists()) {
        file.delete();
      }
      file.createNewFile();

      FileOutputStream out = new FileOutputStream(file);
      out.flush();
      out.close();

      Toast.makeText(getContext(),
          getString(R.string.export_library_file_stored_in) + '\n'
          + "/" + folderName + "/" + fileName + ".bib", Toast.LENGTH_LONG).show();

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void retrieveBibContent() {
    DatabaseHelper dbHelper = new DatabaseHelper(getContext());
    List<ShelfItem> shelfItem = new ArrayList<>();
    shelfItem = libraryModel.getCurrentLibraryList();
    BookDao bd = new BookDao(dbHelper);
    NoteDao nd = new NoteDao(dbHelper);

    //for each shelf in the library
    for (int i = 0; i < shelfItem.size(); i++) {
      Long currShelfId = shelfItem.get(i).getId();

      List<Long> bookIdsCurrShelf = bd.getAllBookIdsForShelf(currShelfId);
      List<Author> authorsCurrBook = new ArrayList<>();
      List<Long> notesCurrBook = new ArrayList<>();
      String allNotesCurrBook;
      String authorNamesCurrBook;

      for (int j = 0; j < bookIdsCurrShelf.size(); j++) {

        Book currBook;
        Long currBookId = bookIdsCurrShelf.get(j);
        currBook = bd.findById(currBookId);
        allNotesCurrBook = "";
        authorNamesCurrBook = "";
        authorsCurrBook = bd.getAllAuthorsForBook(currBookId);
        notesCurrBook = nd.getAllNoteIdsForBook(currBookId);

        /*
        get the notes for the current book
        and save the bib content in one string
        */
        if (notesCurrBook.isEmpty()) {
          allNotesCurrBook = "";
        } else {
          for (int k = 0; k < notesCurrBook.size(); k++) {
            String noteTextCurrBook = nd.findTextById(notesCurrBook.get(k));
            allNotesCurrBook +=  "annote={" + noteTextCurrBook + "}," + '\n';
          }
        }

        /*
        get author's first and last name and include
        the needed book data in a bib format
        */
        if (authorsCurrBook.size() > 1) {
          for (int u = 0; u < authorsCurrBook.size(); u++) {
            authorNamesCurrBook = authorNamesCurrBook
                + authorsCurrBook.get(u).getFirstName()
                + " " + authorsCurrBook.get(u).getLastName();
            if (u < authorsCurrBook.size()) {
              authorNamesCurrBook = authorNamesCurrBook + " and ";
            }
          }
        } else {
          try {
            //if one author presented
            authorNamesCurrBook = authorsCurrBook.get(0).getFirstName()
                + " " + authorsCurrBook.get(0).getLastName();
          } catch (Exception e) {
            authorNamesCurrBook = "";
          }
        }

        //remove whitespaces from book's title
        String bookTitle = currBook.getTitle().replaceAll("\\s+", "");

        bibLibraryExportContent = bibLibraryExportContent
            + "@book{" + bookTitle + currBook.getPubYear() + "," + '\n'
            + "isbn={" + currBook.getIsbn() + "}," + '\n'
            + "author={" + authorNamesCurrBook + "}," + '\n'
            + "title={" + currBook.getTitle() + "}," + '\n'
            + "publisher={" + currBook.getPublisher() + "}," + '\n'
            + "edition={" + currBook.getEdition() + "}," + '\n'
            + allNotesCurrBook
            + "year=" + currBook.getPubYear() + '\n' + "}" + '\n' + '\n';
      }
    }
  }

  private void writeBibFile(String folderName, String fileName, String content) {
    try {
      File dir = new File(Environment.getExternalStorageDirectory()
          + "/" + folderName + "/");

      if (!dir.exists()) {
        dir.mkdirs();
      }

      File bibFile = new File(Environment.getExternalStorageDirectory()
          + "/" + folderName + "/" + fileName + ".bib");

      FileOutputStream fos = new FileOutputStream(bibFile);
      OutputStreamWriter osw = new OutputStreamWriter(fos);
      Writer fileWriter = new BufferedWriter(osw);

      fileWriter.write(content);
      fileWriter.close();

    } catch (IOException e) {
      Log.e("Exception", R.string.file_write_failed + e.toString());
    }
  }

  private void handleManualLibrary() {
    HelpFragment helpFragment = new HelpFragment();
    String htmlAsString = getString(R.string.library_help_text);

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
    alertDeleteShelf.setTitle(R.string.delete_shelf);
    alertDeleteShelf.setMessage(R.string.delete_shelf_message);

    alertDeleteShelf.setNegativeButton(R.string.back, new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
      }
    });

    alertDeleteShelf.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        libraryModel.deleteShelves(selectedShelfItems);
        adapter.notifyDataSetChanged();
        updateEmptyView(libraryModel.getCurrentLibraryList());
        Toast.makeText(context, getString(R.string.deleted_shelf), Toast.LENGTH_SHORT).show();
        unselectLibraryItems();
      }
    });

    alertDeleteShelf.show();
  }

  private Bundle createRenameShelfBundle() {
    Bundle bundle = new Bundle();

    bundle.putStringArray(LibraryKeys.SHELF_NAMES, getAllShelfNames());
    bundle.putString(LibraryKeys.SHELF_NAME, selectedShelfItems.get(0).getName());
    return bundle;
  }

  private void handleRenameShelf() {
    LibraryRenameShelfFragment fragment =
        new LibraryRenameShelfFragment(new LibraryRenameShelfFragment.RenameShelfLibraryListener() {
          @Override
          public void onShelfRenamed(String shelfName) {
            libraryModel.renameShelf(selectedShelfItems.get(0), shelfName);
            unselectLibraryItems();
            adapter.notifyDataSetChanged();
          }
        });

    fragment.setArguments(createRenameShelfBundle());
    fragment
        .show(getActivity().getSupportFragmentManager(), LibraryKeys.DIALOG_FRAGMENT_RENAME_SHELF);
  }

  private void unselectLibraryItems() {
    RecyclerView shelfListView = getView().findViewById(R.id.library_recycler_view);
    for (int i = 0; i < shelfListView.getChildCount(); i++) {
      shelfListView.getChildAt(i).setSelected(false);
    }

    selectedShelfItems.clear();
  }

  private void setupRecyclerView() {
    libraryModel = new LibraryModel(getContext());
    List<ShelfItem> libraryList = libraryModel.getLibraryList(null);

    RecyclerView libraryRecyclerView = view.findViewById(R.id.library_recycler_view);
    adapter = new LibraryRecyclerViewAdapter(libraryList, this, context);
    libraryRecyclerView.setAdapter(adapter);

    updateEmptyView(libraryList);
  }

  private void setupAddShelfBtn() {
    FloatingActionButton addShelfBtn = view.findViewById(R.id.btn_add_shelf);
    createAddShelfListener(addShelfBtn);
  }

  private void createAddShelfListener(FloatingActionButton addShelfBtn) {
    addShelfBtn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        handleAddShelf();
      }
    });
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
    LibraryAddShelfFragment fragment =
        new LibraryAddShelfFragment(new LibraryAddShelfFragment.AddShelfLibraryListener() {
          @Override
          public void onShelfAdded(String name, Long shelfId) {
            libraryModel.addShelf(name, libraryModel.getShelfId());
            updateLibraryListView(libraryModel.getCurrentLibraryList());
            unselectLibraryItems();
          }
        });

    fragment.setArguments(createAddShelfBundle());
    fragment.show(getActivity().getSupportFragmentManager(), LibraryKeys.DIALOG_FRAGMENT_ADD_NAME);
  }

  private void closeAddShelfFragment() {
    LibraryAddShelfFragment fragment =
        (LibraryAddShelfFragment) getActivity().getSupportFragmentManager()
            .findFragmentById(R.id.fragment_container_add_shelf);
    if (fragment != null) {
      fragment.closeFragment();
    }
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
    adapter.notifyDataSetChanged();
    updateEmptyView(libraryList);
  }

  private void updateBookListView(LibraryItem libraryItem) {
    BookFragment fragment = new BookFragment();
    fragment.setArguments(createShelfBundle(libraryItem));

    getActivity().getSupportFragmentManager().beginTransaction()
        .replace(R.id.fragment_container_view, fragment)
        .setReorderingAllowed(true)
        .addToBackStack(null)
        .commit();
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
  public void onItemClicked(int position) {
    closeAddShelfFragment();

    LibraryItem libraryItem = libraryModel.getSelectedLibraryItem(position);
    ((MainActivity) getActivity()).updateHeaderFragment(libraryItem.getName());
    updateBookListView(libraryItem);
  }

  @Override
  public void onLongItemClicked(int position, ShelfItem shelfItem, View v) {
    closeAddShelfFragment();

    if (v.isSelected()) {
      v.setSelected(false);
      selectedShelfItems.remove(shelfItem);
    } else {
      v.setSelected(true);
      selectedShelfItems.add(shelfItem);
    }
  }
}
