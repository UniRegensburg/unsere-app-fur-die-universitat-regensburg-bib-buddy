package de.bibbuddy;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

public class NotesFragment extends Fragment {

    static ArrayList<NoteItem> notes = new ArrayList<>();
    static View view;
    RecyclerViewAdapter adapter;
    private RecyclerView recyclerView;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_notes, container, false);
        recyclerView = view.findViewById(R.id.recyclerView);

        notes = TextNoteEditorFragment.getNotes();

        setupRecyclerView();
        enableSwipeToDelete();
        setupAddButton();

        return view;
    }

    /*
        Set up default RecyclerViewAdapter to manage recyclerview containing the notes arraylist
     */
    private void setupRecyclerView() {
        adapter = new RecyclerViewAdapter(notes, (MainActivity) getActivity());
        recyclerView.setAdapter(adapter);
    }

    /*
        Enable swipe left to delete a recyclerView item
         by using ItemTouchHelper given a default SwipeToDeleteCallback-class
     */
    private void enableSwipeToDelete() {
        SwipeToDeleteCallback swipeToDeleteCallback = new SwipeToDeleteCallback(getContext(), adapter, recyclerView);
        ItemTouchHelper itemTouchhelper = new ItemTouchHelper(swipeToDeleteCallback);
        itemTouchhelper.attachToRecyclerView(recyclerView);
    }

    /*
        To-do: Take this add button to single book view / book fragment
    */
    private void setupAddButton() {
        View addButtonView = view.findViewById(R.id.addButton);
        PopupMenu pm = new PopupMenu(getContext(), addButtonView);
        pm.getMenuInflater().inflate(R.menu.add_note_menu, pm.getMenu());

        pm.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.add_text_note: {
                        TextNoteEditorFragment nextFrag = new TextNoteEditorFragment();
                        getActivity().getSupportFragmentManager().beginTransaction()
                                .replace(R.id.fragment_container_view, nextFrag, "fragment_text_note_editor")
                                .addToBackStack(null)
                                .commit();
                        break;
                    }

                    /*
                        To-do: add features to add voice notes and pictures
                     */
                    //case R.id.add_voice_note:
                    //     break;

                    // case R.id.add_picture:
                    //      break;

                    default:
                        break;
                }
                return true;
            }
        });

        /*popup menu for choosing note type to add
            -> maybe there is a more suitable UI form?
         */
        addButtonView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pm.show();
            }
        });

    }

}