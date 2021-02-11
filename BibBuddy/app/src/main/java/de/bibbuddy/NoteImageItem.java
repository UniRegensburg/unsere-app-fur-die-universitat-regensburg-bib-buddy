package de.bibbuddy;

public class NoteImageItem extends NoteItem {
    public NoteImageItem(String modDate, String name, Long id) {
        super(modDate, name, R.drawable.picture, id);
    }

    public NoteImageItem(String modDate, String name, Long id, Long bookId) {
        super(modDate, name, R.drawable.picture, id, bookId);
    }
}
