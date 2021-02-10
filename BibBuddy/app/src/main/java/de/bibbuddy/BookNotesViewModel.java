package de.bibbuddy;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

public class BookNotesViewModel {


    private final Context context;
    private final NoteDAO noteDao;

    private List<NoteItem> noteList;


    public BookNotesViewModel(Context context) {
        this.context = context;
        DatabaseHelper databaseHelper = new DatabaseHelper(context);
        this.noteDao = new NoteDAO(databaseHelper);
    }


    public List<NoteItem> getNoteList(Long bookId) {
        List<Note> list = noteDao.getAllNotesForBook(bookId);

        noteList = new ArrayList<>();
        for (Note item : list) {
            if (item.getType() == 1) { //text
               // noteList.add(new NoteItem(item.getName(), item.getId()));
            } else if (item.getType() == 2) { //image
              //  noteList.add(new NoteImageItem(item.getName(), item.getId()));
            } else if (item.getType() == 3) { //audio
              //  noteList.add(new NoteAudioItem(item.getName(), item.getId()));
            }
        }

        return noteList;

    }

    public NoteItem getSelectedNoteItem(int position) {
        return noteList.get(position);
    }


    // TODO: maybe move method to NoteModel
    public void createNote(String name, Integer type, String text, Long bookId) {
        Note note = new Note(name, type, text);

        noteDao.create(note);
        noteDao.linkNoteWithBook(bookId, note.getId());

    }

    // TODO: maybe move method to NoteModel
    public String getNoteText(Long noteId) {
        return noteDao.findTextById(noteId);
    }

}

