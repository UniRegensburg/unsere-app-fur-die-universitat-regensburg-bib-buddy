package de.bibbuddy;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * NoteDao contains all sql queries related to Note.
 *
 * @author Sarah Kurek, Claudia Schönherr
 */
public class NoteDao implements InterfaceNoteDao {

  private final DatabaseHelper dbHelper;

  public NoteDao(DatabaseHelper dbHelper) {
    this.dbHelper = dbHelper;
  }

  @Override
  public boolean create(Note note) {
    Long currentTime = new Date().getTime();
    try (SQLiteDatabase db = dbHelper.getWritableDatabase()) {
      try {
        ContentValues noteFile = new ContentValues();
        noteFile.put(DatabaseHelper.FILE, "");
        db.insert(DatabaseHelper.TABLE_NAME_NOTE_FILE, null, noteFile);
        Cursor c =
            db.query(DatabaseHelper.TABLE_NAME_NOTE_FILE, null, null,
                null, null, null, null);
        c.moveToLast();

        ContentValues contentValues = new ContentValues();
        contentValues.put(DatabaseHelper.NAME, note.getName());
        contentValues.put(DatabaseHelper.TYPE, note.getType()); // LUT !?
        contentValues.put(DatabaseHelper.TEXT, note.getText());
        contentValues.put(DatabaseHelper.CREATE_DATE, currentTime);
        contentValues.put(DatabaseHelper.MOD_DATE, currentTime);
        contentValues.put(DatabaseHelper.NOTE_FILE_ID, c.getLong(0));

        c.close();
        db.insert(DatabaseHelper.TABLE_NAME_NOTE, null, contentValues);

        Cursor cursor =
            db.query(DatabaseHelper.TABLE_NAME_NOTE, null, null, null,
                null, null, null);
        cursor.moveToLast();
        long id = cursor.getLong(0);
        cursor.close();
        note.setId(id);
        note.setCreateDate(currentTime);
        note.setModDate(currentTime);
      } catch (SQLiteException ex) {
        return false;
      } finally {
        db.close();
      }
    }
    return true;
  }

  // get single note entry
  @Override
  public Note findById(long id) {
    SQLiteDatabase db = dbHelper.getReadableDatabase();

    Cursor cursor = db.query(DatabaseHelper.TABLE_NAME_NOTE, new String[] {DatabaseHelper._ID,
        DatabaseHelper.NAME, DatabaseHelper.TYPE, DatabaseHelper.TEXT,
        DatabaseHelper.CREATE_DATE, DatabaseHelper.MOD_DATE, DatabaseHelper.NOTE_FILE_ID},
        DatabaseHelper._ID + "=?", new String[] {String.valueOf(id)},
        null, null, null, null);

    Note note = null;
    if (cursor != null) {
      cursor.moveToFirst();
      note = new Note(
          Long.parseLong(cursor.getString(0)), // Id
          cursor.getString(1), // Name
          Integer.parseInt(cursor.getString(2)), // Type
          cursor.getString(3), // Text
          Long.parseLong(cursor.getString(4)), // Create date
          Long.parseLong(cursor.getString(5)), // Mod date
          cursor.getLong(6) // Note file id
      );
      cursor.close();
    }
    return note;
  }

  // get all notes in a list view
  @Override
  public List<Note> findAll() {
    List<Note> noteList = new ArrayList<>();
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
        note.setCreateDate(Long.parseLong(cursor.getString(4)));
        note.setModDate(Long.parseLong(cursor.getString(5)));
        note.setNoteFileId(cursor.getLong(6));

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
        new String[] {String.valueOf(id)});

    db.delete(DatabaseHelper.TABLE_NAME_BOOK_NOTE_LNK, DatabaseHelper.NOTE_ID + " = ?",
              new String[] {String.valueOf(id)});

    db.close();
  }

  //Update a single note selected by given id

  /**
   * This method updates a note object within database selected by its id.
   *
   * @param id   note id
   * @param name note name
   * @param text note text if it is of type text
   */
  public void updateNote(Long id, String name, String text) {
    Long currentTime = new Date().getTime();
    ContentValues values = new ContentValues();
    values.put("name", name);
    values.put("text", text);
    values.put("modifikation_date", currentTime);
    dbHelper.getWritableDatabase().update(DatabaseHelper.TABLE_NAME_NOTE, values,
        DatabaseHelper._ID + " = ?", new String[] {String.valueOf(id)});
    SQLiteDatabase db = dbHelper.getWritableDatabase();
    db.close();
  }

  /**
   * This method links a note with a book.
   *
   * @param bookId id of the book to link
   * @param noteId id of the note to link
   * @return if linking was successfull
   */
  public boolean linkNoteWithBook(Long bookId, Long noteId) {

    try (SQLiteDatabase db = dbHelper.getWritableDatabase()) {
      ContentValues contentValues = new ContentValues();
      contentValues.put(DatabaseHelper.BOOK_ID, bookId);
      contentValues.put(DatabaseHelper.NOTE_ID, noteId);

      db.insert(DatabaseHelper.TABLE_NAME_BOOK_NOTE_LNK, null, contentValues);

    } catch (SQLiteException ex) {
      return false;
    }

    return true;
  }

  /**
   * Gets all notes of a book by the bookId.
   *
   * @param bookId id of the book
   * @return a list of all noteIds of a book
   */
  public List<Long> getAllNoteIdsForBook(Long bookId) {
    SQLiteDatabase db = dbHelper.getReadableDatabase();
    List<Long> noteIds = new ArrayList<Long>();
    String selectQuery = "SELECT  * FROM " + DatabaseHelper.TABLE_NAME_BOOK_NOTE_LNK + " WHERE "
        + DatabaseHelper.BOOK_ID + "=" + bookId;
    Cursor cursor = db.rawQuery(selectQuery, null);
    if (cursor.moveToFirst()) {
      do {
        noteIds.add(Long.parseLong(cursor.getString(2)));
      } while (cursor.moveToNext());
      cursor.close();
    }

    return noteIds;
  }


  // get all Notes for a book with a list of noteIds

  /**
   * This method gets a list of all notes that are connected to a specific book.
   *
   * @param bookId id of the book that the result notes must be connected to
   * @return returns a list of connected note-objects.
   */
  public List<Note> getAllNotesForBook(Long bookId) {
    List<Long> noteIds = getAllNoteIdsForBook(bookId);
    List<Note> noteList = new ArrayList<>();
    for (Long id : noteIds) {
      noteList.add(findById(id));
    }
    return noteList;
  }

  /**
   * This method gets text string of a specific note.
   *
   * @param id id of the note to look for
   * @return returns the notes text value
   */
  public String findTextById(Long id) {
    SQLiteDatabase db = dbHelper.getReadableDatabase();

    Cursor cursor = db.query(DatabaseHelper.TABLE_NAME_NOTE, new String[] {DatabaseHelper._ID,
        DatabaseHelper.NAME, DatabaseHelper.TYPE, DatabaseHelper.TEXT,
        DatabaseHelper.CREATE_DATE, DatabaseHelper.MOD_DATE, DatabaseHelper.NOTE_FILE_ID},
        DatabaseHelper._ID + "=?", new String[] {String.valueOf(id)},
        null, null, null, null);

    String noteText = null;
    if (cursor != null) {
      cursor.moveToFirst();
      noteText = cursor.getString(3);

      cursor.close();
    }

    return noteText;
  }
}
