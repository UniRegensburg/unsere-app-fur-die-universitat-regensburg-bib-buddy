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

  private View view;
  private NoteModel noteModel;
  public List<NoteItem> noteList; // TODO why is this public only because of test? should be private
  private SwipeableRecyclerView notesRecyclerView;
  private NoteRecyclerViewAdapter adapter;
  private SortTypeLut sortTypeLut;

  private void setupMainActivity() {
    MainActivity mainActivity = (MainActivity) requireActivity();

    mainActivity.setVisibilityImportShareBtn(View.GONE, View.GONE);
    mainActivity.setVisibilitySortBtn(true);
    sortTypeLut = mainActivity.getSortTypeLut();

    mainActivity.updateHeaderFragment(getString(R.string.navigation_notes));
    mainActivity.updateNavigationFragment(R.id.navigation_notes);
  }


  private void handleDeleteNote(List<NoteItem> itemsToDelete) {
    AlertDialog.Builder alertDeleteBookNote =
        new AlertDialog.Builder(requireActivity());
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

  private void setupRecyclerView() {
    notesRecyclerView = view.findViewById(R.id.note_list_recycler_view);

    noteModel = new NoteModel(requireActivity());
    noteList = noteModel.getNoteList();

    adapter =
        new NoteRecyclerViewAdapter((MainActivity) requireActivity(), noteList, noteModel);
    notesRecyclerView.setAdapter(adapter);
    notesRecyclerView.setListener(this);

    updateEmptyListView(noteList);
  }

  private void updateEmptyListView(List<NoteItem> noteList) {
    TextView emptyListView = view.findViewById(R.id.empty_notes_list_view);
    if (noteList.isEmpty()) {
      emptyListView.setVisibility(View.VISIBLE);
    } else {
      emptyListView.setVisibility(View.GONE);
    }
  }

  private void setupSortBtn() {
    ImageButton sortBtn = requireActivity().findViewById(R.id.sort_btn);
    sortBtn.setOnClickListener(v -> sortNotes());
  }

  private void sortNotes() {
    MainActivity mainActivity = (MainActivity) requireActivity();
    SortDialog sortDialog = new SortDialog(mainActivity, sortTypeLut,
                                           newSortCriteria -> {
                                             sortTypeLut = newSortCriteria;
                                             mainActivity.setSortTypeLut(newSortCriteria);
                                             sortNoteList();
                                           });
    sortDialog.show();
  }

  private void sortNoteList() {
    noteList = noteModel.getAllSortedNoteList(sortTypeLut);
    adapter.setNoteList(noteList);
  }

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

    view = inflater.inflate(R.layout.fragment_notes, container, false);

    setupMainActivity();
    setupRecyclerView();
    setupSortBtn();

    setHasOptionsMenu(true);

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
