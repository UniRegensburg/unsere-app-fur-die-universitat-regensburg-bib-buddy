package de.bibbuddy;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import java.util.ArrayList;
import java.util.List;

public class ShelfDao implements IShelfDAO {

  private final DatabaseHelper dbHelper;

  public ShelfDao(DatabaseHelper dbHelper) {
    this.dbHelper = dbHelper;
  }

  @Override
  public boolean create(Shelf shelf) {
    long currentTime = System.currentTimeMillis() / 1_000L;
    SQLiteDatabase db = dbHelper.getWritableDatabase();

    try {
      ContentValues contentValues = new ContentValues();
      contentValues.put(DatabaseHelper.NAME, shelf.getName());
      contentValues.put(DatabaseHelper.CREATE_DATE, currentTime);
      contentValues.put(DatabaseHelper.MOD_DATE, currentTime);
      contentValues.put(DatabaseHelper.SHELF_ID, shelf.getShelfId());

      long id = db.insert(DatabaseHelper.TABLE_NAME_SHELF, null, contentValues);

      shelf.setId(id);

    } catch (SQLiteException ex) {
      return false;
    } finally {
      db.close();
    }

    return true;
  }

  @Override
  public Shelf findById(long id) {
    SQLiteDatabase db = dbHelper.getReadableDatabase();

    Cursor cursor = db.query(DatabaseHelper.TABLE_NAME_SHELF, new String[] {DatabaseHelper._ID,
        DatabaseHelper.NAME, DatabaseHelper.CREATE_DATE, DatabaseHelper.MOD_DATE,
        DatabaseHelper.SHELF_ID},
        DatabaseHelper._ID + "=?", new String[] {String.valueOf(id)},
        null, null, null, null);

    Shelf shelf = null;
    if (cursor != null) {
      cursor.moveToFirst();

      shelf = new Shelf();

      shelf.setId(Long.parseLong(cursor.getString(0))); // Id
      shelf.setName(cursor.getString(1)); // Name
      shelf.setCreateDate(Integer.parseInt(cursor.getString(2))); // Create date
      shelf.setModDate(Integer.parseInt(cursor.getString(3))); // Mod date

      if (cursor.getString(4) == null) { // without it an error occurs
        shelf.setShelfId(null);
      } else {
        shelf.setShelfId(Long.parseLong(cursor.getString(4))); // Shelf id
      }

      cursor.close();
    }
    return shelf;
  }


  // get all shelves in a list view
  @Override
  public List<Shelf> findAll() {
    List<Shelf> shelfList = new ArrayList<Shelf>();
    // Select All Query
    String selectQuery = "SELECT  * FROM " + DatabaseHelper.TABLE_NAME_SHELF;

    SQLiteDatabase db = dbHelper.getWritableDatabase();
    Cursor cursor = db.rawQuery(selectQuery, null);

    // looping through all rows and adding to list
    if (cursor.moveToFirst()) {
      do {
        Shelf shelf = new Shelf();

        shelf.setId(Long.parseLong(cursor.getString(0)));
        shelf.setName(cursor.getString(1));
        shelf.setCreateDate(Integer.parseInt(cursor.getString(2)));
        shelf.setModDate(Integer.parseInt(cursor.getString(3)));
        shelf.setShelfId(Long.parseLong(cursor.getString(4)));

        // Adding shelf to list
        shelfList.add(shelf);
      } while (cursor.moveToNext());
      cursor.close();
    }

    return shelfList;
  }


  // delete single shelf entry
  @Override
  public void delete(Long id) {
    SQLiteDatabase db = dbHelper.getWritableDatabase();
    db.delete(DatabaseHelper.TABLE_NAME_SHELF, DatabaseHelper._ID + " = ?",
        new String[] {String.valueOf(id)});

    db.close();
  }

  private String partSqlQuery(Long id) {
    if (id == null) {
      return " IS NULL";
    } else {
      return " = " + id;
    }
  }

  /**
   * DUMMY COMMENT - PLEASE ADJUST.
   *
   * @param id test
   * @return test
   */
  public List<Shelf> findAllByParentId(Long id) {
    // Find all shelves with the given parent id
    List<Shelf> shelfList = new ArrayList<Shelf>();
    String selectQuery = "SELECT * FROM " + DatabaseHelper.TABLE_NAME_SHELF
        + " WHERE " + DatabaseHelper.SHELF_ID + partSqlQuery(id);

    SQLiteDatabase db = dbHelper.getWritableDatabase();
    Cursor cursor = db.rawQuery(selectQuery, null);

    // looping through all rows and adding to list
    if (cursor.moveToFirst()) {
      do {
        Shelf shelf = new Shelf();

        shelf.setId(Long.parseLong(cursor.getString(0)));
        shelf.setName(cursor.getString(1));
        shelf.setCreateDate(Integer.parseInt(cursor.getString(2)));
        shelf.setModDate(Integer.parseInt(cursor.getString(3)));

        if (id == null) {
          shelf.setShelfId(null);
        } else {
          shelf.setShelfId(Long.parseLong(cursor.getString(4)));
        }
        // Adding shelf to list
        shelfList.add(shelf);
      } while (cursor.moveToNext());
      cursor.close();
    }

    return shelfList;
  }

  /**
   * DUMMY COMMENT - PLEASE ADJUST.
   *
   * @return test
   */
  public Long findLatestId() {
    SQLiteDatabase db = dbHelper.getReadableDatabase();
    String selectQuery = "SELECT " + DatabaseHelper._ID + " FROM "
        + DatabaseHelper.TABLE_NAME_SHELF + " ORDER BY " + DatabaseHelper._ID + " DESC LIMIT 1";

    Cursor cursor = db.rawQuery(selectQuery, null);

    Long id = null;
    if (cursor != null) {
      cursor.moveToFirst();
      id = cursor.getLong(0); // Id
      cursor.close();
    }

    return id;
  }

  /**
   * DUMMY COMMENT - PLEASE ADJUST.
   *
   * @param shelfBookIds test
   * @return test
   */
  public int countAllNotesForShelf(List<Long> shelfBookIds) {
    SQLiteDatabase db = dbHelper.getReadableDatabase();
    int noteCount = 0;

    for (Long bookId : shelfBookIds) {
      String selectQuery = "SELECT COUNT(" + DatabaseHelper._ID + ") FROM "
          + DatabaseHelper.TABLE_NAME_BOOK_NOTE_LNK + " WHERE "
          + DatabaseHelper.BOOK_ID + "=" + bookId;

      Cursor cursor = db.rawQuery(selectQuery, null);

      if (cursor.moveToFirst()) {
        do {
          noteCount += Integer.parseInt(cursor.getString(0));
        } while (cursor.moveToNext());
        cursor.close();
      }
    }

    return noteCount;
  }

  /**
   * DUMMY COMMENT - PLEASE ADJUST.
   *
   * @param shelfId test
   * @return test
   */
  public int countAllBooksForShelf(Long shelfId) {
    SQLiteDatabase db = dbHelper.getReadableDatabase();
    int bookCount = 0;

    String selectQuery = "SELECT COUNT(" + DatabaseHelper._ID + ") FROM "
        + DatabaseHelper.TABLE_NAME_SHELF_BOOK_LNK + " WHERE "
        + DatabaseHelper.SHELF_ID + "=" + shelfId;

    Cursor cursor = db.rawQuery(selectQuery, null);

    if (cursor.moveToFirst()) {
      do {
        bookCount = Integer.parseInt(cursor.getString(0));
      } while (cursor.moveToNext());
      cursor.close();
    }

    return bookCount;
  }

}
