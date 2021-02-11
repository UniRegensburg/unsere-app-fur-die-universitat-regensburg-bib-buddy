package de.bibbuddy;

import android.content.Context;
import java.util.ArrayList;
import java.util.List;

public class BookModel {
  private final BookDao bookDao;
  private final Long shelfId;

  private List<BookItem> bookList;

  public BookModel(Context context, Long shelfId) {
    this.shelfId = shelfId;
    DatabaseHelper databaseHelper = new DatabaseHelper(context);
    this.bookDao = new BookDao(databaseHelper);
  }

  private String convertAuthorListToString(List<Author> authorList) {
    StringBuilder authors = new StringBuilder();

    boolean savedAuthor = false;
    for (Author author : authorList) {
      if (savedAuthor) {
        authors.append(", ");
      }
      if (author.getTitle() != null) {
        authors.append(author.getTitle()).append(" ");
      }
      authors.append(author.getFirstName()).append(" ").append(author.getLastName());
      savedAuthor = true;
    }

    return authors.toString();
  }

  public void addBook(Book book, List<Author> authorList) {
    bookDao.create(book, authorList, shelfId);
    String authors = convertAuthorListToString(authorList);
    bookList
        .add(new BookItem(book.getTitle(), book.getId(), shelfId, book.getPubYear(), authors, 0));
  }

  public List<BookItem> getBookList(Long shelfId) {
    bookList = new ArrayList<>();
    List<Book> bookDbList = bookDao.getAllBooksForShelf(shelfId);

    for (Book book : bookDbList) {
      List<Author> authorList = bookDao.getAllAuthorsForBook(book.getId());
      int noteCount = bookDao.countAllNotesForBook(book.getId());
      bookList.add(new BookItem(book.getTitle(), book.getId(), shelfId, book.getPubYear(),
          convertAuthorListToString(authorList), noteCount));
    }

    return bookList;
  }

  public List<BookItem> getCurrentBookList() {
    return bookList;
  }

  public BookItem getSelectedBookItem(int position) {
    return bookList.get(position);
  }
}
