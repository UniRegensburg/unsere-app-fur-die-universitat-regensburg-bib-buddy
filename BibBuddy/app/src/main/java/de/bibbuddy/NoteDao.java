package de.bibbuddy;

import static android.text.Html.escapeHtml;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * NoteDao contains all sql queries related to Note.
 *
 * @author Sarah Kurek, Claudia Schönherr, Luis Moßburger, Silvia Ivanova
 */
public class NoteDao {

  private static final String TAG = NoteDao.class.getSimpleName();

  private final DatabaseHelper dbHelper;

  private Note createNoteData(Cursor cursor) {

    return new Note(
        Long.parseLong(cursor.getString(0)), // Id
        cursor.getString(1), // Name
        NoteTypeLut.valueOf(Integer.parseInt(cursor.getString(2))), // Type
        cursor.getString(3), // Text
        Long.parseLong(cursor.getString(4)), // Create date
        Long.parseLong(cursor.getString(5)), // Mod date
        cursor.getLong(6) // Note file id
    );
  }

  private void updateBookModified(Long bookId) {

    try (SQLiteDatabase db = dbHelper.getWritableDatabase()) {

      ContentValues contentValues = new ContentValues();
      contentValues.put(DatabaseHelper.MOD_DATE, new Date().getTime());

      db.update(DatabaseHelper.TABLE_NAME_BOOK, contentValues,
                DatabaseHelper._ID + " = ?",
                new String[] {String.valueOf(bookId)});

    } catch (SQLiteException ex) {
      Log.e(TAG, ex.toString(), ex);
    }

  }

  public NoteDao(DatabaseHelper dbHelper) {
    this.dbHelper = dbHelper;
  }

  /*
  * Creates a new Note.
  *
  * @param note the note that should be created
  * */
  public void create(Note note) {
    Long currentTime = new Date().getTime();
    try (SQLiteDatabase db = dbHelper.getWritableDatabase()) {
      try {
        ContentValues noteFileValues = new ContentValues();
        if (note.getNoteFilePath() == null) {
          noteFileValues.put(DatabaseHelper.FILE, "");
        } else {
          noteFileValues.put(DatabaseHelper.FILE, note.getNoteFilePath());
        }

        db.insert(DatabaseHelper.TABLE_NAME_NOTE_FILE, null, noteFileValues);
        Cursor c =
            db.query(DatabaseHelper.TABLE_NAME_NOTE_FILE, null, null,
                     null, null, null, null);
        c.moveToLast();

        ContentValues noteValues = new ContentValues();
        noteValues.put(DatabaseHelper.NAME, note.getName());
        noteValues.put(DatabaseHelper.TYPE, note.getType().getId());
        noteValues.put(DatabaseHelper.TEXT, note.getText());
        noteValues.put(DatabaseHelper.CREATE_DATE, currentTime);
        noteValues.put(DatabaseHelper.MOD_DATE, currentTime);
        noteValues.put(DatabaseHelper.NOTE_FILE_ID, c.getLong(0));

        c.close();

        db.insert(DatabaseHelper.TABLE_NAME_NOTE, null, noteValues);

        Cursor cursor =
            db.query(DatabaseHelper.TABLE_NAME_NOTE, null, null, null,
                     null, null, null);
        cursor.moveToLast();

        cursor.close();

      } catch (SQLiteException ex) {
        Log.e(TAG, ex.toString(), ex);

      } finally {
        db.close();
      }
    }
  }

  // Gets single note entry
  public Note findById(long id) {
    SQLiteDatabase db = dbHelper.getReadableDatabase();

    Cursor cursor = db.query(DatabaseHelper.TABLE_NAME_NOTE,
                             new String[] {DatabaseHelper._ID, DatabaseHelper.NAME,
                                 DatabaseHelper.TYPE, DatabaseHelper.TEXT,
                                 DatabaseHelper.CREATE_DATE, DatabaseHelper.MOD_DATE,
                                 DatabaseHelper.NOTE_FILE_ID},
                             DatabaseHelper._ID + " = ?", new String[] {String.valueOf(id)},
                             null, null, null, String.valueOf(1));

    Note note = null;
    if (cursor.moveToFirst()) {
      do {
        note = createNoteData(cursor);
      } while (cursor.moveToNext());
    }

    cursor.close();

    return note;
  }

  /**
   * Fetches the path-string of a note-file linked to a note.
   *
   * @param noteFileId id of the note-file linked to the note
   * @return returns a string representing the path to the saved note media
   */
  public String getNoteFilePath(long noteFileId) {
    SQLiteDatabase db = dbHelper.getReadableDatabase();

    Cursor cursor = db.query(DatabaseHelper.TABLE_NAME_NOTE_FILE,
                             new String[] {DatabaseHelper._ID,
                                 DatabaseHelper.FILE},
                             DatabaseHelper._ID + " = ?", new String[] {String.valueOf(noteFileId)},
                             null, null, null, String.valueOf(1));

    String path = "";
    if (cursor.moveToFirst()) {
      path = cursor.getString(1);
    }

    cursor.close();

    return path;
  }

  // Gets all notes in a list view
  public List<Note> findAll() {
    SQLiteDatabase db = dbHelper.getWritableDatabase();

    String selectQuery = "SELECT * FROM " + DatabaseHelper.TABLE_NAME_NOTE;

    Cursor cursor = db.rawQuery(selectQuery, null);

    List<Note> noteList = new ArrayList<>();
    if (cursor.moveToFirst()) {
      do {
        Note note = createNoteData(cursor);
        noteList.add(note);
      } while (cursor.moveToNext());
    }

    cursor.close();

    return noteList;
  }

  // Deletes single note entry
  public void delete(Long id) {
    SQLiteDatabase db = dbHelper.getWritableDatabase();
    db.delete(DatabaseHelper.TABLE_NAME_NOTE, DatabaseHelper._ID + " = ?",
              new String[] {String.valueOf(id)});

    db.delete(DatabaseHelper.TABLE_NAME_BOOK_NOTE_LNK, DatabaseHelper.NOTE_ID + " = ?",
              new String[] {String.valueOf(id)});

    db.close();
  }

  /**
   * Updates a note object within database selected by its id.
   *
   * @param id   note id
   * @param name note name
   * @param text note text if it is of type text
   */
  public void updateNote(Long id, String name, String text) {
    Long currentTime = new Date().getTime();
    ContentValues values = new ContentValues();
    values.put(DatabaseHelper.NAME, name);
    values.put(DatabaseHelper.TEXT, text);
    values.put(DatabaseHelper.MOD_DATE, currentTime);

    dbHelper.getWritableDatabase().update(DatabaseHelper.TABLE_NAME_NOTE, values,
                                          DatabaseHelper._ID + " = ?",
                                          new String[] {String.valueOf(id)});
    SQLiteDatabase db = dbHelper.getWritableDatabase();
    db.close();
  }

  /**
   * Links a note with a book.
   *
   * @param bookId id of the book to link
   * @param noteId id of the note to link
   */
  public void linkNoteWithBook(Long bookId, Long noteId) {

    try (SQLiteDatabase db = dbHelper.getWritableDatabase()) {
      ContentValues contentValues = new ContentValues();
      contentValues.put(DatabaseHelper.BOOK_ID, bookId);
      contentValues.put(DatabaseHelper.NOTE_ID, noteId);

      db.insert(DatabaseHelper.TABLE_NAME_BOOK_NOTE_LNK, null, contentValues);

    } catch (SQLiteException ex) {
      Log.e(TAG, ex.toString(), ex);
      return;
    }

    updateBookModified(bookId);

  }

  /**
   * Gets all notes of a book by the bookId.
   *
   * @param bookId id of the book
   * @return a list of all noteIds of a book
   */
  public List<Long> getAllNoteIdsForBook(Long bookId) {
    SQLiteDatabase db = dbHelper.getReadableDatabase();

    String selectQuery = "SELECT * FROM " + DatabaseHelper.TABLE_NAME_BOOK_NOTE_LNK + " WHERE "
        + DatabaseHelper.BOOK_ID + " = ?";

    Cursor cursor = db.rawQuery(selectQuery, new String[] {String.valueOf(bookId)});

    List<Long> noteIds = new ArrayList<>();
    if (cursor.moveToFirst()) {
      do {
        noteIds.add(Long.parseLong(cursor.getString(2)));
      } while (cursor.moveToNext());
    }

    cursor.close();

    return noteIds;
  }

  /**
   * Gets a list of all notes that are connected to a specific book.
   *
   * @param bookId id of the book that the result notes must be connected to
   * @return returns a list of connected note-objects
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
   * Gets text string of a specific note.
   *
   * @param id id of the note to look for
   * @return returns the notes text value
   */
  public String findTextById(Long id) {
    SQLiteDatabase db = dbHelper.getReadableDatabase();

    Cursor cursor = db.query(DatabaseHelper.TABLE_NAME_NOTE,
                             new String[] {DatabaseHelper._ID, DatabaseHelper.NAME,
                                 DatabaseHelper.TYPE, DatabaseHelper.TEXT,
                                 DatabaseHelper.CREATE_DATE, DatabaseHelper.MOD_DATE,
                                 DatabaseHelper.NOTE_FILE_ID},
                             DatabaseHelper._ID + " = ?", new String[] {String.valueOf(id)},
                             null, null, null, String.valueOf(1));

    String noteText = null;
    if (cursor.moveToFirst()) {
      noteText = cursor.getString(3);
    }

    cursor.close();

    return noteText;
  }

  /**
   * Finds all text notes which which contain searchInput.
   *
   * @param searchInput searchInput of the user
   * @return returns a list of notes which have the searchInput in the name
   */
  public List<Note> findTextNotesByName(String searchInput) {
    searchInput = escapeHtml(searchInput);

    SQLiteDatabase db = dbHelper.getReadableDatabase();

    String selectQuery = "SELECT n.* FROM " + DatabaseHelper.TABLE_NAME_NOTE
        + " n JOIN " + DatabaseHelper.TABLE_NAME_BOOK_NOTE_LNK
        + " lnk ON (n." + DatabaseHelper._ID + " = lnk." + DatabaseHelper.NOTE_ID + ")"
        + " WHERE n." + DatabaseHelper.TYPE + " = ? AND (n."
        + DatabaseHelper.NAME + " LIKE '%" + searchInput + "%'"
        + " OR n." + DatabaseHelper.TEXT + " LIKE '%" + searchInput + "%')";

    Cursor cursor = db.rawQuery(selectQuery,
                                new String[] {String.valueOf(NoteTypeLut.TEXT.getId())});

    List<Note> noteList = new ArrayList<>();
    if (cursor.moveToFirst()) {
      do {
        Note note = createNoteData(cursor);
        noteList.add(note);

      } while (cursor.moveToNext());
    }

    cursor.close();

    return noteList;
  }

  /**
   * Finds the id of the book by the given noteId.
   *
   * @param noteId id of the note
   * @return returns the book id which contains the noteId
   */
  public Long findBookIdByNoteId(Long noteId) {
    SQLiteDatabase db = dbHelper.getReadableDatabase();

    Cursor cursor = db.query(DatabaseHelper.TABLE_NAME_BOOK_NOTE_LNK,
                             new String[] {DatabaseHelper.BOOK_ID},
                             DatabaseHelper.NOTE_ID + " = ?",
                             new String[] {String.valueOf(noteId)},
                             null, null, null, String.valueOf(1));

    //noinspection WrapperTypeMayBePrimitive
    Long bookId = 0L; // Because all other ids are Long
    if (cursor.moveToFirst()) {
      bookId = cursor.getLong(0);
    }

    cursor.close();

    return bookId;
  }

  /**
   * Gets all text notes of a book by the bookId.
   *
   * @param bookId id of the book
   * @return a list of all text noteIds of a book
   */
  public List<Long> getTextNoteIdsForBook(Long bookId) {
    SQLiteDatabase db = dbHelper.getReadableDatabase();

    String selectQuery = "SELECT lnk." + DatabaseHelper.NOTE_ID + " FROM "
        + DatabaseHelper.TABLE_NAME_BOOK_NOTE_LNK + " lnk JOIN "
        + DatabaseHelper.TABLE_NAME_NOTE
        + " n ON (n." + DatabaseHelper._ID + " = lnk." + DatabaseHelper.NOTE_ID + ")"
        + " WHERE n." + DatabaseHelper.TYPE + " = ? AND lnk." + DatabaseHelper.BOOK_ID + "= ?";

    Cursor cursor = db.rawQuery(selectQuery, new String[] {
        String.valueOf(NoteTypeLut.TEXT.getId()), String.valueOf(bookId)
    });

    List<Long> noteIds = new ArrayList<>();
    if (cursor.moveToFirst()) {
      do {
        noteIds.add(Long.parseLong(cursor.getString(0)));
      } while (cursor.moveToNext());
    }

    cursor.close();

    return noteIds;
  }

}
