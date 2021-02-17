package de.bibbuddy;

/**
 * The ShelfItem is responsible for holding the information of the shelf view items.
 * It is a subclass of the LibraryItem class.
 *
 * @author Claudia Schönherr
 */
public class ShelfItem extends LibraryItem {
  private int bookCount;
  private int noteCount;

  public ShelfItem(String name, Long id) {
    super(name, R.drawable.ic_shelf, id);
  }

  /**
   * Constructor for a BookItem.
   *
   * @param name      title of the shelf
   * @param id        id of the shelf
   * @param parentId  shelfId of the shelf (is deprecated)
   * @param bookCount total number of books in the shelf
   * @param noteCount total number of notes in the shelf
   * @author Claudia Schönherr
   */
  public ShelfItem(String name, Long id, Long parentId, int bookCount, int noteCount) {
    super(name, R.drawable.ic_shelf, id, parentId);
    this.bookCount = bookCount;
    this.noteCount = noteCount;
  }

  public int getBookCount() {
    return bookCount;
  }

  public int getNoteCount() {
    return noteCount;
  }
}
