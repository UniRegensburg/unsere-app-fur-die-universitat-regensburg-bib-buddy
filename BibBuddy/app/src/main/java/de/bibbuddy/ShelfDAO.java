package de.bibbuddy;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import java.util.ArrayList;
import java.util.List;

public class ShelfDAO implements IShelfDAO {

    private final DatabaseHelper dbHelper;

    public ShelfDAO(DatabaseHelper dbHelper) {
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

        Cursor cursor = db.query(DatabaseHelper.TABLE_NAME_SHELF, new String[]{DatabaseHelper._ID,
                        DatabaseHelper.NAME, DatabaseHelper.CREATE_DATE, DatabaseHelper.MOD_DATE, DatabaseHelper.SHELF_ID},
                DatabaseHelper._ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null, null);

        Shelf shelf = null;
        if (cursor != null) {
            cursor.moveToFirst();

            shelf = new Shelf(
                    Long.parseLong(cursor.getString(0)), // Id
                    cursor.getString(1), // Name
                    Integer.parseInt(cursor.getString(4)), // Create date
                    Integer.parseInt(cursor.getString(5)), // Mod date
                    Long.parseLong(cursor.getString(0)) // Shelf id
            );
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
                new String[]{String.valueOf(id)});

        db.close();
    }


}
