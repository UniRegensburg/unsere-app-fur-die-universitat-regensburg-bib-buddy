package de.bibbuddy;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

public class NotesFragment extends Fragment {

  static List<NoteItem> notes;
  private View view;
  private NoteRecyclerViewAdapter adapter;
  private RecyclerView recyclerView;

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
    view = inflater.inflate(R.layout.fragment_notes, container, false);
    recyclerView = view.findViewById(R.id.recyclerView);
    NoteModel noteModel = new NoteModel(getContext());
    notes = noteModel.getNoteList();
    setupRecyclerView();
    enableSwipeToDelete();
    setupAddButton();
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

  /*
     TODO: Take this add button to single book view / book fragment
  */
  private void setupAddButton() {
    View addButtonView = view.findViewById(R.id.addButton);
    PopupMenu pm = new PopupMenu(getContext(), addButtonView);
    pm.getMenuInflater().inflate(R.menu.add_note_menu, pm.getMenu());

    pm.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
      @Override
      public boolean onMenuItemClick(MenuItem item) {
        if (item.getItemId() == R.id.add_text_note) {
          TextNoteEditorFragment nextFrag = new TextNoteEditorFragment();
          getActivity().getSupportFragmentManager().beginTransaction()
              .replace(R.id.fragment_container_view, nextFrag, "fragment_text_note_editor")
              .addToBackStack(null)
              .commit();
        }
        //TODO: add features to add voice notes and pictures
        //case R.id.add_voice_note:
        //case R.id.add_picture:
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


}
