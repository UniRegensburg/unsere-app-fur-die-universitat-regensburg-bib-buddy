package de.bibbuddy;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
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

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_notes, container, false);
    recyclerView = view.findViewById(R.id.recyclerView);
    NoteModel noteModel = new NoteModel(getContext());
    notes = noteModel.getCompleteNoteList();
    setupRecyclerView();
    enableSwipeToDelete();
    return view;
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

}
