package de.bibbuddy;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import java.util.ArrayList;
import java.util.List;

public class AuthorDAO implements IAuthorDAO {

    private final DatabaseHelper dbHelper;

    public AuthorDAO(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    @Override
    public boolean create(Author author) {
        long currentTime = System.currentTimeMillis() / 1_000L;
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        try {
            ContentValues contentValues = new ContentValues();
            contentValues.put(DatabaseHelper.FIRST_NAME, author.getFirstName());
            contentValues.put(DatabaseHelper.LAST_NAME, author.getLastName());
            contentValues.put(DatabaseHelper.TITLE, author.getTitle());
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

    // get single author entry
    @Override
    public Author findById(Long id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(DatabaseHelper.TABLE_NAME_AUTHOR, new String[]{DatabaseHelper._ID,
                        DatabaseHelper.FIRST_NAME, DatabaseHelper.LAST_NAME, DatabaseHelper.TITLE,
                        DatabaseHelper.CREATE_DATE, DatabaseHelper.MOD_DATE},
                DatabaseHelper._ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null, null);


        Author author = null;
        if (cursor != null) {
            cursor.moveToFirst();

            author = new Author(
                    Long.parseLong(cursor.getString(0)), // Id
                    cursor.getString(1), // First name
                    cursor.getString(2), // Last name
                    cursor.getString(3), // Title
                    Integer.parseInt(cursor.getString(4)), // Create date
                    Integer.parseInt(cursor.getString(5)) // Mod date
            );
            cursor.close();
        }
        return author;
    }

    // get all authors in a list view
    @Override
    public List<Author> findAll() {
        List<Author> authorList = new ArrayList<Author>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + DatabaseHelper.TABLE_NAME_AUTHOR;

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Author author = new Author();

                author.setId(Long.parseLong(cursor.getString(0))); // Id
                author.setFirstName(cursor.getString(1)); // First name
                author.setLastName(cursor.getString(2)); // Last name
                author.setTitle(cursor.getString(3)); // Title
                author.setCreateDate(Integer.parseInt(cursor.getString(4))); // Create date
                author.setModDate(Integer.parseInt(cursor.getString(5))); // Mod date

                // Adding author to list
                authorList.add(author);
            } while (cursor.moveToNext());
        }

        cursor.close();

        return authorList;
    }


    // delete single author entry
    @Override
    public void delete(Long id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(DatabaseHelper.TABLE_NAME_AUTHOR, DatabaseHelper._ID + " = ?",
                new String[]{String.valueOf(id)});

        db.close();
    }
}