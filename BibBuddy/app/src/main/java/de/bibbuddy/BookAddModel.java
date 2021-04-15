package de.bibbuddy;

import android.content.Context;
import java.util.List;

/**
 * The BookAddModel contains the db query for adding a book.
 *
 * @author Claudia Schönherr
 */
public class BookAddModel {
  private final BookDao bookDao;

  public BookAddModel(Context context) {
    DatabaseHelper databaseHelper = new DatabaseHelper(context);
    this.bookDao = new BookDao(databaseHelper);
  }

  public void addBook(Book book, List<Author> authorList, Long shelfId) {
    bookDao.create(book, authorList, shelfId);
  }

}
