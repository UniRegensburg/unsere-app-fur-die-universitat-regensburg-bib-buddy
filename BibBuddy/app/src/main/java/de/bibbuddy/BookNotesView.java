package de.bibbuddy;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class BookNotesView extends Fragment
    implements BookNotesRecyclerViewAdapter.BookNotesViewListener {

  static List<NoteItem> noteList;
  private View view;
  private RecyclerView recyclerView;
  private BookNotesViewModel model;
  private Long bookId;
  private Long shelfId;
  private String shelfName;


  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
    view = inflater.inflate(R.layout.fragment_book_notes_view, container, false);
    recyclerView = view.findViewById(R.id.bookNotesViewRecyclerView);

    model = new BookNotesViewModel(getContext());
    Bundle bundle = this.getArguments();

    bookId = bundle.getLong(LibraryKeys.BOOK_ID);


    shelfId = bundle.getLong(LibraryKeys.SHELF_ID);
    shelfName = bundle.getString(LibraryKeys.SHELF_NAME);

    noteList = model.getNoteList(bookId);

    TextView bookTitleView = view.findViewById(R.id.text_view_book);
    String bookTitle = bundle.getString(LibraryKeys.BOOK_TITLE);
    bookTitleView.setText(bookTitle);

    setupRecyclerView();
    setupAddButton();
    setupBackButton();
    updateNoteListView(noteList);

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
          Bundle bundle = new Bundle();
          bundle.putLong(LibraryKeys.BOOK_ID, bookId);
          TextNoteEditorFragment nextFrag = new TextNoteEditorFragment();
          nextFrag.setArguments(bundle);
          getActivity().getSupportFragmentManager().beginTransaction()
              .replace(R.id.fragment_container_view, nextFrag,
                  LibraryKeys.FRAGMENT_TEXT_NOTE_EDITOR)
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

  private Bundle createBookBundle() {
    Bundle bundle = new Bundle();

    bundle.putLong(LibraryKeys.SHELF_ID, shelfId);
    bundle.putString(LibraryKeys.SHELF_NAME, shelfName);

    return bundle;
  }

  private void setupBackButton() {
    View backButton = view.findViewById(R.id.btn_back);

    backButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        BookFragment nextFrag = new BookFragment();
        nextFrag.setArguments(createBookBundle());

        getActivity().getSupportFragmentManager().beginTransaction()
            .replace(R.id.fragment_container_view, nextFrag, LibraryKeys.FRAGMENT_BOOK)
            .addToBackStack(null)
            .commit();
      }
    });
  }

  private Bundle createNoteBundle(NoteItem item) {
    Bundle bundle = new Bundle();
    Long currentNoteId = item.getId();
    bundle.putLong(LibraryKeys.BOOK_ID, bookId);
    bundle.putLong(LibraryKeys.NOTE_ID, currentNoteId);

    return bundle;
  }

  private void updateNoteListView(List noteList) {
    recyclerView.setAdapter(new BookNotesRecyclerViewAdapter(noteList, this));
    TextView emptyView = view.findViewById(R.id.empty_notelist_view);

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
    nextFrag.setArguments(createNoteBundle(noteItem));
    getActivity().getSupportFragmentManager().beginTransaction()
        .replace(R.id.fragment_container_view, nextFrag, LibraryKeys.FRAGMENT_TEXT_NOTE_EDITOR)
        .addToBackStack(null)
        .commit();

    // TODO: Retrieve data at the appropriate place (onCreate) in the TextNoteEditor and set text
  }

}
