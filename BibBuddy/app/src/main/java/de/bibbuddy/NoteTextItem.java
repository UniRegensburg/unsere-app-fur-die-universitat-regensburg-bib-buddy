package de.bibbuddy;

public class NoteTextItem extends NoteItem {
    public NoteTextItem(String name, Long id) {
        super(name, R.drawable.document, id);
    }

    public NoteTextItem(String name, Long id, Long bookId) {
        super(name, R.drawable.document, id, bookId);
    }

}