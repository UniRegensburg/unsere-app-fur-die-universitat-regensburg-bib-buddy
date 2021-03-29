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
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import com.tsuryo.swipeablerv.SwipeLeftRightCallback;
import com.tsuryo.swipeablerv.SwipeableRecyclerView;
import java.util.Collections;
import java.util.List;

/**
 * The NotesFragment is responsible for the Notes of a Book.
 *
 * @author Sabrina Freisleben
 */
public class NotesFragment extends Fragment implements SwipeLeftRightCallback.Listener {

  public static List<NoteItem> noteList;
  private static NoteModel noteModel;
  private SwipeableRecyclerView notesRecyclerView;
  private NoteRecyclerViewAdapter adapter;
  private SortCriteria sortCriteria;
  private TextView emptyListView;

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
    requireActivity().getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
      @Override
      public void handleOnBackPressed() {
        if (adapter.getSelectedNoteItems().size() > 0) {
          deselectNoteItems();
        } else {
          closeFragment();
        }
      }
    });

    View view = inflater.inflate(R.layout.fragment_notes, container, false);

    noteModel = new NoteModel(getContext());
    notesRecyclerView = view.findViewById(R.id.note_list_recycler_view);
    noteList = noteModel.getNoteList();
    sortCriteria = ((MainActivity) requireActivity()).getSortCriteria();
    emptyListView = view.findViewById(R.id.empty_notes_list_view);

    ((MainActivity) requireActivity()).updateNavigationFragment(R.id.navigation_notes);

    ((MainActivity) requireActivity())
        .setVisibilityImportShareButton(View.GONE, View.GONE);
    setupSortBtn();
    setHasOptionsMenu(true);
    setupRecyclerView(view);

    return view;
  }


  /**
   * Closes the NotesFragment.
   */
  private void closeFragment() {
    FragmentManager fragmentManager = getParentFragmentManager();
    if (fragmentManager.getBackStackEntryCount() > 0) {
      fragmentManager.popBackStack();
    } else {
      requireActivity().onBackPressed();
    }
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
      handleDeleteNote(adapter.getSelectedNoteItems());
    } else if (id == R.id.menu_note_list_help) {
      handleHelpNotesFragment();
    } else if (id == R.id.menu_imprint) {
      ((MainActivity) getActivity()).openImprint();
    }

    return super.onOptionsItemSelected(item);
  }

  private void handleDeleteNote(List<NoteItem> itemsToDelete) {
    AlertDialog.Builder alertDeleteBookNote = new AlertDialog.Builder(requireActivity());
    alertDeleteBookNote.setCancelable(false);
    alertDeleteBookNote.setTitle(R.string.delete_notes);

    if (adapter.getSelectedNoteItems().size() > 1) {
      alertDeleteBookNote.setMessage(
          getString(R.string.delete_notes_message)
              + convertNoteListToString(itemsToDelete)
              + getString(R.string.finally_delete) + " "
              + getString(R.string.delete_warning));
    } else {
      alertDeleteBookNote.setMessage(
          getString(R.string.delete_note_message)
              + convertNoteListToString(itemsToDelete)
              + getString(R.string.finally_delete) + " "
              + getString(R.string.delete_warning));
    }

    alertDeleteBookNote.setNegativeButton(R.string.cancel, (dialog, which) -> deselectNoteItems());
    alertDeleteBookNote
        .setPositiveButton(R.string.delete, (dialog, which) -> performDelete(itemsToDelete));

    alertDeleteBookNote.show();
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

      ++counter;
    }
    return notes.toString();
  }

  private void deselectNoteItems() {
    for (int i = 0; i < notesRecyclerView.getChildCount(); i++) {
      notesRecyclerView.getChildAt(i).setSelected(false);
    }
  }

  private void performDelete(List<NoteItem> itemsToDelete) {
    itemsToDelete.forEach(n -> noteModel.deleteNote(n.getId()));

    noteList = noteModel.getNoteList();
    adapter.setNoteList(noteList);

    if (itemsToDelete.size() > 0) {
      Toast.makeText(requireContext(), getString(R.string.deleted_notes), Toast.LENGTH_SHORT)
          .show();
    } else {
      Toast.makeText(requireContext(), getString(R.string.deleted_note), Toast.LENGTH_SHORT).show();
    }

    deselectNoteItems();
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
