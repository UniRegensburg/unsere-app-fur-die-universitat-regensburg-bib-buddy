package de.bibbuddy;

import android.content.Context;
import java.util.ArrayList;
import java.util.List;

/**
 * The BookModel contains all the book data for the BookFragment.
 *
 * @author Claudia Schönherr, Silvia Ivanova, Luis Moßburger.
 */

public class BookModel {
  private final BookDao bookDao;
  private final AuthorDao authorDao;
  private final NoteDao noteDao;
  private Long shelfId;

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

  /**
   * Gets all books from the databse.
   *
   * @return a list with objects of the class Book
   */
  public List<Book> getAllBooks() {
    return bookDao.findAllBooks();
  }

  /**
   * Gets all books for a shelf with given id.
   *
   * @param id id of a shelf
   * @return a list from type Book
   * with all books from a given shelf
   */
  public List<Book> getAllBooksForShelf(Long id) {
    return bookDao.getAllBooksForShelf(id);
  }

  /**
   * Gets all book Ids from a shelf with given ID.
   *
   * @param id id of a shelf
   * @return a list with all book ids from a given shelf
   */
  public List<Long> getAllBookIdsForShelf(Long id) {
    return bookDao.getAllBookIdsForShelf(id);
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

    book = bookDao.findById(bookDao.findLatestId());

    bookList.add(new BookItem(book, shelfId, authors, 0));
  }

  /**
   * Adds an imported book to the bookList and database.
   * Sets the note counter to 1 because a BibTeX item can only
   * import one note.
   *
   * @param book       book data for the database and bookList
   * @param authorList authorList of the new book
   */
  public void addImportedBook(Book book, List<Author> authorList) {
    bookDao.create(book, authorList, shelfId);
    String authors = convertAuthorListToString(authorList);

    book = bookDao.findById(bookDao.findLatestId());

    bookList
        .add(new BookItem(book, shelfId, authors, 1));
  }

  /**
   * Gets the bookList of the current shelfId.
   *
   * @param shelfId shelfId of the given shelf
   * @return the bookList of the current shelfId
   */
  public List<BookItem> getBookList(Long shelfId) {
    bookList = new ArrayList<>();
    List<Book> bookDbList = bookDao.getAllBooksForShelf(shelfId);

    for (Book book : bookDbList) {
      List<Author> authorList = bookDao.getAllAuthorsForBook(book.getId());
      int noteCount = countAllNotesForBook(book.getId());

      bookList.add(new BookItem(book, shelfId, convertAuthorListToString(authorList), noteCount));
    }

    return bookList;
  }

  public List<Author> getAuthorList(Long bookId) {
    return bookDao.getAllAuthorsForBook(bookId);
  }

  public String getAuthorString(Long bookId) {
    return convertAuthorListToString(getAuthorList(bookId));
  }

  private void deleteAuthors(Long bookId) {
    List<Long> authorIds = bookDao.getAllAuthorIdsForBook(bookId);

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
  public void deleteBooks(List<BookItem> selectedBookItems, Long shelfId) {
    if (selectedBookItems == null || selectedBookItems.isEmpty()) {
      return;
    }

    for (BookItem book : selectedBookItems) {
      Long bookId = book.getId();

      deleteNotes(bookId);
      deleteAuthors(bookId);

      bookDao.delete(bookId, shelfId);
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

  public void updateBook(Book book, List<Author> authorList) {
    bookDao.updateBook(book, authorList);
  }

  public List<BookItem> getCurrentBookList() {
    return bookList;
  }

  public BookItem getSelectedBookItem(int position) {
    return bookList.get(position);
  }

  public Book getBookById(Long id) {
    return bookDao.findById(id);
  }

  public void setShelfId(Long shelfId) {
    this.shelfId = shelfId;
  }

  private void sortBookList(SortCriteria sortCriteria) {
    switch (sortCriteria) {

      case MOD_DATE_LATEST:
        bookList.sort(new SortDate());
        break;

      case MOD_DATE_OLDEST:
        bookList.sort(new SortDate().reversed());
        break;

      case NAME_ASCENDING:
        bookList.sort(new SortName());
        break;

      case NAME_DESCENDING:
        bookList.sort(new SortName().reversed());
        break;

      default:
        break;
    }
  }

  /**
   * Gets the sorted bookList by sortCriteria.
   *
   * @param sortCriteria sortCriteria of the list
   * @return the sorted bookList
   */
  public List<BookItem> getSortedBookList(SortCriteria sortCriteria) {
    sortBookList(sortCriteria);

    return bookList;
  }

  /**
   * Gets the sorted bookList by sortCriteria with the given bookList.
   *
   * @param sortCriteria sortCriteria of the list
   * @param bookList     bookList that should be sorted
   * @return the sorted bookList
   */
  public List<BookItem> getSortedBookList(SortCriteria sortCriteria, List<BookItem> bookList) {
    this.bookList = bookList;
    sortBookList(sortCriteria);

    return bookList;
  }

  /**
   * Find an amount of last modified books.
   *
   * @param amount of books to retrieve.
   * @return a list of the retrieved books.
   */
  public List<Book> findModifiedBooks(int amount) {
    return bookDao.findModifiedBooks(amount);
  }

  /**
   * Method that finds the shelfId of a book in the database.
   *
   * @param id id of the book
   * @return the shelfId of the book
   */
  public Long findShelfIdByBook(Long id) {
    return bookDao.findShelfIdByBook(id);
  }

  /**
   * Find the shelf name of a book in the database.
   *
   * @param id of the book.
   * @return the shelf name of the book.
   */
  public String findShelfNameByBook(Long id) {
    return bookDao.findShelfNameByBook(id);
  }

  /**
   * Method to count all Notes for a specific Book.
   *
   * @param bookId current bookId
   * @return count of all notes that belong to the current book
   */
  public int countAllNotesForBook(Long bookId) {
    return bookDao.countAllNotesForBook(bookId);
  }

}
