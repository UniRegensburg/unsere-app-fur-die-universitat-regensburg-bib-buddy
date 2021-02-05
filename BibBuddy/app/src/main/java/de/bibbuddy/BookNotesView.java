package de.bibbuddy;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

public class BookNotesView extends Fragment implements BookNotesRecyclerViewAdapter.BookNotesViewListener {

    static List<NoteItem> noteList;
    private View view;
    private RecyclerView recyclerView;
    private BookNotesViewModel model;
    private Long bookId;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_book_notes_view, container, false);
        recyclerView = view.findViewById(R.id.bookNotesViewRecyclerView);

        model = new BookNotesViewModel(getContext());
        Bundle bundle = this.getArguments();

        bookId = bundle.getLong(LibraryKeys.BOOK_ID);
        String bookTitle = bundle.getString(LibraryKeys.BOOK_TITLE);

        noteList = model.getNoteList(bookId);

        TextView bookTitleView = view.findViewById(R.id.text_view_book);
        bookTitleView.setText(bookTitle);

        setupRecyclerView();
        setupAddButton();
        setupBackButton();

        return view;
    }


    private void setupRecyclerView() {
        recyclerView.setAdapter(new BookNotesRecyclerViewAdapter(noteList, this));
    }

    private void setupAddButton() {
        View addButtonView = view.findViewById(R.id.btn_add_note);
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


                    // TODO: If back-button is pressed in TextEditor, save note (first line = NoteName),
                    // model.createNote("name", 1, "text", bookId);
                    // and updateNoteListView(model.getNoteList(bookId);

                } /* else if (item.getItemId() == R.id.add_picture_note) {
                    TODO: add features to add pictures
                }
                else if (item.getItemId() == R.id.add_voice_note) {
                    TODO: add features to add voice notes
                }*/


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

    private void setupBackButton() {
        View backButton = view.findViewById(R.id.btn_back);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LibraryFragment nextFrag = new LibraryFragment();
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container_view, nextFrag, "fragment_library")
                        .addToBackStack(null)
                        .commit();
            }
        });
    }

    private Bundle createNoteBundle(NoteItem item) {
        Bundle bundle = new Bundle();

        Long currentNoteId = item.getId();
        String currentNoteName = item.getName();
        String currentNoteText = model.getNoteText(item.getId());

        bundle.putLong(LibraryKeys.NOTE_ID, currentNoteId);
        bundle.putString(LibraryKeys.NOTE_NAME, currentNoteName);
        bundle.putString(LibraryKeys.NOTE_TEXT, currentNoteText);

        return bundle;
    }

    private void updateNoteListView(List noteList) {
        recyclerView.setAdapter(new BookNotesRecyclerViewAdapter(noteList, this));
        TextView emptyView = getActivity().findViewById(R.id.empty_notelist_view);

        if (noteList.isEmpty()) {
            emptyView.setVisibility(View.VISIBLE);
        } else {
            emptyView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onItemClicked(int position) {
        NoteItem noteItem = model.getSelectedNoteItem(position);


        TextNoteEditorFragment nextFrag = new TextNoteEditorFragment();
        createNoteBundle(noteItem);
        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container_view, nextFrag, "fragment_text_note_editor")
                .addToBackStack(null)
                .commit();

        createNoteBundle(noteItem);
        // TODO: Retrieve data at the appropriate place (onCreate) in the TextNoteEditor and set text
    }

}
