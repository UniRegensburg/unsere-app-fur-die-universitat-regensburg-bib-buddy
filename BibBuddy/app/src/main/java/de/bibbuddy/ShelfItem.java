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

  /**
   * Constructor for a ShelfItem.
   *
   * @param shelf     shelf from database
   * @param bookCount total number of books in the shelf
   * @param noteCount total number of notes in the shelf
   */
  public ShelfItem(Shelf shelf, int bookCount, int noteCount) {
    super(shelf.getName(), R.drawable.books, shelf.getId(), shelf.getShelfId(),
          shelf.getModDate());

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
