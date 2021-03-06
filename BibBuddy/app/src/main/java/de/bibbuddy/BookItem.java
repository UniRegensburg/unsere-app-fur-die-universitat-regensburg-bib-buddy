package de.bibbuddy;

/**
 * The BookItem is responsible for holding the information of the book view items.
 * It is a subclass of the LibraryItem class.
 *
 * @author Claudia Schönherr
 */
public class BookItem extends LibraryItem {
  private int year;
  private String authors;
  private int noteCount;

  public BookItem(String title, Long id) {
    super(title, R.drawable.ic_book, id);
  }

  /**
   * Constructor for a BookItem.
   *
   * @param title     title of the book
   * @param id        id of the book
   * @param shelfId   shelfId of the book
   * @param year      published year of the book
   * @param authors   authors of the book
   * @param noteCount total number of notes in the book
   */
  public BookItem(String title, Long id, Long shelfId, int year, String authors, int noteCount) {
    super(title, R.drawable.ic_book, id, shelfId);
    this.year = year;
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
