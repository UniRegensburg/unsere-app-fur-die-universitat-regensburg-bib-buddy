package de.bibbuddy;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * AuthorDao includes all sql queries related to Author.
 *
 * @author Sarah Kurek, Claudia Schönherr, Silvia Ivanova
 */
public class AuthorDao {

  private static final String TAG = AuthorDao.class.getSimpleName();

  private final DatabaseHelper dbHelper;

  private static boolean isNullOrEmpty(String text) {
    return text == null || text.isEmpty();
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


  private int countAuthorBookLinks(Long authorId) {
    if (authorId == null || authorId == 0) {
      return 0;
    }

    SQLiteDatabase db = dbHelper.getReadableDatabase();

    String selectQuery = "SELECT count(" + DatabaseHelper.BOOK_ID + ") FROM "
        + DatabaseHelper.TABLE_NAME_AUTHOR_BOOK_LNK + " WHERE "
        + DatabaseHelper.AUTHOR_ID + " = ?";

    try (Cursor cursor = db.rawQuery(selectQuery, new String[] {authorId.toString()})) {
      if (!cursor.moveToFirst()) {
        return 0;
      }

      return cursor.getInt(0);
    } catch (SQLiteException ex) {
      Log.e(TAG, ex.toString(), ex);
      return 0;
    }
  }

  // Checks if there is an entry of the author in the AUTHOR_BOOK_LNK table
  private boolean existsAuthorBookLink(Long authorId) {
    return countAuthorBookLinks(authorId) > 0;
  }

  public AuthorDao(DatabaseHelper dbHelper) {
    this.dbHelper = dbHelper;
  }

  public void create(Author author) {
    Long currentTime = new Date().getTime();

    try (SQLiteDatabase db = dbHelper.getWritableDatabase()) {
      ContentValues contentValues = new ContentValues();
      contentValues.put(DatabaseHelper.FIRST_NAME, author.getFirstName());
      contentValues.put(DatabaseHelper.LAST_NAME, author.getLastName());

      if (!isNullOrEmpty(author.getTitle())) {
        contentValues.put(DatabaseHelper.TITLE, author.getTitle());
      }

      contentValues.put(DatabaseHelper.CREATE_DATE, currentTime);
      contentValues.put(DatabaseHelper.MOD_DATE, currentTime);

      Long id = db.insert(DatabaseHelper.TABLE_NAME_AUTHOR, null, contentValues);
      author.setId(id);

    } catch (SQLiteException ex) {
      Log.e(TAG, ex.toString(), ex);

    }

  }

  /**
   * Updates an existing author.
   *
   * @param author author object
   */
  public void update(Author author) {
    Long currentTime = new Date().getTime();

    try (SQLiteDatabase db = dbHelper.getWritableDatabase()) {
      ContentValues contentValues = new ContentValues();
      contentValues.put(DatabaseHelper.FIRST_NAME, author.getFirstName());
      contentValues.put(DatabaseHelper.LAST_NAME, author.getLastName());
      contentValues.put(DatabaseHelper.TITLE, isNullOrEmpty(
          author.getTitle()) ? null : author.getTitle());
      contentValues.put(DatabaseHelper.MOD_DATE, currentTime);

      db.update(DatabaseHelper.TABLE_NAME_AUTHOR, contentValues,
                DatabaseHelper._ID + " = ?",
                new String[] {String.valueOf(author.getId())});

    } catch (SQLiteException ex) {
      Log.e(TAG, ex.toString(), ex);
    }

  }

  // Gets single author entry
  public Author findById(Long id) {
    SQLiteDatabase db = dbHelper.getReadableDatabase();

    Cursor cursor = db.query(DatabaseHelper.TABLE_NAME_AUTHOR,
                             new String[] {DatabaseHelper._ID,
                                 DatabaseHelper.FIRST_NAME, DatabaseHelper.LAST_NAME,
                                 DatabaseHelper.TITLE,
                                 DatabaseHelper.CREATE_DATE, DatabaseHelper.MOD_DATE},
                             DatabaseHelper._ID + " = ?",
                             new String[] {String.valueOf(id)}, null, null, null, null);

    Author author = null;
    if (cursor.moveToFirst()) {
      author = createAuthorData(cursor);
    }

    cursor.close();

    return author;
  }

  /**
   * Finds an existing author by its title, first and last name.
   *
   * @param authorToFind the author containing the data to search for
   * @return if found, the author (with its database ID), else null
   */
  public Author findByTitleAndFullName(Author authorToFind) {
    List<String> params = new ArrayList<>();
    StringBuilder stringBuilder = new StringBuilder();

    stringBuilder.append(DatabaseHelper.FIRST_NAME + " = ?");
    params.add(authorToFind.getFirstName());

    stringBuilder.append(" AND " + DatabaseHelper.LAST_NAME + " = ?");
    params.add(authorToFind.getLastName());

    stringBuilder.append(" AND " + DatabaseHelper.TITLE);
    if (isNullOrEmpty(authorToFind.getTitle())) {
      stringBuilder.append(" IS NULL");
    } else {
      stringBuilder.append(" = ?");
      params.add(authorToFind.getTitle());
    }

    String selection = stringBuilder.toString();
    SQLiteDatabase db = dbHelper.getReadableDatabase();

    //noinspection ToArrayCallWithZeroLengthArrayArgument
    try (Cursor cursor = db.query(DatabaseHelper.TABLE_NAME_AUTHOR,
                                  new String[] {DatabaseHelper._ID,
                                      DatabaseHelper.FIRST_NAME,
                                      DatabaseHelper.LAST_NAME,
                                      DatabaseHelper.TITLE,
                                      DatabaseHelper.MOD_DATE,
                                      DatabaseHelper.CREATE_DATE},
                                  selection,
                                  params.toArray(new String[params.size()]),
                                  null, null, null, null)) {
      if (!cursor.moveToFirst()) {
        return null;
      }

      return createAuthorData(cursor);
    }
  }

  /**
   * Deletes the relevant author entries.
   *
   * @param authorId id of the author
   * @param bookId   id of the book
   */
  public void delete(Long authorId, Long bookId) {
    SQLiteDatabase db = dbHelper.getWritableDatabase();

    db.delete(DatabaseHelper.TABLE_NAME_AUTHOR_BOOK_LNK, DatabaseHelper.AUTHOR_ID
                  + " = ?" + " AND " + DatabaseHelper.BOOK_ID + " = ?",
              new String[] {String.valueOf(authorId), String.valueOf(bookId)});

    // Deletes author only if author has no link to another book
    if (!existsAuthorBookLink(authorId)) {
      db.delete(DatabaseHelper.TABLE_NAME_AUTHOR, DatabaseHelper._ID + " = ?",
                new String[] {String.valueOf(authorId)});
    }

    db.close();
  }


  /**
   * Checks if a certain Author already exists in the database.
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
   * Creates an Author if it does not exist yet.
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

}
