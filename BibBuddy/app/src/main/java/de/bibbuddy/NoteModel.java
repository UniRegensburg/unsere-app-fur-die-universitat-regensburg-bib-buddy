package de.bibbuddy;

import android.content.Context;

import org.jsoup.Jsoup;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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

	public List<NoteItem> getNoteList() {
		List<Note> noteList = noteDao.findAll();

		List<NoteItem> noteItemList = new ArrayList<>();
		for (Note note : noteList) {
			Long noteId = note.getId();
			String name = note.getName();
			name = Jsoup.parse(name).text();
			int image;
			if (name.length() > 40) {
				name = name.substring(0, 35) + " ...";
			}
			if(note.getType() == 0){
				image = R.drawable.document;
			} else if (note.getType() == 1){
				image = R.drawable.picture;
			} else {
				image = R.drawable.microphone;
			}
			String modDate = getDate(note.getModDate());
			noteItemList.add(new NoteItem(modDate, name, image, noteId));
		}
		return noteItemList;
	}

	private String getDate(Long date) {
		Date d = new Date(date);
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");

		String string = simpleDateFormat.format(d);
		String day = string.substring(8, 10);
		String month = string.substring(5, 7);
		String year = string.substring(0, 4);
		String time = string.substring(11, 16);

		string = day + "." + month + "." + year + " " + time + " Uhr";

		return string;
	}

}
