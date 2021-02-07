package de.bibbuddy;

public class ShelfItem extends LibraryItem {
    private int bookCount;
    private int noteCount;

    public ShelfItem(String name, Long id) {
        super(name, R.drawable.ic_shelf, id);
    }

    public ShelfItem(String name, Long id, Long parentId, int bookCount, int noteCount) {
        super(name, R.drawable.ic_shelf, id, parentId);
        this.bookCount = bookCount;
        this.noteCount = noteCount;
    }

    public int getBookCount() {
        return bookCount;
    }

    public void setBookCount(int bookCount) {
        this.bookCount = bookCount;
    }

    public int getNoteCount() {
        return noteCount;
    }

    public void setNoteCount(int noteCount) {
        this.noteCount = noteCount;
    }
}
