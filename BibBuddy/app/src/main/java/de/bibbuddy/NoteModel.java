package de.bibbuddy;

import android.content.Context;
import java.util.List;

public class NoteModel {

	private final NoteDAO noteDao;

	public NoteModel(Context context) {
		DatabaseHelper databaseHelper = new DatabaseHelper(context);
		this.noteDao = new NoteDAO(databaseHelper);
	}

	public void addNote(String name, int type, String text) {
		Note note = new Note(name, type, text);
		noteDao.create(note);
	}

	public void updateNote(Note note, String name, String text) {
		noteDao.updateNote(note.getId(), name, text);
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
