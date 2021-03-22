package de.bibbuddy;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ShareCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

/**
 * BookNotesView is responsible for the noteList of a certain book.
 *
 * @author Sarah Kurek, Silvia Ivanova, Luis Moßburger
 */
public class BookNotesView extends Fragment {

  private View view;
  private Context context;
  private BookNotesViewModel bookNotesViewModel;
  private RecyclerView recyclerView;
  private NoteRecyclerViewAdapter adapter;
  private Long bookId;
  private List<NoteItem> noteList;

  private BookDao bookDao;
  private NoteDao noteDao;

  private ExportBibTex exportBibTex;
  private SortCriteria sortCriteria;


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

    Bundle bundle = this.getArguments();
    bookId = bundle.getLong(LibraryKeys.BOOK_ID);

    view = inflater.inflate(R.layout.fragment_book_notes, container, false);
    context = view.getContext();
    recyclerView = view.findViewById(R.id.book_notes_recycler_view);

    sortCriteria = ((MainActivity) getActivity()).getSortCriteria();

    setupRecyclerView(bookId);

    setHasOptionsMenu(true);
    setupAddButton();

    updateBookNoteList(noteList);

    String bookTitle = bundle.getString(LibraryKeys.BOOK_TITLE);

    ((MainActivity) getActivity()).updateHeaderFragment(bookTitle);
    ((MainActivity) getActivity()).setVisibilityImportShareButton(View.GONE, View.VISIBLE);
    setupSortBtn();

    setFunctionsToolbar();

    bookDao = bookNotesViewModel.getBookDao();
    noteDao = bookNotesViewModel.getNoteDao();

    String fileName = (bookDao.findById(bookId).getTitle()
        + bookDao.findById(bookId).getPubYear())
        .replaceAll("\\s+", "");
    exportBibTex = new ExportBibTex(StorageKeys.DOWNLOAD_FOLDER, fileName);

    return view;
  }

  /**
   * Closes the BookNotesViewFragment.
   */
  public void closeFragment() {
    FragmentManager fragmentManager = getParentFragmentManager();
    if (fragmentManager.getBackStackEntryCount() > 0) {
      fragmentManager.popBackStack();
    } else {
      requireActivity().onBackPressed();
    }
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
    int itemId = item.getItemId();
    if (itemId == R.id.menu_delete_note) {
      handleDeleteNote();
    } else if (itemId == R.id.menu_export_note) {
      checkEmptyNoteList();
    } else if (itemId == R.id.menu_help_book_note) {
      handleManualBookNotes();
    }

    return super.onOptionsItemSelected(item);
  }

  @Override
  public void onPrepareOptionsMenu(Menu menu) {
    MenuItem deleteNote = menu.findItem(R.id.menu_delete_note);
    deleteNote.setVisible(adapter.getSelectedNoteItems().size() > 0);
  }

  private void handleDeleteNote() {
    AlertDialog.Builder alertDeleteBookNote = new AlertDialog.Builder(context);
    alertDeleteBookNote.setCancelable(false);

    if (adapter.getSelectedNoteItems().size() > 1) {
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
        deselectNoteItems();
      }
    });

    alertDeleteBookNote.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        final int notesNumber = adapter.getSelectedNoteItems().size();
        bookNotesViewModel.deleteNotes(adapter.getSelectedNoteItems());
        adapter.notifyDataSetChanged();
        noteList = bookNotesViewModel.getBookNoteList(bookId);
        updateBookNoteList(noteList);
        updateEmptyView(noteList);
        if (notesNumber > 1) {
          Toast.makeText(context, getString(R.string.deleted_notes), Toast.LENGTH_SHORT).show();
        } else {
          Toast.makeText(context, getString(R.string.deleted_note), Toast.LENGTH_SHORT).show();
        }
      }
    });

    alertDeleteBookNote.show();
  }

  private void deselectNoteItems() {
    RecyclerView bookNotesListView = getView().findViewById(R.id.book_notes_recycler_view);
    for (int i = 0; i < bookNotesListView.getChildCount(); i++) {
      bookNotesListView.getChildAt(i).setSelected(false);
    }
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
    adapter.setNoteList(noteList);
  }

  private void checkEmptyNoteList() {
    if (bookNotesViewModel.getBookNoteList(bookId).isEmpty()) {
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
      shareBookNoteBibIntent();
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
    View addButtonView = view.findViewById(R.id.add_btn);
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

        } /* else if (item.getItemId() == R.id.add_voice_note) {
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
    noteList = bookNotesViewModel.getBookNoteList(bookId);

    RecyclerView notesRecyclerView =
        view.findViewById(R.id.book_notes_recycler_view);
    adapter = new NoteRecyclerViewAdapter((MainActivity) requireActivity(), noteList);
    notesRecyclerView.setAdapter(adapter);

    updateEmptyView(noteList);
  }

  private void updateEmptyView(List<NoteItem> noteList) {
    TextView emptyView = view.findViewById(R.id.empty_note_list_view);

    if (noteList.isEmpty()) {
      emptyView.setVisibility(View.VISIBLE);
    } else {
      emptyView.setVisibility(View.GONE);
    }
  }

  private void shareBookNoteBibIntent() {

    Uri contentUri = exportBibTex.writeTemporaryBibFile(context,
        exportBibTex.getBibDataFromBook(bookId, bookDao, noteDao));

    Intent shareBookNoteIntent =
        ShareCompat.IntentBuilder.from(getActivity())
            .setStream(contentUri)
            .setType("text/*")
            .getIntent()
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

    startActivity(Intent.createChooser(shareBookNoteIntent, "SEND"));

  }

}
