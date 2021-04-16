package de.bibbuddy;

/**
 * The BookItem is responsible for holding the information of the book view items.
 * It is a child of the LibraryItem.
 *
 * @author Claudia Schönherr
 */
public class BookItem extends LibraryItem {
  private final int year;
  private final String authors;
  private final int noteCount;

  /**
   * Constructor for a BookItem.
   *
   * @param book      book from database
   * @param shelfId   shelfId of the book
   * @param authors   authors of the book
   * @param noteCount total number of notes in the book
   */
  public BookItem(Book book, Long shelfId, String authors, int noteCount) {
    super(book.getTitle(), R.drawable.ic_book, book.getId(), shelfId,
          book.getModDate());

    this.year = book.getPubYear();
    this.authors = authors;
    this.noteCount = noteCount;
  }

  public int getYear() {
    return year;
  }

  public String getAuthors() {
    return authors;
  }

  public int getNoteCount() {
    return noteCount;
  }

}
