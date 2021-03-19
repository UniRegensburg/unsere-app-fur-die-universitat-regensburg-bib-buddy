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
import android.widget.ImageButton;
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
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * BookNotesView is responsible for the noteList of a certain book.
 *
 * @author Sarah Kurek, Silvia Ivanova, Luis Moßburger
 */
public class BookNotesView extends Fragment
    implements BookNotesRecyclerViewAdapter.BookNotesViewListener {

  private View view;
  private Context context;
  private BookNotesViewModel bookNotesViewModel;
  private BookNotesRecyclerViewAdapter adapter;
  private Long bookId;
  private List<NoteItem> noteList;
  private List<NoteItem> selectedNoteItems;

  private BookDao bookDao;
  private NoteDao noteDao;

  private ExportBibTex exportBibTex;
  private String fileName;

  private SortCriteria sortCriteria;


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

    Bundle bundle = this.getArguments();
    bookId = bundle.getLong(LibraryKeys.BOOK_ID);

    view = inflater.inflate(R.layout.fragment_book_notes, container, false);
    context = view.getContext();

    sortCriteria = ((MainActivity) getActivity()).getSortCriteria();

    setupRecyclerView(bookId);

    setHasOptionsMenu(true);
    setupAddButton();

    updateBookNoteList(noteList);

    String bookTitle = bundle.getString(LibraryKeys.BOOK_TITLE);

    ((MainActivity) getActivity()).updateHeaderFragment(bookTitle);
    ((MainActivity) getActivity()).setVisibilityImportShareButton(View.INVISIBLE, View.VISIBLE);
    setupSortBtn();

    setFunctionsToolbar();

    selectedNoteItems = new ArrayList<NoteItem>();

    bookDao = bookNotesViewModel.getBookDao();
    noteDao = bookNotesViewModel.getNoteDao();

    fileName = (bookDao.findById(bookId).getTitle()
        + bookDao.findById(bookId).getPubYear())
        .replaceAll("\\s+", "");
    exportBibTex = new ExportBibTex(StorageKeys.DOWNLOAD_FOLDER, fileName);

    return view;
  }

  private void setupSortBtn() {
    ImageButton sortBtn = getActivity().findViewById(R.id.sort_btn);
    ((MainActivity) getActivity()).setVisibilitySortButton(true);

    sortBtn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        handleSortNote();
      }
    });
  }

  private void setFunctionsToolbar() {

    ((MainActivity) getActivity()).shareBtn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        checkEmptyNoteList();
      }
    });

  }

  @Override
  public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
    inflater.inflate(R.menu.fragment_book_note_menu, menu);
    super.onCreateOptionsMenu(menu, inflater);
  }


  @Override
  public boolean onOptionsItemSelected(MenuItem item) {

    switch (item.getItemId()) {
      case R.id.menu_delete_note:
        handleDeleteNote();
        break;

      case R.id.menu_export_note:
        checkEmptyNoteList();
        break;

      case R.id.menu_help_book_note:
        handleManualBookNotes();
        break;

      default:
        Toast.makeText(getContext(), "Fehler", Toast.LENGTH_SHORT).show();
    }

    return super.onOptionsItemSelected(item);
  }

  @Override
  public void onPrepareOptionsMenu(Menu menu) {
    MenuItem deleteNote = menu.findItem(R.id.menu_delete_note);
    deleteNote.setVisible(selectedNoteItems != null && !selectedNoteItems.isEmpty());
  }

  private void handleDeleteNote() {
    AlertDialog.Builder alertDeleteBookNote = new AlertDialog.Builder(context);
    alertDeleteBookNote.setCancelable(false);

    if (selectedNoteItems.size() > 1) {
      alertDeleteBookNote.setTitle(R.string.delete_notes);
      alertDeleteBookNote.setMessage(
          getString(R.string.delete_notes_message) + " " + getString(R.string.delete_warning));
    } else {
      alertDeleteBookNote.setTitle(R.string.delete_note);
      alertDeleteBookNote.setMessage(
          getString(R.string.delete_note_message) + " " + getString(R.string.delete_warning));
    }

    alertDeleteBookNote.setNegativeButton(R.string.back, new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
      }
    });

    alertDeleteBookNote.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        final int notesNumber = selectedNoteItems.size();

        bookNotesViewModel.deleteNotes(selectedNoteItems);
        adapter.notifyDataSetChanged();
        updateEmptyView(bookNotesViewModel.getCurrentNoteList());

        if (notesNumber > 1) {
          Toast.makeText(context, getString(R.string.deleted_notes), Toast.LENGTH_SHORT).show();
        } else {
          Toast.makeText(context, getString(R.string.deleted_note), Toast.LENGTH_SHORT).show();
        }

        unselectNoteItems();
      }
    });

    alertDeleteBookNote.show();
  }

  private void unselectNoteItems() {
    RecyclerView bookNotesListView = getView().findViewById(R.id.book_notes_recycler_view);
    for (int i = 0; i < bookNotesListView.getChildCount(); i++) {
      bookNotesListView.getChildAt(i).setSelected(false);
    }

    selectedNoteItems.clear();
  }

  private void handleSortNote() {
    SortDialog sortDialog = new SortDialog(context, sortCriteria,
        new SortDialog.SortDialogListener() {
          @Override
          public void onSortedSelected(SortCriteria newSortCriteria) {
            sortCriteria = newSortCriteria;
            ((MainActivity) getActivity()).setSortCriteria(newSortCriteria);
            sortNoteList();
          }
        });

    sortDialog.show();
  }

  private void sortNoteList() {
    List<NoteItem> noteList = bookNotesViewModel.getSortedNoteList(sortCriteria, bookId);
    adapter.setBookNoteList(noteList);
    adapter.notifyDataSetChanged();
  }

  private void checkEmptyNoteList() {
    if (bookNotesViewModel.getCurrentNoteList().isEmpty()) {
      AlertDialog.Builder alertDialogEmptyLib = new AlertDialog.Builder(getContext());
      alertDialogEmptyLib.setTitle(R.string.empty_note_list);
      alertDialogEmptyLib.setMessage(R.string.empty_note_list_description);

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
      // if the user has already allowed access to device external storage
      exportBibTex.createBibFile();
      exportBibTex.writeBibFile(exportBibTex.getBibDataFromBook(bookId, bookDao, noteDao));

      Toast.makeText(getContext(),
          getString(R.string.exported_file_stored_in) + '\n'
              + File.separator + StorageKeys.DOWNLOAD_FOLDER + File.separator
              + fileName + StorageKeys.BIB_FILE_TYPE, Toast.LENGTH_LONG).show();
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
   * @param requestCode  unique integer value for the requested permission
   *                     This value is given by the programmer.
   * @param permissions  array of requested name(s)
   *                     of the permission(s)
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
        exportBibTex.writeBibFile(exportBibTex.getBibDataFromBook(bookId, bookDao, noteDao));

        Toast.makeText(getContext(),
            getString(R.string.exported_file_stored_in) + '\n'
                + File.separator + StorageKeys.DOWNLOAD_FOLDER
                + File.separator + fileName
                + StorageKeys.BIB_FILE_TYPE, Toast.LENGTH_LONG).show();

      } else {
        Toast.makeText(getContext(), R.string.storage_permission_denied,
            Toast.LENGTH_SHORT).show();
      }

    }
  }

  private void handleManualBookNotes() {
    HelpFragment helpFragment = new HelpFragment();
    String htmlAsString = getString(R.string.book_note_help_text);

    Bundle bundle = new Bundle();

    bundle.putString(LibraryKeys.MANUAL_TEXT, htmlAsString);
    helpFragment.setArguments(bundle);

    getActivity().getSupportFragmentManager().beginTransaction()
        .replace(R.id.fragment_container_view, helpFragment,
            LibraryKeys.FRAGMENT_HELP_VIEW)
        .addToBackStack(null)
        .commit();
  }

  private void setupAddButton() {
    View addButtonView = view.findViewById(R.id.btn_add_note);
    PopupMenu pm = new PopupMenu(getContext(), addButtonView);
    pm.getMenuInflater().inflate(R.menu.add_note_menu, pm.getMenu());

    pm.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
      @Override
      public boolean onMenuItemClick(MenuItem item) {
        if (item.getItemId() == R.id.add_text_note) {
          Bundle bundle = new Bundle();
          bundle.putLong(LibraryKeys.BOOK_ID, bookId);
          TextNoteEditorFragment nextFrag = new TextNoteEditorFragment();
          nextFrag.setArguments(bundle);
          getActivity().getSupportFragmentManager().beginTransaction()
              .replace(R.id.fragment_container_view, nextFrag,
                  LibraryKeys.FRAGMENT_TEXT_NOTE_EDITOR)
              .addToBackStack(null)
              .commit();

        } /* else if (item.getItemId() == R.id.add_picture_note) {
                    TODO: add features to add pictures
                }
                else if (item.getItemId() == R.id.add_voice_note) {
                    TODO: add features to add voice notes
                }*/


        return true;
      }
    });


    addButtonView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        pm.show();
      }
    });

  }

  private void updateBookNoteList(List<NoteItem> noteList) {
    sortNoteList();

    updateEmptyView(noteList);
  }

  private Bundle createNoteBundle(NoteItem item) {
    Bundle bundle = new Bundle();
    Long currentNoteId = item.getId();
    bundle.putLong(LibraryKeys.BOOK_ID, bookId);
    bundle.putLong(LibraryKeys.NOTE_ID, currentNoteId);

    return bundle;
  }

  private void setupRecyclerView(Long bookId) {
    bookNotesViewModel = new BookNotesViewModel(getContext());
    noteList = bookNotesViewModel.getNoteList(bookId);

    RecyclerView bookNotesRecyclerViewAdapter =
        view.findViewById(R.id.book_notes_recycler_view);
    adapter = new BookNotesRecyclerViewAdapter(noteList, this, context);
    bookNotesRecyclerViewAdapter.setAdapter(adapter);

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

  @Override
  public void onItemClicked(int position) {
    NoteItem noteItem = bookNotesViewModel.getSelectedNoteItem(position);

    TextNoteEditorFragment nextFrag = new TextNoteEditorFragment();
    nextFrag.setArguments(createNoteBundle(noteItem));
    getActivity().getSupportFragmentManager().beginTransaction()
        .replace(R.id.fragment_container_view, nextFrag, LibraryKeys.FRAGMENT_TEXT_NOTE_EDITOR)
        .addToBackStack(null)
        .commit();
  }

  @Override
  public void onLongItemClicked(int position, NoteItem noteItem, View v) {
    if (v.isSelected()) {
      v.setSelected(false);
      selectedNoteItems.remove(noteItem);
    } else {
      v.setSelected(true);
      selectedNoteItems.add(noteItem);
    }
  }

}
