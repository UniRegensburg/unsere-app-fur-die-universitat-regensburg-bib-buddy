package de.bibbuddy;

import android.app.AlertDialog;
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
import com.tsuryo.swipeablerv.SwipeLeftRightCallback;
import com.tsuryo.swipeablerv.SwipeableRecyclerView;
import java.util.Collections;
import java.util.List;

/**
 * NotesFragment is responsible for the Notes of a Book.
 *
 * @author Sabrina Freisleben
 */
public class NotesFragment extends BackStackFragment implements SwipeLeftRightCallback.Listener {

  public List<NoteItem> noteList;
  public NoteModel noteModel;

  private SwipeableRecyclerView notesRecyclerView;
  private NoteRecyclerViewAdapter adapter;
  private SortCriteria sortCriteria;
  private TextView emptyListView;

  @Override
  protected void onBackPressed() {
    if (adapter.getSelectedNoteItems().isEmpty()) {
      closeFragment();
    } else {
      deselectNoteItems();
    }
  }

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
    enableBackPressedHandler();

    MainActivity mainActivity = (MainActivity) requireActivity();
    noteModel = new NoteModel(mainActivity);
    noteList = noteModel.getNoteList();
    sortCriteria = mainActivity.getSortCriteria();

    View view = inflater.inflate(R.layout.fragment_notes, container, false);
    notesRecyclerView = view.findViewById(R.id.note_list_recycler_view);
    emptyListView = view.findViewById(R.id.empty_notes_list_view);

    mainActivity.updateNavigationFragment(R.id.navigation_notes);
    mainActivity.setVisibilityImportShareButton(View.GONE, View.GONE);

    setupSortBtn();
    setHasOptionsMenu(true);
    setupRecyclerView(view);

    return view;
  }

  @Override
  public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
    inflater.inflate(R.menu.fragment_note_list_menu, menu);
    super.onCreateOptionsMenu(menu, inflater);
  }

  @Override
  public void onPrepareOptionsMenu(Menu menu) {
    MenuItem deleteNote = menu.findItem(R.id.menu_note_list_delete);
    deleteNote.setVisible(!adapter.getSelectedNoteItems().isEmpty());
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    long id = item.getItemId();

    if (id == R.id.menu_note_list_delete) {
      handleDeleteNote(adapter.getSelectedNoteItems());
    } else if (id == R.id.menu_note_list_help) {
      handleHelpNotesFragment();
    } else if (id == R.id.menu_imprint) {
      MainActivity mainActivity = (MainActivity) requireActivity();
      mainActivity.openImprint();
    }

    return super.onOptionsItemSelected(item);
  }

  private void handleDeleteNote(List<NoteItem> itemsToDelete) {
    AlertDialog.Builder alertDeleteBookNote =
        new AlertDialog.Builder((MainActivity) requireActivity());
    alertDeleteBookNote.setCancelable(false);

    if (adapter.getSelectedNoteItems().size() > 1) {
      alertDeleteBookNote.setTitle(R.string.delete_notes);
      alertDeleteBookNote.setMessage(
          getString(R.string.delete_notes_message) + assembleAlertString(itemsToDelete));
    } else {
      alertDeleteBookNote.setTitle(R.string.delete_note);
      alertDeleteBookNote.setMessage(
          getString(R.string.delete_note_message) + assembleAlertString(itemsToDelete));
    }

    alertDeleteBookNote.setNegativeButton(R.string.cancel, (dialog, which) -> deselectNoteItems());
    alertDeleteBookNote
        .setPositiveButton(R.string.delete, (dialog, which) -> performDelete(itemsToDelete));

    alertDeleteBookNote.show();
  }

  private String assembleAlertString(List<NoteItem> itemsToDelete) {
    return convertNoteListToString(itemsToDelete)
        + getString(R.string.finally_delete) + " "
        + getString(R.string.delete_warning);
  }

  private String convertNoteListToString(List<NoteItem> noteList) {
    StringBuilder notes = new StringBuilder();

    int counter = 1;
    for (NoteItem note : noteList) {
      notes.append(" \"").append(note.getName()).append("\"");

      if (counter != noteList.size()) {
        notes.append(",");
      }

      notes.append(" ");

      counter++;
    }

    return notes.toString();
  }

  private void deselectNoteItems() {
    for (int i = 0; i < notesRecyclerView.getChildCount(); i++) {
      notesRecyclerView.getChildAt(i).setSelected(false);
    }
  }

  private void performDelete(List<NoteItem> itemsToDelete) {
    itemsToDelete.forEach(note -> noteModel.deleteNote(note.getId()));

    noteList = noteModel.getNoteList();
    adapter.setNoteList(noteList);

    MainActivity mainActivity = (MainActivity) requireActivity();
    if (!itemsToDelete.isEmpty()) {
      Toast.makeText(mainActivity, getString(R.string.deleted_notes), Toast.LENGTH_SHORT).show();
    } else {
      Toast.makeText(mainActivity, getString(R.string.deleted_note), Toast.LENGTH_SHORT).show();
    }

    deselectNoteItems();
    updateEmptyListView(noteList);
  }

  private void handleHelpNotesFragment() {
    String htmlAsString = getString(R.string.note_list_help_text);

    Bundle bundle = new Bundle();
    bundle.putString(LibraryKeys.MANUAL_TEXT, htmlAsString);

    HelpFragment helpFragment = new HelpFragment();
    helpFragment.setArguments(bundle);

    helpFragment
        .show(requireActivity().getSupportFragmentManager(), LibraryKeys.FRAGMENT_HELP_VIEW);
  }

  private void setupRecyclerView(View view) {
    notesRecyclerView = view.findViewById(R.id.note_list_recycler_view);

    adapter =
        new NoteRecyclerViewAdapter((MainActivity) requireActivity(), noteList, noteModel);
    notesRecyclerView.setAdapter(adapter);
    notesRecyclerView.setListener(this);

    updateEmptyListView(noteList);
  }

  private void updateEmptyListView(List<NoteItem> noteList) {
    if (noteList.isEmpty()) {
      emptyListView.setVisibility(View.VISIBLE);
    } else {
      emptyListView.setVisibility(View.GONE);
    }
  }

  private void setupSortBtn() {
    MainActivity mainActivity = (MainActivity) requireActivity();
    ImageButton sortBtn = mainActivity.findViewById(R.id.sort_btn);
    mainActivity.setVisibilitySortButton(true);
    sortBtn.setOnClickListener(v -> sortNotes());
  }

  private void sortNotes() {
    MainActivity mainActivity = (MainActivity) requireActivity();
    SortDialog sortDialog = new SortDialog(mainActivity, sortCriteria,
                                           newSortCriteria -> {
                                             sortCriteria = newSortCriteria;
                                             mainActivity.setSortCriteria(newSortCriteria);
                                             sortNoteList();
                                           });

    sortDialog.show();
  }

  private void sortNoteList() {
    noteList = noteModel.getAllSortedNoteList(sortCriteria);
    adapter.setNoteList(noteList);
  }

  @Override
  public void onSwipedLeft(int position) {
    deselectNoteItems();
    handleDeleteNote(Collections.singletonList(noteList.get(position)));
    adapter.notifyDataSetChanged();
  }

  @Override
  public void onSwipedRight(int position) {
  }

}
