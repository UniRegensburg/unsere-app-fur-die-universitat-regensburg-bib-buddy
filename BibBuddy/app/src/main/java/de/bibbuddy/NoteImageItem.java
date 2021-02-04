package de.bibbuddy;

public class NoteImageItem extends NoteItem {
    public NoteImageItem(String name, Long id) {
        super(name, R.drawable.picture, id);
    }

    public NoteImageItem(String name, Long id, Long bookId) {
        super(name, R.drawable.picture, id, bookId);
    }
}