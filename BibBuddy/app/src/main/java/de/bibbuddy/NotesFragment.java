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

  static List<NoteItem> noteList;
  private static NoteModel noteModel;
  private NoteRecyclerViewAdapter adapter;
  private RecyclerView recyclerView;
  private SortCriteria sortCriteria;

  public static void deleteNote(Long id) {
    noteModel.deleteNote(id);
  }

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {

    View view = inflater.inflate(R.layout.fragment_notes, container, false);
    recyclerView = view.findViewById(R.id.note_list_recycler_view);
    sortCriteria = ((MainActivity) requireActivity()).getSortCriteria();
    noteModel = new NoteModel(getContext());
    noteList = noteModel.getCompleteNoteList();

    setHasOptionsMenu(true);
    setupRecyclerView();
    ((MainActivity) requireActivity())
        .setVisibilityImportShareButton(View.INVISIBLE, View.INVISIBLE);
    setupSortBtn();

    return view;
  }

  @Override
  public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
    inflater.inflate(R.menu.fragment_note_list_menu, menu);
    super.onCreateOptionsMenu(menu, inflater);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    long id = item.getItemId();
    if (id == R.id.menu_delete_note) {
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

  private void deselectNoteItems() {
    RecyclerView bookNotesListView = getView().findViewById(R.id.note_list_recycler_view);
    for (int i = 0; i < bookNotesListView.getChildCount(); i++) {
      bookNotesListView.getChildAt(i).setSelected(false);
    }
  }

  private void performDelete() {
    for (int i = 0; i < recyclerView.getChildCount(); i++) {
      if (recyclerView.getChildAt(i).isSelected()) {
        noteModel.deleteNote(noteList.get(i).getId());
      }
    }
    noteList = noteModel.getCompleteNoteList();
    adapter.notifyDataSetChanged();
    updateEmptyView(noteList);
    Toast.makeText(requireContext(), getString(R.string.deleted_notes), Toast.LENGTH_SHORT).show();
  }

  private void setupRecyclerView() {
    adapter = new NoteRecyclerViewAdapter(noteList, (MainActivity) requireActivity());
    recyclerView.setAdapter(adapter);
    updateEmptyView(noteList);
  }

  private void updateEmptyView(List<NoteItem> noteList) {
    TextView emptyView = requireView().findViewById(R.id.empty_notes_list_view);
    if (noteList.isEmpty()) {
      emptyView.setVisibility(View.VISIBLE);
    } else {
      emptyView.setVisibility(View.GONE);
    }
  }

  private void setupSortBtn() {
    ImageButton sortBtn = requireActivity().findViewById(R.id.sort_btn);
    ((MainActivity) requireActivity()).setVisibilitySortButton(true);
    sortBtn.setOnClickListener(v -> handleSortNote());
  }

  private void handleSortNote() {
    SortDialog sortDialog = new SortDialog(getContext(), sortCriteria,
        newSortCriteria -> {
          sortCriteria = newSortCriteria;
          ((MainActivity) requireActivity()).setSortCriteria(newSortCriteria);
          sortNoteList();
        });

    sortDialog.show();
  }

  private void sortNoteList() {
    List<NoteItem> noteList = noteModel.getAllSortedNoteList(sortCriteria);
    adapter.setNoteList(noteList);
    adapter.notifyDataSetChanged();
  }

}
