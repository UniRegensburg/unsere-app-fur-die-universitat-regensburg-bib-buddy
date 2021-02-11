package de.bibbuddy;

public class BookItem extends LibraryItem {
  private int year;
  private String authors;
  private int noteCount;

  public BookItem(String title, Long id) {
    super(title, R.drawable.ic_book, id);
  }

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
