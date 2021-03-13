package de.bibbuddy;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

/**
 * The NotesFragment is responsible for the Notes of a Book.
 *
 * @author Sabrina Freisleben
 */
public class NotesFragment extends Fragment {

  static List<NoteItem> notes;
  private static NoteModel noteModel;
  private static RecyclerView recyclerView;
  private NotesRecyclerViewAdapter adapter;

  public static void deleteNote(Long id) {
    noteModel.deleteNote(id);
  }

  public static byte[] getNoteMedia(Long noteId) {
    Note note = noteModel.getNoteById(noteId);
    return noteModel.getNoteMedia(note.getNoteFileId());
  }

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
    View view = inflater.inflate(R.layout.fragment_notes, container, false);

    recyclerView = view.findViewById(R.id.notesRecyclerView);
    noteModel = new NoteModel(getContext());
    notes = noteModel.getCompleteNoteList();

    setHasOptionsMenu(true);
    setupRecyclerView();
    enableSwipeToDelete();
    return view;
  }

  @Override
  public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
    inflater.inflate(R.menu.fragment_note_list_menu, menu);
    super.onCreateOptionsMenu(menu, inflater);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == R.id.menu_delete_note_list) {
      adapter.handleDeleteNote();
    } else if (item.getItemId() == R.id.menu_help_note_list) {
      handleManualNotesList();
    } else {
      Toast.makeText(getContext(), R.string.error, Toast.LENGTH_SHORT).show();
    }

    return super.onOptionsItemSelected(item);
  }

  private void handleManualNotesList() {
    Spanned htmlAsString = Html.fromHtml(getString(R.string.note_list_help_text), Html.FROM_HTML_MODE_COMPACT);

    android.app.AlertDialog.Builder alertDeleteNote = new AlertDialog.Builder(requireActivity());
    alertDeleteNote.setCancelable(false);
    alertDeleteNote.setTitle(R.string.help);
    alertDeleteNote.setMessage(htmlAsString);
    alertDeleteNote.setPositiveButton(R.string.ok, (dialog, which) -> {});
    alertDeleteNote.show();
  }

  private void setupRecyclerView() {
    adapter = new NotesRecyclerViewAdapter(notes, (MainActivity) requireActivity(), noteModel);
    recyclerView.setAdapter(adapter);
  }

  private void enableSwipeToDelete() {
    SwipeToDeleteCallback swipeToDeleteCallback =
        new SwipeToDeleteCallback(getContext(), adapter, (MainActivity) requireActivity());
    ItemTouchHelper itemTouchhelper = new ItemTouchHelper(swipeToDeleteCallback);
    itemTouchhelper.attachToRecyclerView(recyclerView);
  }

}
