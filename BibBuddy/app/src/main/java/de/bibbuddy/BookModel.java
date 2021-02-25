package de.bibbuddy;

import android.content.Context;
import java.util.ArrayList;
import java.util.List;

/**
 * The BookModel contains all the book data for the BookFragment.
 *
 * @author Claudia Schönherr
 */

public class BookModel {
  private final BookDao bookDao;
  private final AuthorDao authorDao;
  private final NoteDao noteDao;
  private final Long shelfId;

  private List<BookItem> bookList;

  /**
   * Constructor for a BookModel.
   *
   * @param context context for the BookModel
   * @param shelfId shelfId of the selected book
   */

  public BookModel(Context context, Long shelfId) {
    this.shelfId = shelfId;
    DatabaseHelper databaseHelper = new DatabaseHelper(context);
    this.bookDao = new BookDao(databaseHelper);
    this.authorDao = new AuthorDao(databaseHelper);
    this.noteDao = new NoteDao(databaseHelper);
  }

  private String convertAuthorListToString(List<Author> authorList) {
    if (authorList == null) {
      return "";
    }

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

  /**
   * Adds a new book to the bookList and database.
   *
   * @param book       book data for the database and bookList
   * @param authorList authorList of the new book
   */
  public void addBook(Book book, List<Author> authorList) {
    bookDao.create(book, authorList, shelfId);
    String authors = convertAuthorListToString(authorList);

    bookList
        .add(new BookItem(book.getTitle(), book.getId(), shelfId, book.getPubYear(), authors, 0));
  }

  /**
   * Gets the bookList of the current shelfId.
   *
   * @param shelfId shelfId of the given shelf
   * @return Returns the bookList of the current shelfId
   */
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

  private void deleteAuthors(Long bookId) {
    List<Long> authorIds = bookDao.getAllAuthorsIdsForBook(bookId);

    for (Long authorId : authorIds) {
      authorDao.delete(authorId, bookId);
    }
  }

  private void deleteNotes(Long bookId) {
    List<Long> noteIds = noteDao.getAllNoteIdsForBook(bookId);

    for (Long noteId : noteIds) {
      noteDao.delete(noteId);
    }

  }

  /**
   * Deletes all selected books and their respective notes.
   *
   * @param selectedBookItems selected book items
   */
  public void deleteBooks(List<BookItem> selectedBookItems) {
    if (selectedBookItems == null) {
      return;
    }

    for (BookItem book : selectedBookItems) {
      Long bookId = book.getId();

      deleteNotes(bookId);
      deleteAuthors(bookId);

      bookDao.delete(bookId);
      deleteBookFromBookList(book);
    }
  }

  private void deleteBookFromBookList(BookItem book) {
    for (int i = 0; i < bookList.size(); i++) {
      if (book.equals(bookList.get(i))) {
        bookList.remove(i);
      }
    }
  }

  public List<BookItem> getCurrentBookList() {
    return bookList;
  }

  public BookItem getSelectedBookItem(int position) {
    return bookList.get(position);
  }
}
