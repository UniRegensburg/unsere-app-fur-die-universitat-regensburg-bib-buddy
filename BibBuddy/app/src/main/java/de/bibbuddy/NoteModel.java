package de.bibbuddy;

import android.content.Context;
import java.util.List;

public class NoteModel {

	private final Context context;
	private final NoteDAO noteDao;

	private List<LibraryItem> libraryList;
	private Long currentShelfId;

	public NoteModel(Context context) {
		this.context = context;
		DatabaseHelper databaseHelper = new DatabaseHelper(context);
		this.noteDao = new NoteDAO(databaseHelper);
	}

	public Note newNote() {
		Note note = new Note("", 0, "");
		noteDao.create(note);
		return noteDao.findAll().get(noteDao.findAll().size() - 1);
	}

	public void updateNote(Note note, String text) {
		noteDao.updateNote(note.getId(), text, note.getType(), text, note.getCreateDate(),
			note.getNoteFileId());
	}

	public void deleteNote(Long id){
		noteDao.delete(id);
	}

	public Note getNoteById(Long id){
		return noteDao.findById(id);
	}

	public List<Note> getAllNotes(){
		return noteDao.findAll();
	}

}
