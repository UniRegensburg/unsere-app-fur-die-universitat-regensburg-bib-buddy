package de.bibbuddy;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import java.util.ArrayList;
import java.util.List;

/**
 * TagDao contains all sql queries related to Tag.
 *
 * @author Sarah Kurek
 */
public class TagDao implements InterfaceTagDao {

  private final DatabaseHelper dbHelper;

  public TagDao(DatabaseHelper dbHelper) {
    this.dbHelper = dbHelper;
  }

  @Override
  public boolean create(Tag tag) {
    SQLiteDatabase db = dbHelper.getWritableDatabase();

    try {
      ContentValues contentValues = new ContentValues();
      contentValues.put(DatabaseHelper.NAME, tag.getName());

      long id = db.insert(DatabaseHelper.TABLE_NAME_TAG, null, contentValues);

      tag.setId(id);
    } catch (
    SQLiteException ex) {
      return false;
    } finally {
      db.close();
    }

    return true;
  }


  // get single tag entry
  @Override
  public Tag findById(Long id) {
    SQLiteDatabase db = dbHelper.getReadableDatabase();

    Cursor cursor = db.query(DatabaseHelper.TABLE_NAME_TAG, new String[] {DatabaseHelper._ID,
        DatabaseHelper.NAME}, DatabaseHelper._ID + "=?",
        new String[] {String.valueOf(id)}, null, null, null, null);

    Tag tag = null;
    if (cursor != null) {
      cursor.moveToFirst();

      tag = new Tag(
          Long.parseLong(cursor.getString(0)), // Id
          cursor.getString(1) // Name
      );

      cursor.close();
    }
    return tag;
  }


  // get all tags in a list view
  @Override
  public List<Tag> findAll() {
    List<Tag> tagList = new ArrayList<Tag>();
    // Select All Query
    String selectQuery = "SELECT  * FROM " + DatabaseHelper.TABLE_NAME_TAG;

    SQLiteDatabase db = dbHelper.getWritableDatabase();
    Cursor cursor = db.rawQuery(selectQuery, null);

    if (cursor.moveToFirst()) {
      do {
        Tag tag = new Tag();

        tag.setId(Long.parseLong(cursor.getString(0))); // Id
        tag.setName(cursor.getString(1)); // Name

        tagList.add(tag);
      } while (cursor.moveToNext());
      cursor.close();
    }


    return tagList;
  }

  // delete single tag entry
  @Override
  public void delete(Long id) {
    SQLiteDatabase db = dbHelper.getWritableDatabase();
    db.delete(DatabaseHelper.TABLE_NAME_TAG, DatabaseHelper._ID + " = ?",
        new String[] {String.valueOf(id)});

    db.close();
  }
}
