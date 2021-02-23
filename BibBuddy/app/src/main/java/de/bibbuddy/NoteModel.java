package de.bibbuddy;

import android.content.Context;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.jsoup.Jsoup;

/**
 * NoteModel manages all data of the NoteView.
 *
 * @author Sarah Kurek, Sabrina Freisleben
 */
public class NoteModel {

  private final NoteDao noteDao;

  public NoteModel(Context context) {
    DatabaseHelper databaseHelper = new DatabaseHelper(context);
    this.noteDao = new NoteDao(databaseHelper);
  }

  public void addNote(String name, int type, String text) {
    Note note = new Note(name, type, text);
    noteDao.create(note);
  }

  public void updateNote(Note note, String name, String text) {
    noteDao.updateNote(note.getId(), name, text);
  }

  public void deleteNote(Long id) {
    noteDao.delete(id);
  }

  public void clear(){
    noteDao.clear();
  }

  public Note getNoteById(Long id) {
    return noteDao.findById(id);
  }

  public Note getLastNote() {
    return noteDao.findAll().get(noteDao.findAll().size() - 1);
  }

  /**
   * This method is used to fetch the entire noteList from the database.
   *
   * @return a list of NoteItems (used for views)
   */
  public List<NoteItem> getCompleteNoteList() {
    List<Note> noteList = noteDao.findAll();
    List<NoteItem> noteItemList = createItemList(noteList);
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

  /**
   * Method to fetch all notes, that are connected to a specific book, from the database.
   *
   * @param bookId id of the book, whose connected notes are searched for
   * @return returns a list of NoteItems that are connected to the specific book
   */
  public List<NoteItem> getNoteListForBook(Long bookId) {
    List<Note> noteList = noteDao.getAllNotesForBook(bookId);
    List<NoteItem> noteItemList = createItemList(noteList);
    return noteItemList;
  }

  private List<NoteItem> createItemList(List<Note> noteList) {
    List<NoteItem> noteItemList = new ArrayList<>();
    for (Note note : noteList) {
      Long noteId = note.getId();
      String modDate = getDate(note.getModDate());
      String name = "";
      if (note.getType() == 0) {
        name = note.getName();
        name = Jsoup.parse(name).text();
        if (name.length() > 40) {
          name = name.substring(0, 35) + " ...";
        }
        noteItemList.add(new NoteTextItem(modDate, name, noteId));
      } else if (note.getType() == 1) {
        noteItemList.add(new NoteAudioItem(modDate, name, noteId));
      } else {
        noteItemList.add(new NoteImageItem(modDate, name, noteId));
      }
    }
    return noteItemList;
  }

  public String getNoteText(Long noteId) {
    return noteDao.findTextById(noteId);
  }

  public void linkNoteWithBook(Long bookId, Long noteId) {
    noteDao.linkNoteWithBook(bookId, noteId);
  }

}
