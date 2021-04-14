package de.bibbuddy;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * BookDao contains all sql queries related to Book.
 *
 * @author Sarah Kurek, Claudia Schönherr, Silvia Ivanova, Luis Moßburger
 */
public class BookDao implements InterfaceBookDao {

  private static final String TAG = BookDao.class.getSimpleName();

  private final DatabaseHelper dbHelper;
  private final AuthorDao authorDao;


  public BookDao(DatabaseHelper dbHelper) {
    this.dbHelper = dbHelper;
    this.authorDao = new AuthorDao(dbHelper);
  }

  @Override
  public boolean create(Book book) {
    SQLiteDatabase db = dbHelper.getWritableDatabase();

    try {
      Long currentTime = new Date().getTime();

      ContentValues contentValues = createBookContentValues(book);
      contentValues.put(DatabaseHelper.CREATE_DATE, currentTime);

      long id = db.insert(DatabaseHelper.TABLE_NAME_BOOK, null, contentValues);
      book.setId(id);

    } catch (SQLiteException ex) {
      Log.e(TAG, ex.toString(), ex);
      return false;
    } finally {
      db.close();
    }
    return true;
  }

  /**
   * Method to create a new book in the database, link it with a shelf and add a author to it.
   *
   * @param book       Instance of book
   * @param authorList list of authors
   * @param shelfId    current shelfId
   */
  public void create(Book book, List<Author> authorList, Long shelfId) {
    create(book);
    Long bookId = findLatestId();
    linkBookWithShelf(shelfId, bookId);

    if (authorList == null || authorList.isEmpty()) {
      return;
    }

    authorDao.createOrUpdateAuthors(authorList);
    List<Long> authorIds = authorList.stream().map(Author::getId).collect(Collectors.toList());

    linkBookWithAuthors(bookId, authorIds);
  }

  // Gets a single book entry by id
  @Override
  public Book findById(Long id) {
    SQLiteDatabase db = dbHelper.getReadableDatabase();

    Cursor cursor = db.query(DatabaseHelper.TABLE_NAME_BOOK,
                             new String[] {DatabaseHelper._ID, DatabaseHelper.ISBN,
                                 DatabaseHelper.TITLE, DatabaseHelper.SUBTITLE,
                                 DatabaseHelper.PUB_YEAR,
                                 DatabaseHelper.PUBLISHER,
                                 DatabaseHelper.VOLUME, DatabaseHelper.EDITION,
                                 DatabaseHelper.ADD_INFOS,
                                 DatabaseHelper.CREATE_DATE,
                                 DatabaseHelper.MOD_DATE}, DatabaseHelper._ID + " = ?",
                             new String[] {String.valueOf(id)}, null, null, null, null);

    Book book = null;
    if (cursor.moveToFirst()) {
      book = createBookData(cursor);
    }

    cursor.close();

    return book;
  }


  // Gets all books in a list view
  @Override
  public List<Book> findAll() {
    List<Book> bookList = new ArrayList<Book>();
    String selectQuery = "SELECT * FROM " + DatabaseHelper.TABLE_NAME_AUTHOR;

    SQLiteDatabase db = dbHelper.getWritableDatabase();
    Cursor cursor = db.rawQuery(selectQuery, null);

    if (cursor.moveToFirst()) {
      do {
        bookList.add(createBookData(cursor));

      } while (cursor.moveToNext());
    }

    cursor.close();

    return bookList;
  }


  // Deletes single book entry
  @Override
  @Deprecated
  public void delete(Long id) {
    SQLiteDatabase db = dbHelper.getWritableDatabase();
    db.delete(DatabaseHelper.TABLE_NAME_BOOK, DatabaseHelper._ID + " = ?",
              new String[] {String.valueOf(id)});

    db.close();
  }

  /**
   * Deletes all the relevant book data in the tables.
   *
   * @param bookId  id of the book
   * @param shelfId id of the shelf
   */
  public void delete(Long bookId, Long shelfId) {
    SQLiteDatabase db = dbHelper.getWritableDatabase();

    db.delete(DatabaseHelper.TABLE_NAME_SHELF_BOOK_LNK, DatabaseHelper.BOOK_ID + " = ?"
                  + " AND " + DatabaseHelper.SHELF_ID + " = ?",
              new String[] {String.valueOf(bookId), String.valueOf(shelfId)});

    db.delete(DatabaseHelper.TABLE_NAME_BOOK, DatabaseHelper._ID + " = ?",
              new String[] {String.valueOf(bookId)});

    db.close();
  }

  private Book createBookData(Cursor cursor) {

    return new Book(
        Long.parseLong(cursor.getString(0)),
        cursor.getString(1),
        cursor.getString(2),
        cursor.getString(3),
        Integer.parseInt(cursor.getString(4)),
        cursor.getString(5),
        cursor.getString(6),
        cursor.getString(7),
        cursor.getString(8),
        Long.parseLong(cursor.getString(9)),
        Long.parseLong(cursor.getString(10))
    );
  }

  private ContentValues createBookContentValues(Book book) {
    ContentValues bookContentValues = new ContentValues();

    bookContentValues.put(DatabaseHelper.ISBN, book.getIsbn());
    bookContentValues.put(DatabaseHelper.TITLE, book.getTitle());
    bookContentValues.put(DatabaseHelper.SUBTITLE, book.getSubtitle());
    bookContentValues.put(DatabaseHelper.PUB_YEAR, book.getPubYear());
    bookContentValues.put(DatabaseHelper.PUBLISHER, book.getPublisher());
    bookContentValues.put(DatabaseHelper.VOLUME, book.getVolume());
    bookContentValues.put(DatabaseHelper.EDITION, book.getEdition());
    bookContentValues.put(DatabaseHelper.ADD_INFOS, book.getAddInfo());
    bookContentValues.put(DatabaseHelper.MOD_DATE, new Date().getTime());

    return bookContentValues;
  }

  /**
   * Method to find the last added id.
   *
   * @return last added bookId
   */
  public Long findLatestId() {
    SQLiteDatabase db = dbHelper.getReadableDatabase();
    String selectQuery = "SELECT " + DatabaseHelper._ID + " FROM "
        + DatabaseHelper.TABLE_NAME_BOOK + " ORDER BY " + DatabaseHelper._ID + " DESC LIMIT 1";

    Cursor cursor = db.rawQuery(selectQuery, null);

    Long id = null;
    if (cursor.moveToFirst()) {
      id = cursor.getLong(0); // Id
    }

    cursor.close();

    return id;
  }


  private void linkBookWithShelf(Long shelfId, Long bookId) {
    SQLiteDatabase db = dbHelper.getWritableDatabase();

    try {
      ContentValues contentValues = new ContentValues();
      contentValues.put(DatabaseHelper.BOOK_ID, bookId);
      contentValues.put(DatabaseHelper.SHELF_ID, shelfId);
      db.insert(DatabaseHelper.TABLE_NAME_SHELF_BOOK_LNK, null, contentValues);
    } catch (SQLiteException ex) {
      Log.e(TAG, ex.toString(), ex);
    } finally {
      db.close();
    }
  }

  private void linkBookWithAuthors(Long bookId, List<Long> authorIds) {
    SQLiteDatabase db = dbHelper.getWritableDatabase();

    try {
      List<Long> existingAuthorIds = getAllAuthorIdsForBook(bookId);
      existingAuthorIds.stream()
          .filter(id -> !authorIds.contains(id))
          .forEach(id -> deleteAuthorBookLink(db, bookId, id));

      authorIds.stream()
          .filter(id -> !existingAuthorIds.contains(id))
          .forEach(id -> insertAuthorBookLink(db, bookId, id));
    } catch (SQLException ex) {
      Log.e(TAG, ex.toString(), ex);
    } finally {
      db.close();
    }
  }

  private void insertAuthorBookLink(SQLiteDatabase db, Long bookId, Long authorId) {
    try {
      ContentValues contentValues = new ContentValues();
      contentValues.put(DatabaseHelper.AUTHOR_ID, authorId);
      contentValues.put(DatabaseHelper.BOOK_ID, bookId);
      db.insert(DatabaseHelper.TABLE_NAME_AUTHOR_BOOK_LNK, null, contentValues);
    } catch (Exception ex) {
      Log.e(TAG, ex.toString(), ex);
    }
  }

  private void deleteAuthorBookLink(SQLiteDatabase db, Long bookId, Long authorId) {
    db.delete(DatabaseHelper.TABLE_NAME_AUTHOR_BOOK_LNK, DatabaseHelper.BOOK_ID + " = ?"
                  + " AND " + DatabaseHelper.AUTHOR_ID + " = ?",
              new String[] {bookId.toString(), authorId.toString()});
  }

  /**
   * Method to get all bookIds for a specific Shelf with its shelfId.
   *
   * @param shelfId current shelfId
   * @return all bookIds for current shelf
   */
  public List<Long> getAllBookIdsForShelf(Long shelfId) {
    SQLiteDatabase db = dbHelper.getReadableDatabase();
    List<Long> bookIds = new ArrayList<Long>();
    String selectQuery = "SELECT * FROM " + DatabaseHelper.TABLE_NAME_SHELF_BOOK_LNK + " WHERE "
        + DatabaseHelper.SHELF_ID + " = ?";

    Cursor cursor = db.rawQuery(selectQuery, new String[] {String.valueOf(shelfId)});

    if (cursor.moveToFirst()) {
      do {
        // Id, ShelfId, BookId
        bookIds.add(Long.parseLong(cursor.getString(2)));
      } while (cursor.moveToNext());
    }

    cursor.close();

    return bookIds;
  }


  /**
   * Method to get all Books for a specific Shelf with a list of all bookIds.
   *
   * @param shelfId current shelfId
   * @return list of all books for current shelf
   */
  public List<Book> getAllBooksForShelf(Long shelfId) {
    List<Long> bookIds = getAllBookIdsForShelf(shelfId);
    List<Book> bookList = new ArrayList<Book>();

    for (Long id : bookIds) {
      bookList.add(findById(id));
    }

    return bookList;
  }

  /**
   * Method to get all Books for a specific Shelf with a list of all bookIds.
   *
   * @param bookId id of the book
   * @return list of all authors of a book
   */
  public List<Long> getAllAuthorIdsForBook(Long bookId) {
    SQLiteDatabase db = dbHelper.getReadableDatabase();
    List<Long> authorIds = new ArrayList<Long>();
    String selectQuery = "SELECT * FROM " + DatabaseHelper.TABLE_NAME_AUTHOR_BOOK_LNK + " WHERE "
        + DatabaseHelper.BOOK_ID + " = ?";

    Cursor cursor = db.rawQuery(selectQuery, new String[] {String.valueOf(bookId)});

    if (cursor.moveToFirst()) {
      do {
        // Id, AuthorId, BookId
        authorIds.add(Long.parseLong(cursor.getString(1)));
      } while (cursor.moveToNext());
    }

    cursor.close();

    return authorIds;
  }


  /**
   * Method to get all Authors for a specific Book with its bookId.
   *
   * @param bookId current bookId
   * @return list of all authors for the current book
   */
  public List<Author> getAllAuthorsForBook(Long bookId) {
    List<Author> authorList = new ArrayList<Author>();
    List<Long> authorIds = getAllAuthorIdsForBook(bookId);
    for (Long id : authorIds) {
      authorList.add(authorDao.findById(id));
    }

    return authorList;
  }

  /**
   * Method to count all Notes for a specific Book.
   *
   * @param bookId current bookId
   * @return count of all notes that belong to the current book
   */
  public int countAllNotesForBook(Long bookId) {
    SQLiteDatabase db = dbHelper.getReadableDatabase();
    int noteCount = 0;

    String selectQuery =
        "SELECT COUNT(" + DatabaseHelper._ID + ") FROM " + DatabaseHelper.TABLE_NAME_BOOK_NOTE_LNK
            + " WHERE " + DatabaseHelper.BOOK_ID + " = ?";

    Cursor cursor = db.rawQuery(selectQuery, new String[] {String.valueOf(bookId)});

    if (cursor.moveToFirst()) {
      do {
        noteCount = Integer.parseInt(cursor.getString(0));
      } while (cursor.moveToNext());
    }

    cursor.close();

    return noteCount;
  }

  /**
   * Finds all books which contain searchInput.
   *
   * @param searchInput searchInput of the user
   * @return Returns a list of books which have the searchInput in the name
   */
  public List<Book> findBooksByTitle(String searchInput) {
    List<Book> bookList = new ArrayList<>();

    SQLiteDatabase db = dbHelper.getReadableDatabase();

    String selectQuery = "SELECT * FROM " + DatabaseHelper.TABLE_NAME_BOOK + " WHERE "
        + DatabaseHelper.TITLE + " LIKE '%" + searchInput + "%'";

    Cursor cursor = db.rawQuery(selectQuery, null);

    if (cursor.moveToFirst()) {
      do {
        Book book = createBookData(cursor);
        bookList.add(book);

      } while (cursor.moveToNext());
    }

    cursor.close();

    return bookList;
  }

  /**
   * Method to update an existing book.
   *
   * @param book book data for the database and bookList
   */
  public void updateBook(Book book, List<Author> authorList) {

    ContentValues contentValues = createBookContentValues(book);

    SQLiteDatabase db = dbHelper.getWritableDatabase();

    try {
      dbHelper.getWritableDatabase().update(DatabaseHelper.TABLE_NAME_BOOK, contentValues,
                                            DatabaseHelper._ID + " = ?",
                                            new String[] {String.valueOf(book.getId())});
    } catch (SQLiteException ex) {
      Log.e(TAG, ex.toString(), ex);
    } finally {
      db.close();
    }

    if (authorList == null || authorList.isEmpty()) {
      return;
    }

    authorDao.createOrUpdateAuthors(authorList);
    List<Long> authorIds = authorList.stream().map(Author::getId).collect(Collectors.toList());
    linkBookWithAuthors(book.getId(), authorIds);
  }

  /**
   * Method that finds all books in the database.
   *
   * @return bookList all books as a list
   */
  public List<Book> findAllBooks() {
    List<Book> bookList = new ArrayList<Book>();

    String selectQuery = "SELECT * FROM " + DatabaseHelper.TABLE_NAME_BOOK;
    SQLiteDatabase db = dbHelper.getWritableDatabase();
    Cursor cursor = db.rawQuery(selectQuery, null);

    if (cursor.moveToFirst()) {
      do {
        Book book = new Book();

        book.setId(Long.parseLong(cursor.getString(0)));
        book.setIsbn(cursor.getString(1));
        book.setTitle(cursor.getString(2));
        book.setSubtitle(cursor.getString(3));
        book.setPubYear(Integer.parseInt(cursor.getString(4)));
        book.setPublisher(cursor.getString(5));
        book.setVolume(cursor.getString(6));
        book.setEdition(cursor.getString(7));
        book.setAddInfo(cursor.getString(8));
        book.setCreateDate(Long.parseLong(cursor.getString(9)));
        book.setModDate(Long.parseLong(cursor.getString(10)));

        bookList.add(book);
      } while (cursor.moveToNext());
    }

    cursor.close();

    return bookList;
  }

  /**
   * Method that finds the shelfId of a book in the database.
   *
   * @param id id of the book
   * @return the shelfId of the book
   */
  public Long findShelfIdByBook(Long id) {
    SQLiteDatabase db = dbHelper.getReadableDatabase();

    String selectQuery =
        "SELECT " + DatabaseHelper.SHELF_ID + " FROM " + DatabaseHelper.TABLE_NAME_SHELF_BOOK_LNK
            + " WHERE " + DatabaseHelper.BOOK_ID + " = ? LIMIT 1";

    Cursor cursor = db.rawQuery(selectQuery, new String[] {String.valueOf(id)});

    Long shelfId = 0L;
    if (cursor.moveToFirst()) {
      shelfId = Long.parseLong(cursor.getString(0));
    }

    cursor.close();

    return shelfId;
  }

  /**
   * Find the shelf name of a book in the database.
   *
   * @param id of the book.
   * @return the shelf name of the book.
   */
  public String findShelfNameByBook(Long id) {
    Long shelfId = findShelfIdByBook(id);
    SQLiteDatabase db = dbHelper.getReadableDatabase();

    String selectQuery =
        "SELECT " + DatabaseHelper.NAME + " FROM " + DatabaseHelper.TABLE_NAME_SHELF
            + " WHERE " + DatabaseHelper._ID + " = ? LIMIT 1";

    Cursor cursor = db.rawQuery(selectQuery, new String[] {String.valueOf(shelfId)});

    String shelfName = "";
    if (cursor.moveToFirst()) {
      shelfName = cursor.getString(0);
    }

    cursor.close();

    return shelfName;
  }

  /**
   * Find an amount of last modified books.
   *
   * @param amount of books to retrieve.
   * @return a list of the retrieved books.
   */
  public List<Book> findModifiedBooks(int amount) {
    List<Book> bookList = new ArrayList<Book>();
    List<Long> bookIds = new ArrayList<Long>();

    SQLiteDatabase db = dbHelper.getReadableDatabase();

    String selectQuery = "SELECT " + DatabaseHelper._ID + " FROM "
        + DatabaseHelper.TABLE_NAME_BOOK + " ORDER BY " + DatabaseHelper.MOD_DATE + " DESC LIMIT ?";

    Cursor cursor = db.rawQuery(selectQuery, new String[] {String.valueOf(amount)});

    if (cursor.moveToFirst()) {
      do {
        bookIds.add(Long.parseLong(cursor.getString(0)));
      } while (cursor.moveToNext());
    }

    cursor.close();

    for (Long id : bookIds) {
      bookList.add(findById(id));
    }

    return bookList;
  }

}
