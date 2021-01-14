package de.bibbuddy;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import java.util.ArrayList;
import java.util.List;

public class NoteDAO implements INoteDAO {
    private final DatabaseHelper dbHelper;

    public NoteDAO(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    @Override
    public boolean create(Note note) {
        long currentTime = System.currentTimeMillis() / 1_000L;
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        try {
            ContentValues contentValues = new ContentValues();
            contentValues.put(DatabaseHelper.NAME, note.getName());
            contentValues.put(DatabaseHelper.TYPE, note.getType()); // LUT !?
            contentValues.put(DatabaseHelper.TEXT, note.getText());
            contentValues.put(DatabaseHelper.CREATE_DATE, currentTime);
            contentValues.put(DatabaseHelper.MOD_DATE, currentTime);
            contentValues.put(DatabaseHelper.NOTE_FILE_ID, note.getNoteFileId());

            long id = db.insert(DatabaseHelper.TABLE_NAME_AUTHOR, null, contentValues);

            note.setId(id);
        } catch (SQLiteException ex) {
            return false;
        } finally {
            db.close();
        }

        return true;
    }

    // get single note entry
    @Override
    public Note findById(long id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(DatabaseHelper.TABLE_NAME_NOTE, new String[]{DatabaseHelper._ID,
                        DatabaseHelper.NAME, DatabaseHelper.TYPE, DatabaseHelper.TEXT, DatabaseHelper.CREATE_DATE,
                        DatabaseHelper.MOD_DATE, DatabaseHelper.NOTE_FILE_ID}, DatabaseHelper._ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null, null);

        Note note = null;
        if (cursor != null) {
            cursor.moveToFirst();

            note = new Note(
                    Long.parseLong(cursor.getString(0)), // Id
                    cursor.getString(1), // Name
                    Integer.parseInt(cursor.getString(2)), // Type
                    cursor.getString(3), // Text
                    Integer.parseInt(cursor.getString(4)), // Create date
                    Integer.parseInt(cursor.getString(5)), // Mod date
                    Long.parseLong(cursor.getString(6)) // Note file id
            );
            cursor.close();
        }
        return note;
    }

    // get all notes in a list view
    @Override
    public List<Note> findAll() {
        List<Note> noteList = new ArrayList<Note>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + DatabaseHelper.TABLE_NAME_NOTE;

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Note note = new Note();

                note.setId(Long.parseLong(cursor.getString(0)));
                note.setName(cursor.getString(1));
                note.setType(Integer.parseInt(cursor.getString(2)));
                note.setText(cursor.getString(3));
                note.setCreateDate(Integer.parseInt(cursor.getString(4)));
                note.setModDate(Integer.parseInt(cursor.getString(5)));
                note.setNoteFileId(Long.parseLong(cursor.getString(6)));

                // Adding note to list
                noteList.add(note);
            } while (cursor.moveToNext());
            cursor.close();
        }


        return noteList;
    }


    // delete single note entry
    @Override
    public void delete(Long id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(DatabaseHelper.TABLE_NAME_NOTE, DatabaseHelper._ID + " = ?",
                new String[]{String.valueOf(id)});

        db.close();
    }

}
