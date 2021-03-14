package de.bibbuddy;

import android.os.Bundle;
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
  private NoteRecyclerViewAdapter adapter;
  private RecyclerView recyclerView;
  private static NoteModel noteModel;

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

    View view = inflater.inflate(R.layout.fragment_notes, container, false);
    recyclerView = view.findViewById(R.id.recyclerView);

    sortCriteria = SortCriteria.MOD_DATE_LATEST;

    noteModel = new NoteModel(getContext());
    notes = noteModel.getCompleteNoteList();
    setHasOptionsMenu(true);
    setupRecyclerView();
    enableSwipeToDelete();

    ((MainActivity) getActivity()).setVisibilityImportShareButton(View.INVISIBLE, View.INVISIBLE);

    return view;
  }

  @Override
  public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
    inflater.inflate(R.menu.fragment_note_list_menu, menu);
    super.onCreateOptionsMenu(menu, inflater);
  }


  @Override
  public boolean onOptionsItemSelected(MenuItem item) {

    switch (item.getItemId()) {
      case R.id.menu_note_sort:
        handleSortNote();
        break;

      case R.id.menu_help_note_list:
        handleManualNotesList();
        break;

      default:
        Toast.makeText(getContext(), "Fehler", Toast.LENGTH_SHORT).show();
    }

    return super.onOptionsItemSelected(item);
  }

  private void handleSortNote() {
    SortDialog sortDialog = new SortDialog(getContext(), sortCriteria,
        new SortDialog.SortDialogListener() {
          @Override
          public void onSortedSelected(SortCriteria newSortCriteria) {
            sortCriteria = newSortCriteria;
            sortNoteList();
          }
        });

    sortDialog.show();
  }

  private void sortNoteList() {
    List<NoteItem> noteList = noteModel.getAllSortedNoteList(sortCriteria);
    adapter.setNoteList(noteList);
    adapter.notifyDataSetChanged();
  }

  private void handleManualNotesList() {
    HelpFragment helpFragment = new HelpFragment();
    String htmlAsString = getString(R.string.note_list_help_text);

    Bundle bundle = new Bundle();

    bundle.putString(LibraryKeys.MANUAL_TEXT, htmlAsString);
    helpFragment.setArguments(bundle);

    getActivity().getSupportFragmentManager().beginTransaction()
        .replace(R.id.fragment_container_view, helpFragment,
            LibraryKeys.FRAGMENT_HELP_VIEW)
        .addToBackStack(null)
        .commit();
  }

  private void setupRecyclerView() {
    adapter = new NoteRecyclerViewAdapter(notes, (MainActivity) getActivity());
    recyclerView.setAdapter(adapter);
  }

  private void enableSwipeToDelete() {
    SwipeToDeleteCallback swipeToDeleteCallback =
        new SwipeToDeleteCallback(getContext(), adapter, (MainActivity) getActivity());
    ItemTouchHelper itemTouchhelper = new ItemTouchHelper(swipeToDeleteCallback);
    itemTouchhelper.attachToRecyclerView(recyclerView);
  }

  public static void deleteNote(Long id) {
    noteModel.deleteNote(id);
  }

}
