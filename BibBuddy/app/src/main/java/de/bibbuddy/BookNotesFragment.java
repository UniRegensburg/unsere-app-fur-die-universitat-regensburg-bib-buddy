package de.bibbuddy;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
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
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import java.io.File;
import java.util.List;

/**
 * BookNotesFragment is responsible for the noteList of a certain book.
 *
 * @author Sarah Kurek, Silvia Ivanova
 */
public class BookNotesFragment extends Fragment {

  private View view;
  private BookNotesViewModel bookNotesViewModel;
  private RecyclerView recyclerView;
  private NotesRecyclerViewAdapter adapter;
  private Long bookId;
  private BookDao bookDao;
  private NoteDao noteDao;
  private NoteModel noteModel;
  private List<NoteItem> noteList;
  private Context context;

  private ExportBibTex exportBibTex;
  private String fileName;
  private SortCriteria sortCriteria = SortCriteria.MOD_DATE_LATEST;

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

    view = inflater.inflate(R.layout.fragment_book_notes, container, false);
    context = view.getContext();
    bookNotesViewModel = new BookNotesViewModel(context);
    noteModel = bookNotesViewModel.getNoteModel();
    sortCriteria = SortCriteria.MOD_DATE_LATEST;

    Bundle bundle = this.getArguments();
    if (bundle != null) {
      bookId = bundle.getLong(LibraryKeys.BOOK_ID);
      String bookTitle = bundle.getString(LibraryKeys.BOOK_TITLE);
      ((MainActivity) requireActivity()).updateHeaderFragment(bookTitle);
    }

    noteList = bookNotesViewModel.getNoteList(bookId);

    setHasOptionsMenu(true);
    setupRecyclerView();
    setupAddButton();
    enableSwipeToDelete();

    updateBookNoteList(noteList);

    ((MainActivity) requireActivity()).setVisibilityImportShareButton(View.INVISIBLE, View.VISIBLE);

    setFunctionsToolbar();

    bookDao = bookNotesViewModel.getBookDao();
    noteDao = bookNotesViewModel.getNoteDao();

    fileName = (bookDao.findById(bookId).getTitle()
        + bookDao.findById(bookId).getPubYear())
        .replaceAll("\\s+", "");
    exportBibTex = new ExportBibTex(StorageKeys.DOWNLOAD_FOLDER, fileName);

    return view;
  }

  private void setupRecyclerView() {
    recyclerView = view.findViewById(R.id.book_notes_view_recycler_view);
    adapter = new NotesRecyclerViewAdapter(noteList, (MainActivity) requireActivity(), noteModel);
    recyclerView.setAdapter(adapter);
    updateEmptyView(noteList);
  }

  private void updateEmptyView(List<NoteItem> noteList) {
    TextView emptyView = view.findViewById(R.id.empty_notelist_view);
    if (noteList.isEmpty()) {
      emptyView.setVisibility(View.VISIBLE);
    } else {
      emptyView.setVisibility(View.GONE);
    }
  }

  private void setupAddButton() {
    View addButtonView = view.findViewById(R.id.btn_add_note);
    PopupMenu pm = new PopupMenu(context, addButtonView);
    pm.getMenuInflater().inflate(R.menu.add_note_menu, pm.getMenu());

    pm.setOnMenuItemClickListener(item -> {
      if (item.getItemId() == R.id.add_text_note) {
        Bundle bundle = new Bundle();
        bundle.putLong(LibraryKeys.BOOK_ID, bookId);
        TextNoteEditorFragment nextFrag = new TextNoteEditorFragment();
        nextFrag.setArguments(bundle);
        requireActivity().getSupportFragmentManager().beginTransaction()
            .replace(R.id.fragment_container_view, nextFrag,
                LibraryKeys.FRAGMENT_TEXT_NOTE_EDITOR)
            .addToBackStack(null)
            .commit();
      } else if (item.getItemId() == R.id.add_voice_note) {
        Bundle bundle = new Bundle();
        bundle.putLong(LibraryKeys.BOOK_ID, bookId);
        VoiceNoteEditorFragment nextFrag = new VoiceNoteEditorFragment();
        nextFrag.setArguments(bundle);
        requireActivity().getSupportFragmentManager().beginTransaction()
            .replace(R.id.fragment_container_view, nextFrag,
                LibraryKeys.FRAGMENT_VOICE_NOTE_EDITOR)
            .addToBackStack(null)
            .commit();
      }

      return true;
    });

    addButtonView.setOnClickListener(v -> pm.show());
  }

  private void enableSwipeToDelete() {
    SwipeToDeleteCallback swipeToDeleteCallback =
        new SwipeToDeleteCallback(context, adapter, (MainActivity) requireActivity());
    ItemTouchHelper itemTouchhelper = new ItemTouchHelper(swipeToDeleteCallback);
    itemTouchhelper.attachToRecyclerView(recyclerView);
  }

  private void updateBookNoteList(List<NoteItem> noteList) {
    sortNoteList();
    updateEmptyView(noteList);
  }

  private void setFunctionsToolbar() {
    ((MainActivity) requireActivity()).shareBtn.setOnClickListener(view -> checkEmptyNoteList());
  }

  @Override
  public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
    inflater.inflate(R.menu.fragment_book_note_menu, menu);
    super.onCreateOptionsMenu(menu, inflater);
  }

  @Override
  public void onPrepareOptionsMenu(Menu menu) {
    MenuItem deleteNote = menu.findItem(R.id.menu_delete_note);
    deleteNote.setVisible(!adapter.selectedNoteItems.isEmpty());
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int itemId = item.getItemId();
    if (itemId == R.id.menu_delete_note) {
      adapter.handleDeleteNote();
    } else if (itemId == R.id.menu_export_note) {
      checkEmptyNoteList();
    } else if (itemId == R.id.menu_book_note_sort) {
      handleSortNote();
    } else if (itemId == R.id.menu_help_book_note) {
      handleManualBookNotes();
    } else {
      Toast.makeText(context, String.valueOf(R.string.error), Toast.LENGTH_SHORT).show();
    }

    return super.onOptionsItemSelected(item);
  }

  private void checkEmptyNoteList() {
    if (bookNotesViewModel.getCurrentNoteList().isEmpty()) {
      AlertDialog.Builder alertDialogEmptyLib = new AlertDialog.Builder(context);
      alertDialogEmptyLib.setTitle(R.string.empty_note_list);
      alertDialogEmptyLib.setMessage(R.string.empty_note_list_description);

      alertDialogEmptyLib.setPositiveButton(R.string.ok,
          (dialog, which) -> {
          });

      alertDialogEmptyLib.create().show();
    } else {
      checkStoragePermission();
    }
  }

  private void checkStoragePermission() {
    if (ContextCompat.checkSelfPermission(context,
        Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
      requestStoragePermission();
    } else {
      // if the user has already allowed access to device external storage
      exportBibTex.createBibFile();
      exportBibTex.writeBibFile(exportBibTex.getBibDataFromBook(bookId, bookDao, noteDao));

      Toast.makeText(context,
          getString(R.string.exported_file_stored_in) + '\n'
              + File.separator + StorageKeys.DOWNLOAD_FOLDER + File.separator
              + fileName + StorageKeys.BIB_FILE_TYPE, Toast.LENGTH_LONG).show();
    }
  }

  private void requestStoragePermission() {
    if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(),
        Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
      showRequestPermissionDialog();
    } else {
      ActivityCompat.requestPermissions(requireActivity(),
          new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE},
          StorageKeys.STORAGE_PERMISSION_CODE);
    }
  }

  private void showRequestPermissionDialog() {
    AlertDialog.Builder reqAlertDialog = new AlertDialog.Builder(context);
    reqAlertDialog.setTitle(R.string.storage_permission_needed);
    reqAlertDialog.setMessage(R.string.storage_permission_alert_msg);

    reqAlertDialog.setPositiveButton(R.string.ok,
        (dialog, which) -> ActivityCompat.requestPermissions(requireActivity(),
            new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE},
            StorageKeys.STORAGE_PERMISSION_CODE));

    reqAlertDialog.setNegativeButton(R.string.cancel,
        (dialog, which) -> dialog.dismiss());

    reqAlertDialog.create().show();
  }

  private void handleSortNote() {
    SortDialog sortDialog = new SortDialog(context, sortCriteria,
        newSortCriteria -> {
          sortCriteria = newSortCriteria;
          sortNoteList();
        });
    sortDialog.show();
  }

  private void sortNoteList() {
    if (noteList.size() != 0) {
      List<NoteItem> sortedList = noteModel.getSortedNoteList(sortCriteria, bookId);
      adapter.setBookNoteList(sortedList);
      adapter.notifyDataSetChanged();
    }
  }

  private void handleManualBookNotes() {
    Spanned htmlAsString =
        Html.fromHtml(getString(R.string.book_note_help_text), Html.FROM_HTML_MODE_COMPACT);

    android.app.AlertDialog.Builder helpAlert = new AlertDialog.Builder(requireActivity());
    helpAlert.setCancelable(false);
    helpAlert.setTitle(R.string.help);
    helpAlert.setMessage(htmlAsString);
    helpAlert.setPositiveButton(R.string.ok, (dialog, which) -> {
    });
    helpAlert.show();
  }

  /**
   * Callback method, that checks the result from requesting permissions.
   *
   * @param requestCode  unique integer value for the requested permission
   *                     This value is given by the programmer.
   * @param permissions  array of requested name(s)
   *                     of the permission(s)
   * @param grantResults grant results for the corresponding permissions
   *                     which is either PackageManager.PERMISSION_GRANTED
   *                     or PackageManager.PERMISSION_DENIED.
   */
  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull
      int[] grantResults) {

    if (requestCode == StorageKeys.STORAGE_PERMISSION_CODE) {

      if (grantResults.length > 0
          && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
        exportBibTex.createBibFile();
        exportBibTex.writeBibFile(exportBibTex.getBibDataFromBook(bookId, bookDao, noteDao));

        Toast.makeText(context,
            getString(R.string.exported_file_stored_in) + '\n'
                + File.separator + StorageKeys.DOWNLOAD_FOLDER
                + File.separator + fileName
                + StorageKeys.BIB_FILE_TYPE, Toast.LENGTH_LONG).show();

      } else {
        Toast.makeText(context, R.string.storage_permission_denied,
            Toast.LENGTH_SHORT).show();
      }
    }
  }

}
