package de.bibbuddy;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class NoteDAO implements INoteDAO {

  private final DatabaseHelper dbHelper;

  public NoteDAO(DatabaseHelper dbHelper) {
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
				DatabaseHelper.NAME, DatabaseHelper.TYPE, DatabaseHelper.TEXT, DatabaseHelper.CREATE_DATE,
				DatabaseHelper.MOD_DATE, DatabaseHelper.NOTE_FILE_ID}, DatabaseHelper._ID + "=?",
			new String[] {String.valueOf(id)}, null, null, null, null);

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

		db.close();
	}

	//Update a single note selected by given id

	/**
	 * This method updates a note object within database selected by its id.
	 *
	 * @param id         note id
	 * @param name       note name
	 * @param type       note type (text, voice, image)
	 * @param text       note text if it is of type text
	 * @param createDate note creation date
	 * @param noteFileId note file id
	 */
	public void updateNote(Long id, String name, int type, String text, Long createDate, Long noteFileId) {
		Long currentTime = new Date().getTime();
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		ContentValues values = new ContentValues();
		//values.put("id", id);
		values.put("name", name);
		//values.put("type", type);
		values.put("text", text);
		//values.put("creation_date", createDate);
		values.put("modification_date", currentTime);
		//values.put("note_file_id", noteFileId);
		dbHelper.getWritableDatabase().update(DatabaseHelper.TABLE_NAME_NOTE, values,
                  DatabaseHelper._ID + " = ?", new String[] {String.valueOf(id)});
		db.close();
    }

    // Link note with book
    public boolean linkNoteWithBook(Long bookId, Long noteId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        try {
            ContentValues contentValues = new ContentValues();
            contentValues.put(DatabaseHelper.BOOK_ID, bookId);
            contentValues.put(DatabaseHelper.NOTE_ID, noteId);

            long id = db.insert(DatabaseHelper.TABLE_NAME_BOOK_NOTE_LNK, null, contentValues);

        } catch (SQLiteException ex) {
            return false;
        } finally {
            db.close();
        }

        return true;
    }


    // get all Notes for a book by the bookId
    private List<Long> getAllNoteIdsForBook(Long bookId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        List<Long> noteIds = new ArrayList<Long>();
        String selectQuery = "SELECT  * FROM " + DatabaseHelper.TABLE_NAME_BOOK_NOTE_LNK + " WHERE " +
                DatabaseHelper.BOOK_ID + "=" + bookId;

        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                // Id, BookId, NoteId
                noteIds.add(Long.parseLong(cursor.getString(2)));
            } while (cursor.moveToNext());
            cursor.close();
        }

        return noteIds;
    }


    // get all Notes for a book with a list of noteIds
    public List<Note> getAllNotesForBook(Long bookId) {
        List<Long> noteIds = getAllNoteIdsForBook(bookId);
        List<Note> noteList = new ArrayList<Note>();

        for(Long id:noteIds) {
            noteList.add(findById(id));
        }

        return noteList;
    }

    public String findTextById(Long id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(DatabaseHelper.TABLE_NAME_NOTE, new String[]{DatabaseHelper._ID,
                        DatabaseHelper.NAME, DatabaseHelper.TYPE, DatabaseHelper.TEXT, DatabaseHelper.CREATE_DATE,
                        DatabaseHelper.MOD_DATE, DatabaseHelper.NOTE_FILE_ID}, DatabaseHelper._ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null, null);

        String noteText = null;
        if (cursor != null) {
            cursor.moveToFirst();
            noteText = cursor.getString(3);

            cursor.close();
        }

        return noteText;
  }

}
