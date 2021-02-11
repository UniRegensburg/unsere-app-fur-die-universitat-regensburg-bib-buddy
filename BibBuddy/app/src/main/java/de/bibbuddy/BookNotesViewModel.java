package de.bibbuddy;

import android.content.Context;

import java.util.List;

public class BookNotesViewModel {


    private final Context context;
    private final NoteModel noteModel;

    private List<NoteItem> noteList;


    public BookNotesViewModel(Context context) {
        this.context = context;
        DatabaseHelper databaseHelper = new DatabaseHelper(context);
        this.noteModel = new NoteModel(context);
    }


    public List<NoteItem> getNoteList(Long bookId) {
        List<NoteItem> list = noteModel.getNoteListForABook(bookId);
        return list;
    }

    public NoteItem getSelectedNoteItem(int position) {
        return noteList.get(position);
    }

}

