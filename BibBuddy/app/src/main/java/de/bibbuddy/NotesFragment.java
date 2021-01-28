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
import androidx.annotation.VisibleForTesting;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

public class NotesFragment extends Fragment {

    @VisibleForTesting
    static List<Note> notes;
    View view;
    RecyclerViewAdapter adapter;
    RecyclerView recyclerView;
    NoteDAO noteDAO;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_notes, container, false);
        recyclerView = view.findViewById(R.id.recyclerView);
        DatabaseHelper databaseHelper = new DatabaseHelper(getContext());
        noteDAO = new NoteDAO(databaseHelper);
        notes = noteDAO.findAll();

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
        /*
        recyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {

            @Override
            public void onDraw(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                super.onDraw(c, parent, state);
                SwipeToDeleteCallback.onDraw(c);
            }
        });
    */
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

                    /*
                        TODO: add features to add voice notes and pictures

                    case R.id.add_voice_note:
                        break;

                    case R.id.add_picture:
                        break;
                     */

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
