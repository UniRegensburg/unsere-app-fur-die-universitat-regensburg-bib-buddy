package de.bibbuddy;

public class NoteItem extends LibraryItem {

    public NoteItem(String name, int image, Long id) {
        super(name, R.drawable.ic_file, id);
    }

    public NoteItem(String name, int image, Long id, Long bookId) {
        super(name, R.drawable.ic_file, id, bookId);
    }
}
