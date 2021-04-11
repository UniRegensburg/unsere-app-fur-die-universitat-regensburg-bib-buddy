package de.bibbuddy;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * AuthorDao includes all sql queries related to Author.
 *
 * @author Sarah Kurek, Claudia Schönherr, Silvia Ivanova
 */
public class AuthorDao implements InterfaceAuthorDao {

  private final DatabaseHelper dbHelper;

  private static boolean isNullOrEmpty(String text) {
    return text == null || text.isEmpty();
  }

  public AuthorDao(DatabaseHelper dbHelper) {
    this.dbHelper = dbHelper;
  }

  @Override
  public boolean create(Author author) {
    Long currentTime = new Date().getTime();
    SQLiteDatabase db = dbHelper.getWritableDatabase();

    try {
      ContentValues contentValues = new ContentValues();
      contentValues.put(DatabaseHelper.FIRST_NAME, author.getFirstName());
      contentValues.put(DatabaseHelper.LAST_NAME, author.getLastName());

      if (!isNullOrEmpty(author.getTitle())) {
        contentValues.put(DatabaseHelper.TITLE, author.getTitle());
      }

      contentValues.put(DatabaseHelper.CREATE_DATE, currentTime);
      contentValues.put(DatabaseHelper.MOD_DATE, currentTime);

      long id = db.insert(DatabaseHelper.TABLE_NAME_AUTHOR, null, contentValues);
      author.setId(id);

    } catch (SQLiteException ex) {
      return false;
    } finally {
      db.close();
    }

    return true;
  }

  /**
   * Method to update an existing author.
   *
   * @param author author object
   */
  public void update(Author author) {
    Long currentTime = new Date().getTime();
    SQLiteDatabase db = dbHelper.getWritableDatabase();

    try {
      ContentValues contentValues = new ContentValues();
      contentValues.put(DatabaseHelper.FIRST_NAME, author.getFirstName());
      contentValues.put(DatabaseHelper.LAST_NAME, author.getLastName());
      contentValues.put(DatabaseHelper.TITLE, isNullOrEmpty(
          author.getTitle()) ? null : author.getTitle());
      contentValues.put(DatabaseHelper.MOD_DATE, currentTime);

      db.update(DatabaseHelper.TABLE_NAME_AUTHOR, contentValues,
          DatabaseHelper._ID + " = ?",
          new String[] {String.valueOf(author.getId())});

    } finally {
      db.close();
    }

  }

  // Gets single author entry
  @Override
  public Author findById(Long id) {
    SQLiteDatabase db = dbHelper.getReadableDatabase();

    Cursor cursor = db.query(DatabaseHelper.TABLE_NAME_AUTHOR, new String[] {DatabaseHelper._ID,
        DatabaseHelper.FIRST_NAME, DatabaseHelper.LAST_NAME, DatabaseHelper.TITLE,
        DatabaseHelper.CREATE_DATE, DatabaseHelper.MOD_DATE},
        DatabaseHelper._ID + " = ?",
        new String[] {String.valueOf(id)}, null, null, null, null);

    Author author = null;
    if (cursor.moveToFirst()) {
      author = createAuthorData(cursor);
      cursor.close();
    }
    return author;
  }

  /**
   * Finds an existing author by its title, first and last name.
   *
   * @param authorToFind The author containing the data to search for
   * @return if found, the author (with its database ID), else null
   */
  public Author findByTitleAndFullName(Author authorToFind) {
    List<String> params = new ArrayList<>();
    StringBuilder sb = new StringBuilder();

    sb.append(DatabaseHelper.FIRST_NAME + " = ?");
    params.add(authorToFind.getFirstName());

    sb.append(" AND " + DatabaseHelper.LAST_NAME + " = ?");
    params.add(authorToFind.getLastName());

    sb.append(" AND " + DatabaseHelper.TITLE);
    if (isNullOrEmpty(authorToFind.getTitle())) {
      sb.append(" IS NULL");
    } else {
      sb.append(" = ?");
      params.add(authorToFind.getTitle());
    }

    String selection = sb.toString();
    SQLiteDatabase db = dbHelper.getReadableDatabase();

    Cursor cursor = db.query(DatabaseHelper.TABLE_NAME_AUTHOR,
        new String[] { DatabaseHelper._ID,
            DatabaseHelper.FIRST_NAME,
            DatabaseHelper.LAST_NAME,
            DatabaseHelper.TITLE,
            DatabaseHelper.MOD_DATE,
            DatabaseHelper.CREATE_DATE},
            selection,
        params.toArray(new String[params.size()]),
        null, null, null, null);

    try {
      if (!cursor.moveToFirst()) {
        return null;
      }

      return createAuthorData(cursor);
    } finally {
      cursor.close();
    }
  }

  // Gets all authors in a list view
  @Override
  public List<Author> findAll() {
    List<Author> authorList = new ArrayList<Author>();

    String selectQuery = "SELECT * FROM " + DatabaseHelper.TABLE_NAME_AUTHOR;

    SQLiteDatabase db = dbHelper.getWritableDatabase();
    Cursor cursor = db.rawQuery(selectQuery, null);

    if (cursor.moveToFirst()) {
      do {
        Author author = createAuthorData(cursor);

        authorList.add(author);
      } while (cursor.moveToNext());
    }

    cursor.close();

    return authorList;
  }

  // Deletes single author entry
  @Override
  public void delete(Long id) {
    SQLiteDatabase db = dbHelper.getWritableDatabase();
    db.delete(DatabaseHelper.TABLE_NAME_AUTHOR, DatabaseHelper._ID + " = ?",
        new String[] {String.valueOf(id)});

    db.close();
  }


  /**
   * Deletes the relevant author entries.
   *
   * @param authorId Id of the author
   * @param bookId   Id of the book
   */
  public void delete(Long authorId, Long bookId) {
    SQLiteDatabase db = dbHelper.getWritableDatabase();

    db.delete(DatabaseHelper.TABLE_NAME_AUTHOR_BOOK_LNK, DatabaseHelper.AUTHOR_ID
            + " = ?" + " AND " + DatabaseHelper.BOOK_ID + " = ?",
        new String[] {String.valueOf(authorId), String.valueOf(bookId)});

    // delete author only if author has no link to another book
    if (!existsAuthorBookLink(authorId)) {
      db.delete(DatabaseHelper.TABLE_NAME_AUTHOR, DatabaseHelper._ID + " = ?",
          new String[] {String.valueOf(authorId)});
    }

    db.close();
  }

  private Author createAuthorData(Cursor cursor) {
    return new Author(Long.parseLong(cursor.getString(0)), // Id
                      cursor.getString(1), // First name
                      cursor.getString(2), // Last name
                      cursor.getString(3), // Title
                      Long.parseLong(cursor.getString(4)), // Create date
                      Long.parseLong(cursor.getString(5)) // Mod date
    );
  }

  /**
   * Method to check if a certain Author already exists in the database.
   *
   * @param author instance of author
   * @return true if author exists, otherwise false
   */
  public boolean existsAuthor(Author author) {
    Author dbAuthor = findByTitleAndFullName(author);

    if (dbAuthor == null) {
      return false;
    }

    author.setId(dbAuthor.getId());
    return true;
  }

  /**
   * Method to create an Author if it does not exist yet.
   *
   * @param authorList list of all authors
   */
  public void createOrUpdateAuthors(List<Author> authorList) {
    for (Author author : authorList) {
      if (existsAuthor(author)) {
        continue;
      }

      if (author.getId() != null
          && countAuthorBookLinks(author.getId()) == 1) {
        update(author);
      } else {
        create(author);
      }
    }
  }

  private int countAuthorBookLinks(Long authorId) {
    if (authorId == null || authorId == 0) {
      return 0;
    }

    SQLiteDatabase db = dbHelper.getReadableDatabase();

    String selectQuery = "SELECT count(" + DatabaseHelper.BOOK_ID + ") FROM "
        + DatabaseHelper.TABLE_NAME_AUTHOR_BOOK_LNK + " WHERE "
        + DatabaseHelper.AUTHOR_ID + " = ?";

    Cursor cursor = db.rawQuery(selectQuery, new String[] {authorId.toString()});

    try {
      if (!cursor.moveToFirst()) {
        return 0;
      }

      return cursor.getInt(0);
    } finally {
      cursor.close();
    }
  }

  // Checks if there is an entry of the author in the AUTHOR_BOOK_LNK table
  private boolean existsAuthorBookLink(Long authorId) {
    return countAuthorBookLinks(authorId) > 0;
  }

}
