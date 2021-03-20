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
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

/**
 * The NotesFragment is responsible for the Notes of a Book.
 *
 * @author Sabrina Freisleben
 */
public class NotesFragment extends Fragment {

  public static List<NoteItem> noteList;
  private static NoteModel noteModel;
  private RecyclerView notesRecyclerView;
  private NoteRecyclerViewAdapter adapter;
  private SortCriteria sortCriteria;
  private TextView emptyListView;

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_notes, container, false);

    noteModel = new NoteModel(getContext());
    notesRecyclerView = view.findViewById(R.id.note_list_recycler_view);
    noteList = noteModel.getNoteList();
    sortCriteria = ((MainActivity) requireActivity()).getSortCriteria();
    emptyListView = view.findViewById(R.id.empty_notes_list_view);

    ((MainActivity) requireActivity())
        .setVisibilityImportShareButton(View.INVISIBLE, View.INVISIBLE);
    setupSortBtn();
    setHasOptionsMenu(true);
    setupRecyclerView();

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
    deleteNote.setVisible(adapter.getSelectedNoteItems().size() > 0);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    long id = item.getItemId();
    if (id == R.id.menu_note_list_delete) {
      handleDeleteNote();
    } else if (item.getItemId() == R.id.menu_note_list_help) {
      handleHelpNotesFragment();
    } else {
      Toast.makeText(getContext(), R.string.error, Toast.LENGTH_SHORT).show();
    }

    return super.onOptionsItemSelected(item);
  }

  private void handleDeleteNote() {
    AlertDialog.Builder alertDeleteBookNote = new AlertDialog.Builder(requireActivity());

    alertDeleteBookNote.setCancelable(false);
    alertDeleteBookNote.setTitle(R.string.delete_note);
    alertDeleteBookNote.setMessage(R.string.delete_note_message);
    alertDeleteBookNote.setNegativeButton(R.string.back, (dialog, which) -> deselectNoteItems());
    alertDeleteBookNote.setPositiveButton(R.string.delete, (dialog, which) -> performDelete());

    alertDeleteBookNote.show();
  }

  private void deselectNoteItems() {
    for (int i = 0; i < notesRecyclerView.getChildCount(); i++) {
      notesRecyclerView.getChildAt(i).setSelected(false);
    }
  }

  private void performDelete() {
    if (adapter.getSelectedNoteItems().size() > 1) {
      Toast.makeText(requireContext(), getString(R.string.deleted_notes), Toast.LENGTH_SHORT)
          .show();
    } else {
      Toast.makeText(requireContext(), getString(R.string.deleted_note), Toast.LENGTH_SHORT).show();
    }
    for (int i = 0; i < notesRecyclerView.getChildCount(); i++) {
      if (notesRecyclerView.getChildAt(i).isSelected()) {
        noteModel.deleteNote(noteList.get(i).getId());
      }
    }
    noteList = noteModel.getNoteList();
    adapter.setNoteList(noteList);
    updateEmptyListView(noteList);
  }

  private void handleHelpNotesFragment() {
    HelpFragment helpFragment = new HelpFragment();
    String htmlAsString = getString(R.string.note_list_help_text);

    Bundle bundle = new Bundle();

    bundle.putString(LibraryKeys.MANUAL_TEXT, htmlAsString);
    helpFragment.setArguments(bundle);

    requireActivity().getSupportFragmentManager().beginTransaction()
        .replace(R.id.fragment_container_view, helpFragment,
            LibraryKeys.FRAGMENT_HELP_VIEW)
        .addToBackStack(null)
        .commit();
  }

  private void setupRecyclerView() {
    adapter =
        new NoteRecyclerViewAdapter((MainActivity) requireActivity(), noteList);
    notesRecyclerView.setAdapter(adapter);
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
    ImageButton sortBtn = requireActivity().findViewById(R.id.sort_btn);
    ((MainActivity) requireActivity()).setVisibilitySortButton(true);
    sortBtn.setOnClickListener(v -> sortNotes());
  }

  private void sortNotes() {
    SortDialog sortDialog = new SortDialog(getContext(), sortCriteria,
        newSortCriteria -> {
          sortCriteria = newSortCriteria;
          ((MainActivity) requireActivity()).setSortCriteria(newSortCriteria);
          sortNoteList();
        });

    sortDialog.show();
  }

  private void sortNoteList() {
    noteList = noteModel.getAllSortedNoteList(sortCriteria);
    adapter.setNoteList(noteList);
  }

}
