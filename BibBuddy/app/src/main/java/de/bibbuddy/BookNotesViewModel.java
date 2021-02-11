package de.bibbuddy;

import android.content.Context;

import java.util.List;

public class BookNotesViewModel {


    private final Context context;
    private final NoteModel noteModel;
    private List<NoteItem> noteList;


    public BookNotesViewModel(Context context) {
        this.context = context;
        this.noteModel = new NoteModel(context);
    }

    public NoteModel getNoteModel(){
        return noteModel;
    }


    public List<NoteItem> getNoteList(Long bookId) {
        noteList = noteModel.getNoteListForABook(bookId);
        return noteList;
    }

    public NoteItem getSelectedNoteItem(int position) {
        return noteList.get(position);
    }

}

