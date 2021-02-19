package de.bibbuddy;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import java.util.ArrayList;
import java.util.List;

/**
 * BookDao contains all sql queries related to Book.
 *
 * @author Sarah Kurek
 */
public class BookDao implements InterfaceBookDao {
  private final DatabaseHelper dbHelper;
  private final AuthorDao authorDao;


  public BookDao(DatabaseHelper dbHelper) {
    this.dbHelper = dbHelper;
    this.authorDao = new AuthorDao(dbHelper);
  }

  @Override
  public boolean create(Book book) {
    long currentTime = System.currentTimeMillis() / 1_000L;
    SQLiteDatabase db = dbHelper.getWritableDatabase();
    try {
      ContentValues contentValues = new ContentValues();
      contentValues.put(DatabaseHelper.ISBN, book.getIsbn());
      contentValues.put(DatabaseHelper.TITLE, book.getTitle());
      contentValues.put(DatabaseHelper.SUBTITLE, book.getSubtitle());
      contentValues.put(DatabaseHelper.PUB_YEAR, book.getPubYear());
      contentValues.put(DatabaseHelper.PUBLISHER, book.getPublisher());
      contentValues.put(DatabaseHelper.VOLUME, book.getVolume());
      contentValues.put(DatabaseHelper.EDITION, book.getEdition());
      contentValues.put(DatabaseHelper.ADD_INFOS, book.getAddInfo());
      contentValues.put(DatabaseHelper.CREATE_DATE, currentTime);
      contentValues.put(DatabaseHelper.MOD_DATE, currentTime);
      long id = db.insert(DatabaseHelper.TABLE_NAME_BOOK, null, contentValues);
      book.setId(id);
    } catch (SQLiteException ex) {
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

    authorDao.createAuthors(authorList);
    linkBookWithAuthors(authorList, bookId, authorDao.getAuthorIds(authorList));
  }

  // get a single book entry by id
  @Override
  public Book findById(Long id) {
    SQLiteDatabase db = dbHelper.getReadableDatabase();

    Cursor cursor = db.query(DatabaseHelper.TABLE_NAME_BOOK,
        new String[] {DatabaseHelper._ID, DatabaseHelper.ISBN,
            DatabaseHelper.TITLE, DatabaseHelper.SUBTITLE, DatabaseHelper.PUB_YEAR,
            DatabaseHelper.PUBLISHER,
            DatabaseHelper.VOLUME, DatabaseHelper.EDITION, DatabaseHelper.ADD_INFOS,
            DatabaseHelper.CREATE_DATE,
            DatabaseHelper.MOD_DATE}, DatabaseHelper._ID + "=?",
        new String[] {String.valueOf(id)}, null, null, null, null);

    Book book = null;
    if (cursor != null) {
      cursor.moveToFirst();

      book = new Book(
          Long.parseLong(cursor.getString(0)), // Id
          cursor.getString(1), // Isbn
          cursor.getString(2), // Title
          cursor.getString(3), // Subtitle
          Integer.parseInt(cursor.getString(4)), // Pub year
          cursor.getString(5), // Publisher
          cursor.getString(6), // Volume
          cursor.getString(7), // Edition
          cursor.getString(8), // Add Infos
          Integer.parseInt(cursor.getString(9)), // Create date
          Integer.parseInt(cursor.getString(10)) // Mod date
      );

      cursor.close();
    }
    return book;
  }


  // get all books in a list view
  @Override
  public List<Book> findAll() {
    List<Book> bookList = new ArrayList<Book>();
    String selectQuery = "SELECT  * FROM " + DatabaseHelper.TABLE_NAME_AUTHOR;

    SQLiteDatabase db = dbHelper.getWritableDatabase();
    Cursor cursor = db.rawQuery(selectQuery, null);

    // looping through all rows and adding to list
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
        book.setCreateDate(Integer.parseInt(cursor.getString(9)));
        book.setModDate(Integer.parseInt(cursor.getString(10)));

        bookList.add(book);

      } while (cursor.moveToNext());
      cursor.close();
    }

    return bookList;
  }


  // delete single book entry
  @Override
  public void delete(Long id) {
    SQLiteDatabase db = dbHelper.getWritableDatabase();
    db.delete(DatabaseHelper.TABLE_NAME_BOOK, DatabaseHelper._ID + " = ?",
        new String[] {String.valueOf(id)});

    db.close();
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
    if (cursor != null) {
      cursor.moveToFirst();
      id = cursor.getLong(0); // Id
      cursor.close();
    }

    return id;
  }


  private void linkBookWithShelf(Long shelfId, Long bookId) {
    SQLiteDatabase db = dbHelper.getWritableDatabase();

    try {
      ContentValues contentValues = new ContentValues();
      contentValues.put(DatabaseHelper.BOOK_ID, bookId);
      contentValues.put(DatabaseHelper.SHELF_ID, shelfId);
      db.insert(DatabaseHelper.TABLE_NAME_SHELF_BOOK_LNK, null, contentValues);
    } finally {
      db.close();
    }
  }

  private void linkBookWithAuthors(List<Author> authorList, Long bookId, List<Long> authorIds) {
    SQLiteDatabase db = dbHelper.getWritableDatabase();

    for (Long id : authorIds) {
      try {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DatabaseHelper.AUTHOR_ID, id);
        contentValues.put(DatabaseHelper.BOOK_ID, bookId);
        db.insert(DatabaseHelper.TABLE_NAME_AUTHOR_BOOK_LNK, null, contentValues);
      } catch (Exception e) {
        e.printStackTrace();
      }

    }
    if (db != null) {
      db.close();
    }
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
    String selectQuery = "SELECT  * FROM " + DatabaseHelper.TABLE_NAME_SHELF_BOOK_LNK + " WHERE "
        + DatabaseHelper.SHELF_ID + "=" + shelfId;

    Cursor cursor = db.rawQuery(selectQuery, null);

    if (cursor.moveToFirst()) {
      do {
        // Id, ShelfId, BookId
        bookIds.add(Long.parseLong(cursor.getString(2)));
      } while (cursor.moveToNext());
      cursor.close();
    }

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

  private List<Long> getAllAuthorsIdsForBook(Long bookId) {
    SQLiteDatabase db = dbHelper.getReadableDatabase();
    List<Long> authorIds = new ArrayList<Long>();
    String selectQuery = "SELECT  * FROM " + DatabaseHelper.TABLE_NAME_AUTHOR_BOOK_LNK + " WHERE "
        + DatabaseHelper.BOOK_ID + "=" + bookId;

    Cursor cursor = db.rawQuery(selectQuery, null);

    if (cursor.moveToFirst()) {
      do {
        // Id, AuthorId, BookId
        authorIds.add(Long.parseLong(cursor.getString(1)));
      } while (cursor.moveToNext());
      cursor.close();
    }

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
    List<Long> authorIds = getAllAuthorsIdsForBook(bookId);
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
            + " WHERE " + DatabaseHelper.BOOK_ID + "=" + bookId;

    Cursor cursor = db.rawQuery(selectQuery, null);

    if (cursor.moveToFirst()) {
      do {
        noteCount = Integer.parseInt(cursor.getString(0));
      } while (cursor.moveToNext());
      cursor.close();
    }

    return noteCount;
  }

}
