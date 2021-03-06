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
 * ShelfDao contains all sql queries related to Shelf.
 *
 * @author Sarah Kurek, Claudia Schönherr
 */
public class ShelfDao {

  private static final String TAG = ShelfDao.class.getSimpleName();

  private final DatabaseHelper dbHelper;

  private String partSqlQuery(Long id) {
    if (id == null) {
      return " IS NULL";
    } else {
      return " = " + id;
    }
  }

  private Shelf createShelfData(Cursor cursor) {

    return new Shelf(Long.parseLong(cursor.getString(0)), // Id
                     cursor.getString(1), // Name
                     Long.parseLong(cursor.getString(2)), // Create date
                     Long.parseLong(cursor.getString(3)), // Mod date
                     null // parent shelf id is deprecated
    );
  }

  public ShelfDao(DatabaseHelper dbHelper) {
    this.dbHelper = dbHelper;
  }

  /**
   * Save a shelf in the database.
   *
   * @param shelf that should be saved.
   */
  public void create(Shelf shelf) {
    Long currentTime = new Date().getTime();

    try (SQLiteDatabase db = dbHelper.getWritableDatabase()) {
      ContentValues contentValues = new ContentValues();
      contentValues.put(DatabaseHelper.NAME, shelf.getName());
      contentValues.put(DatabaseHelper.CREATE_DATE, currentTime);
      contentValues.put(DatabaseHelper.MOD_DATE, currentTime);
      contentValues.put(DatabaseHelper.SHELF_ID, shelf.getShelfId());

      Long id = db.insert(DatabaseHelper.TABLE_NAME_SHELF, null, contentValues);
      shelf.setId(id);

    } catch (SQLiteException ex) {
      Log.e(TAG, ex.toString(), ex);

    }

  }

  public Shelf findById(long id) {
    SQLiteDatabase db = dbHelper.getReadableDatabase();

    Cursor cursor = db.query(DatabaseHelper.TABLE_NAME_SHELF,
                             new String[] {DatabaseHelper._ID, DatabaseHelper.NAME,
                                 DatabaseHelper.CREATE_DATE, DatabaseHelper.MOD_DATE,
                                 DatabaseHelper.SHELF_ID},
                             DatabaseHelper._ID + " = ?", new String[] {String.valueOf(id)},
                             null, null, null, String.valueOf(1));

    Shelf shelf = null;
    if (cursor.moveToFirst()) {
      do {
        shelf = createShelfData(cursor);

        if (cursor.getString(4) == null) { // without it an error occurs
          shelf.setShelfId(null);
        } else {
          shelf.setShelfId(Long.parseLong(cursor.getString(4))); // Shelf id
        }
      } while (cursor.moveToNext());
    }

    cursor.close();

    return shelf;
  }

  // Deletes single shelf entry
  public void delete(Long id) {
    SQLiteDatabase db = dbHelper.getWritableDatabase();
    db.delete(DatabaseHelper.TABLE_NAME_SHELF, DatabaseHelper._ID + " = ?",
              new String[] {String.valueOf(id)});

    db.close();
  }

  /**
   * Finds all sub-shelves of a certain shelf with the parentId.
   *
   * @param id current shelfId
   * @return list with all sub-shelves for the current shelf
   */
  public List<Shelf> findAllByParentId(Long id) {
    SQLiteDatabase db = dbHelper.getWritableDatabase();

    String selectQuery = "SELECT * FROM " + DatabaseHelper.TABLE_NAME_SHELF
        + " WHERE " + DatabaseHelper.SHELF_ID + partSqlQuery(id);

    Cursor cursor = db.rawQuery(selectQuery, null);

    List<Shelf> shelfList = new ArrayList<>();
    if (cursor.moveToFirst()) {
      do {
        Shelf shelf = createShelfData(cursor);

        if (id == null) {
          shelf.setShelfId(null);
        } else {
          shelf.setShelfId(Long.parseLong(cursor.getString(4)));
        }

        shelfList.add(shelf);
      } while (cursor.moveToNext());
    }

    cursor.close();

    return shelfList;
  }

  /**
   * Finds the last added shelfId.
   *
   * @return last added shelfId
   */
  public Long findLatestId() {
    SQLiteDatabase db = dbHelper.getReadableDatabase();

    String selectQuery = "SELECT " + DatabaseHelper._ID + " FROM "
        + DatabaseHelper.TABLE_NAME_SHELF + " ORDER BY " + DatabaseHelper._ID + " DESC LIMIT 1";

    Cursor cursor = db.rawQuery(selectQuery, null);

    Long id = null;
    if (cursor.moveToFirst()) {
      do {
        id = cursor.getLong(0); // Id
      } while (cursor.moveToNext());
    }

    cursor.close();

    return id;
  }

  /**
   * Counts all Notes for a certain Shelf.
   *
   * @param shelfBookIds list with all bookIds for current shelf
   * @return count of all books for current shelf
   */
  public int countAllNotesForShelf(List<Long> shelfBookIds) {
    SQLiteDatabase db = dbHelper.getReadableDatabase();

    int noteCount = 0;
    for (Long bookId : shelfBookIds) {
      String selectQuery = "SELECT COUNT(" + DatabaseHelper._ID + ") FROM "
          + DatabaseHelper.TABLE_NAME_BOOK_NOTE_LNK + " WHERE "
          + DatabaseHelper.BOOK_ID + " = ?";

      Cursor cursor = db.rawQuery(selectQuery, new String[] {String.valueOf(bookId)});

      if (cursor.moveToFirst()) {
        do {
          noteCount += Integer.parseInt(cursor.getString(0));
        } while (cursor.moveToNext());
      }

      cursor.close();
    }

    return noteCount;
  }

  /**
   * Counts all Books for a certain Shelf.
   *
   * @param shelfId current shelfId
   * @return count of all books for the current shelf
   */
  public int countAllBooksForShelf(Long shelfId) {
    SQLiteDatabase db = dbHelper.getReadableDatabase();

    String selectQuery = "SELECT COUNT(" + DatabaseHelper._ID + ") FROM "
        + DatabaseHelper.TABLE_NAME_SHELF_BOOK_LNK + " WHERE "
        + DatabaseHelper.SHELF_ID + " = ?";

    Cursor cursor = db.rawQuery(selectQuery, new String[] {String.valueOf(shelfId)});

    int bookCount = 0;
    if (cursor.moveToFirst()) {
      do {
        bookCount = Integer.parseInt(cursor.getString(0));
      } while (cursor.moveToNext());
    }

    cursor.close();

    return bookCount;
  }

  /**
   * Renames the shelf in the database and updates the modified date.
   *
   * @param id        id of the shelf
   * @param shelfName new name of the shelf
   */
  public void renameShelf(Long id, String shelfName) {
    ContentValues values = new ContentValues();

    values.put(DatabaseHelper.NAME, shelfName);
    values.put(DatabaseHelper.MOD_DATE, new Date().getTime());

    dbHelper.getWritableDatabase().update(DatabaseHelper.TABLE_NAME_SHELF, values,
                                          DatabaseHelper._ID + " = ?",
                                          new String[] {String.valueOf(id)});
    SQLiteDatabase db = dbHelper.getWritableDatabase();
    db.close();
  }

  /**
   * Finds all shelf names which contain the searchInput.
   *
   * @param searchInput searchInput of the user
   * @return a list of shelves which have the searchInput in the name
   */
  public List<Shelf> findShelvesByName(String searchInput) {
    SQLiteDatabase db = dbHelper.getReadableDatabase();

    String selectQuery = "SELECT * FROM " + DatabaseHelper.TABLE_NAME_SHELF + " WHERE "
        + DatabaseHelper.NAME + " LIKE '%" + searchInput + "%'";

    Cursor cursor = db.rawQuery(selectQuery, null);

    List<Shelf> shelfList = new ArrayList<>();
    if (cursor.moveToFirst()) {
      do {
        Shelf shelf = createShelfData(cursor);
        shelfList.add(shelf);

      } while (cursor.moveToNext());
    }

    cursor.close();

    return shelfList;
  }

}
